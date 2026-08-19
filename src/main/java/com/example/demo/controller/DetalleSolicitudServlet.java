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
 * Controlador servlet encargado de gestionar la visita detallada de una visita acádemica.
 * <p>
 *     Funciona como el núcleo dinámico de interacción en el sistema, adaptando el contenido de la visita
 *     según el rol del usuario (Docente solicitante o Estadias supervisor) así cómo el estado actual de la solicitud
 * </p>
 * @author Eder Gabriel García Vázquez
 * @since 18/08/2026
 */
@WebServlet(name = "DetalleSolicitudServlet", value = "/detalle")
public class DetalleSolicitudServlet extends HttpServlet {

    public static final String TIPO_FO_FIRMADO = "FO-UTEZ-EST-08 firmado";
    public static final String TIPO_CARTA_RESPONSIVA = "Carta responsiva";

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final DocumentoDao documentoDao = new DocumentoDao();
    private final ReporteDao reporteDao = new ReporteDao();

    /**
     * Procesa las peticiones HTTP {@code GET} par cargar y renderizar los detalles de una solcitud.
     * <p>
     *     Carga la solitud validando los permisos del usuario, sus documentos asociados, la existencia
     *     del formato firmado y el reporte de la visita si existiera.
     * </p>
     * @param request objeto {@link HttpServletRequest} con el parámetro "id" de la solicitud.
     * @param response objeto {@link HttpServletResponse} para renderizar la visita o envíar errores HTTP
     * @throws ServletException si ocurre un error cuándo se envía la petición
     * @throws IOException si ocurre un error de entrada/salida.
     * @author Eder Gabriel García Vázquez
     * since 18/08/2026
     */

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

    /**
     * Procesa las peticiones HTTP {@code POST} para la toma de decisiones y cambios de estado de la solicitud.
     * <p>
     *     Maneja las siguientes acciones:
     *     {code Enviar} Cambio de estado a "En revisión" por parte del docente tras adjuntar el archivo firmado
     *     {code Aprobar} Autorización por parte del supervisor (Estadías)
     *     {code Rechazar} Devolución de la solicitud con un motivo para su posterior edición
     *     Utiliza el patrón PRG (Post/Redirect/Get) añadiendo parámetros de control o error a la URL de dirección
     * </p>
     * @param request objeto {@link HttpServletRequest} con la acción a ejecutar y sus parámetros
     * @param response objeto {@link HttpServletResponse} para la redirección o respuesta del error
     * @throws ServletException si ocurre un error en el Servlet
     * @throws IOException si ocurre un error en la entrada/salida de la petición
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Solicitud solicitud = cargarSolicitudPermitida(request, response);
        if (solicitud == null) {
            return; // el helper ya mandó la página de error que corresponde
        }

        boolean esDocente = SesionUtils.esDocente(request);
        String action = request.getParameter("action");
        int id = solicitud.getIdSolicitud();

        // Clave del aviso que verá el usuario si la operación no se pudo hacer;
        // null significa que todo salió bien. Antes cualquier fallo terminaba en
        // la misma recarga sin explicación y el botón parecía no responder.
        String error = null;

        if ("enviar".equals(action)) {
            // El paso "Enviar solicitud" solo se completa dando click en ENVIAR,
            // y solo si el docente ya subió su FO-UTEZ-EST-08 firmado (RN-02)
            if (!esDocente) {
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
            if (esDocente) {
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
     * Carga la solicitud validando el acceso: el docente solo ve las suyas y
     * el coordinador solo las que ya fueron enviadas.
     *
     * Devuelve null cuando no se puede mostrar, pero antes manda la página de
     * error que corresponde: 404 si la solicitud no existe y 403 si existe pero
     * no es de quien la pide. Antes los dos casos redirigían al inicio en
     * silencio y parecía que el sistema no había hecho nada.
     */

    /**
     * Recupera y valida la solicitud dependiendo la solicitud especificada en la petición
     * Reglas de acceso:
     * Los docentes solo pueden ver las solicitudes creadas por ellos mismos.
     * Los coordinadores o supervisores no pueden consultar solicitudes en estado "Pendiente"
     * En caso de denegación o ausencia, emite las respuestas de HTTP 404 (Not found) o 403 (Forbidden)
     * @param request petición HTTP con el parámetro Id
     * @param response respuesta HTTP
     * @return el objeto {@link Solicitud} validado, o {@code null} si el acceso no es permitido o no existe
     * @throws IOException si ocurre un fallo al emitir el error HTTP en la respuesta.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
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

        boolean permitida = SesionUtils.esDocente(request)
                ? solicitud.getIdUsuarioSolicitante() == idUsuario
                // Coordinador/Admin: las Pendientes aún no se envían, no le aparecen
                : !"Pendiente".equalsIgnoreCase(solicitud.getNombreEstado());

        if (!permitida) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return solicitud;
    }

    /**
     * Construye y envía una notificación por correo electrónico de manera asíncrona al docente solicitante
     * informando sobre la resolución (Aprobada/Rechazada) de su visita.
     * @param solicitud la {@link Solicitud} evaluada.
     * @param nuevoEstado el dictamen emitido ("Aprobado o Rechazado")
     * @param motivo la justificación o retroalimentación otorgada de parte de Estadías.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
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
