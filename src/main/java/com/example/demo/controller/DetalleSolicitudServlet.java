package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Solicitud;
import com.example.demo.model.dao.DocumentoDao;
import com.example.demo.model.dao.ReporteDao;
import com.example.demo.model.dao.SolicitudDao;
import com.example.demo.utils.EmailSender;
import com.example.demo.utils.SesionUtils;

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
        Solicitud solicitud = cargarSolicitudPermitida(request, response);
        if (solicitud == null) {
            return; // el helper ya mandó la página de error que corresponde
        }

        request.setAttribute("solicitud", solicitud);
        request.setAttribute("documentos", documentoDao.getBySolicitud(solicitud.getIdSolicitud()));
        request.setAttribute("existeFirmado",
                documentoDao.existeTipoEnSolicitud(solicitud.getIdSolicitud(), TIPO_FO_FIRMADO));
        // La vista lo usa para poner el botón "Reemplazar" solo en la fila
        // del FO firmado (el único archivo que se puede reemplazar)
        request.setAttribute("tipoFoFirmado", TIPO_FO_FIRMADO);
        request.setAttribute("reporte", reporteDao.getBySolicitud(solicitud.getIdSolicitud()));

        request.getRequestDispatcher("detalle.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Solicitud solicitud = cargarSolicitudPermitida(request, response);
        if (solicitud == null) {
            return; // el helper ya mandó la página de error que corresponde
        }

        // Lo del docente lo hace el DUEÑO de la solicitud, no "el rol Docente":
        // el Administrador también levanta las suyas y con ellas le toca
        // firmar y enviar como cualquier docente
        boolean esDueno = solicitud.getIdUsuarioSolicitante() == SesionUtils.idUsuario(request);
        String action = request.getParameter("action");
        int id = solicitud.getIdSolicitud();

        // Clave del aviso que verá el usuario si la operación no se pudo hacer;
        // null significa que todo salió bien. Antes cualquier fallo terminaba en
        // la misma recarga sin explicación y el botón parecía no responder.
        String error = null;

        if ("enviar".equals(action)) {
            // El paso "Enviar solicitud" solo se completa dando click en ENVIAR,
            // y solo si el dueño ya subió su FO-UTEZ-EST-08 firmado (RN-02)
            if (!esDueno) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if (!"Pendiente".equalsIgnoreCase(solicitud.getNombreEstado())) {
                error = "enviada";
            } else if (!documentoDao.existeTipoEnSolicitud(id, TIPO_FO_FIRMADO)) {
                error = "sinfirmado";
            } else if (!solicitudDao.cambiarEstado(id, "En revisión")) {
                error = "guardar";
            }
        } else if ("aprobar".equals(action) || "rechazar".equals(action)) {
            // Solo el coordinador de Estadías (o Admin) evalúa, y solo En revisión
            if (!SesionUtils.esRevisor(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String motivo = request.getParameter("motivo");
            boolean esRechazo = "rechazar".equals(action);

            if (!"En revisión".equalsIgnoreCase(solicitud.getNombreEstado())) {
                error = "yaevaluada";
            } else if (esRechazo && (motivo == null || motivo.isBlank())) {
                error = "sinmotivo";
            } else {
                Integer idAutoriza = SesionUtils.idUsuario(request);
                String nuevoEstado = esRechazo ? "Rechazada" : "Aprobada";
                boolean ok = solicitudDao.decidir(id, nuevoEstado,
                        motivo != null ? motivo.trim() : null, idAutoriza);
                if (ok) {
                    notificarDecision(solicitud, nuevoEstado, motivo);
                } else {
                    error = "guardar";
                }
            }
        }

        // Patrón PRG: recargar los detalles ya con el nuevo estado
        response.sendRedirect("detalle?id=" + id + (error != null ? "&error=" + error : ""));
    }

    /**
     * Carga la solicitud validando el acceso: la suya la ve siempre quien la
     * creó, y las ajenas solo el revisor y solo si ya fueron enviadas.
     *
     * Se pregunta primero por el dueño y no por el rol porque el Administrador
     * es las dos cosas: con sus propias solicitudes entra como dueño (incluso
     * Pendientes, que es cuando firma y envía) y con las demás como revisor.
     *
     * Devuelve null cuando no se puede mostrar, pero antes manda la página de
     * error que corresponde: 404 si la solicitud no existe y 403 si existe pero
     * no es de quien la pide. Antes los dos casos redirigían al inicio en
     * silencio y parecía que el sistema no había hecho nada.
     */
    private Solicitud cargarSolicitudPermitida(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer idUsuario = SesionUtils.idUsuario(request);
        if (idUsuario == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Solicitud solicitud = solicitudDao.getById(id);
        if (solicitud == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        boolean permitida = solicitud.getIdUsuarioSolicitante() == idUsuario
                // El Administrador entra a cualquiera, en cualquier estado:
                // también le toca corregirle los datos al docente, y si no
                // vería las Pendientes no podría abrir la que acaba de corregir
                || SesionUtils.esAdministrador(request)
                // Estadías: las Pendientes no, porque el docente todavía no las envía
                || (SesionUtils.esRevisor(request)
                    && !"Pendiente".equalsIgnoreCase(solicitud.getNombreEstado()));

        if (!permitida) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return solicitud;
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
        // En un hilo aparte: la decisión ya quedó guardada y el SMTP tarda
        // varios segundos; el coordinador no tiene que esperar a Gmail
        EmailSender.sendMailAsync(solicitud.getCorreoSolicitante(),
                "Solicitud " + (aprobada ? "aprobada" : "rechazada") + " - Visitas Académicas",
                cuerpo);
    }
}
