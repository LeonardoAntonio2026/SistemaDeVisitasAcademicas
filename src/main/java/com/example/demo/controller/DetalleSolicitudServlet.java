package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.demo.model.Solicitud;
import com.example.demo.model.dao.DocumentoDao;
import com.example.demo.model.dao.ReporteDao;
import com.example.demo.model.dao.SolicitudDao;
import com.example.demo.utils.EmailSender;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Página de detalles de la visita: el corazón del sistema. Es UNA sola página
 * cuyas cards se muestran u ocultan según el rol y el estado de la solicitud
 * (el docente ve la carga de archivos; el coordinador la card de evaluación).
 */
@WebServlet(name = "DetalleSolicitudServlet", value = "/detalle")
public class DetalleSolicitudServlet extends HttpServlet {

    public static final String TIPO_FO_FIRMADO = "FO-UTEZ-EST-08 firmado";
    public static final String TIPO_CARTA_RESPONSIVA = "Carta responsiva";

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final DocumentoDao documentoDao = new DocumentoDao();
    private final ReporteDao reporteDao = new ReporteDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Solicitud solicitud = cargarSolicitudPermitida(request);
        if (solicitud == null) {
            response.sendRedirect("indexSv");
            return;
        }

        request.setAttribute("solicitud", solicitud);
        request.setAttribute("documentos", documentoDao.getBySolicitud(solicitud.getIdSolicitud()));
        request.setAttribute("existeFirmado",
                documentoDao.existeTipoEnSolicitud(solicitud.getIdSolicitud(), TIPO_FO_FIRMADO));
        request.setAttribute("reporte", reporteDao.getBySolicitud(solicitud.getIdSolicitud()));

        request.getRequestDispatcher("detalle.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Solicitud solicitud = cargarSolicitudPermitida(request);
        if (solicitud == null) {
            response.sendRedirect("indexSv");
            return;
        }

        HttpSession session = request.getSession(false);
        String rol = (String) session.getAttribute("rol");
        boolean esDocente = rol == null || "Docente".equalsIgnoreCase(rol);
        String action = request.getParameter("action");
        int id = solicitud.getIdSolicitud();

        if ("enviar".equals(action)) {
            // El paso "Enviar solicitud" solo se completa dando click en ENVIAR,
            // y solo si el docente ya subió su FO-UTEZ-EST-08 firmado (RN-02)
            if (esDocente && "Pendiente".equalsIgnoreCase(solicitud.getNombreEstado())
                    && documentoDao.existeTipoEnSolicitud(id, TIPO_FO_FIRMADO)) {
                solicitudDao.cambiarEstado(id, "En revisión");
            }
        } else if ("aprobar".equals(action) || "rechazar".equals(action)) {
            // Solo el coordinador de Estadías (o Admin) evalúa, y solo En revisión
            String motivo = request.getParameter("motivo");
            boolean esRechazo = "rechazar".equals(action);
            if (!esDocente && "En revisión".equalsIgnoreCase(solicitud.getNombreEstado())
                    && (!esRechazo || (motivo != null && !motivo.isBlank()))) {

                Integer idAutoriza = (Integer) session.getAttribute("idUsuario");
                String nuevoEstado = esRechazo ? "Rechazada" : "Aprobada";
                boolean ok = solicitudDao.decidir(id, nuevoEstado,
                        motivo != null ? motivo.trim() : null, idAutoriza);
                if (ok) {
                    notificarDecision(solicitud, nuevoEstado, motivo);
                }
            }
        }

        // Patrón PRG: recargar los detalles ya con el nuevo estado
        response.sendRedirect("detalle?id=" + id);
    }

    /**
     * Carga la solicitud validando el acceso: el docente solo ve las suyas y
     * el coordinador solo las que ya fueron enviadas
     */
    private Solicitud cargarSolicitudPermitida(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        if (idUsuario == null) {
            return null;
        }

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            return null;
        }

        Solicitud solicitud = solicitudDao.getById(id);
        if (solicitud == null) {
            return null;
        }

        String rol = (String) session.getAttribute("rol");
        boolean esDocente = rol == null || "Docente".equalsIgnoreCase(rol);
        if (esDocente) {
            return solicitud.getIdUsuarioSolicitante() == idUsuario ? solicitud : null;
        }
        // Coordinador/Admin: las Pendientes aún no se envían, no le aparecen
        return "Pendiente".equalsIgnoreCase(solicitud.getNombreEstado()) ? null : solicitud;
    }

    /** Correo automático al docente cuando su solicitud es aprobada o rechazada  */
    private void notificarDecision(Solicitud solicitud, String nuevoEstado, String motivo) {
        if (solicitud.getCorreoSolicitante() == null) {
            return;
        }
        boolean aprobada = "Aprobada".equals(nuevoEstado);
        String plantillaHtml = """
        <html>
            <body style="font-family: Arial, sans-serif; color: #333333;">
                <h2 style="color: #183052;">Tu solicitud fue {0}</h2>
                <p>La solicitud de visita a <strong>{1}</strong> fue <strong>{0}</strong> por el área de Estadías.</p>
                {2}
                <p>{3}</p>
                <p style="font-size: 12px; color: #777777;">Sistema de Gestión de Visitas Académicas - UTEZ</p>
            </body>
        </html>
        """;
        String bloqueMotivo = (motivo != null && !motivo.isBlank())
                ? "<p><strong>Motivo:</strong> " + motivo + "</p>"
                : "";
        String siguientePaso = aprobada
                ? "Entra a los detalles de tu solicitud para descargar el oficio y subir tu carta responsiva firmada."
                : "Puedes consultar los detalles en el sistema.";
        String cuerpo = MessageFormat.format(plantillaHtml,
                aprobada ? "aprobada" : "rechazada",
                solicitud.getNombreEmpresaActividad(), bloqueMotivo, siguientePaso);
        try {
            EmailSender.sendMail(solicitud.getCorreoSolicitante(),
                    "Solicitud " + (aprobada ? "aprobada" : "rechazada") + " - Visitas Académicas",
                    cuerpo);
        } catch (RuntimeException e) {
            // La decisión ya quedó guardada; si falla el correo no bloqueamos el flujo
            System.err.println("No se pudo enviar la notificación: " + e.getMessage());
        }
    }
}
