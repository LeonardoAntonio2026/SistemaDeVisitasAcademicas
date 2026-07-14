package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import com.example.demo.model.Documento;
import com.example.demo.model.Solicitud;
import com.example.demo.model.dao.DocumentoDao;
import com.example.demo.model.dao.ReporteDao;
import com.example.demo.model.dao.SolicitudDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * Archivos del proceso:
 *  - GET  ?id=N               descarga un documento subido (PDF en Base64).
 *  - GET  ?gen=fo|oficio|responsiva&solicitud=N  vista imprimible del formato
 *    generado a partir de los datos (se imprime o guarda como PDF y se firma).
 *  - POST action=firmado|responsiva  sube el PDF firmado (máx 10 MB, RN-07).
 */
@WebServlet(name = "DocumentoServlet", value = "/documento")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class DocumentoServlet extends HttpServlet {

    private static final long MAX_PDF_BYTES = 10L * 1024 * 1024;

    private final DocumentoDao documentoDao = new DocumentoDao();
    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final ReporteDao reporteDao = new ReporteDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String gen = request.getParameter("gen");
        if (gen != null) {
            generarFormato(gen, request, response);
            return;
        }

        int idDocumento;
        try {
            idDocumento = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Documento doc = documentoDao.getById(idDocumento);
        if (doc == null || doc.getIdSolicitud() == null
                || solicitudPermitida(request, doc.getIdSolicitud()) == null) {
            response.sendRedirect("indexSv");
            return;
        }

        byte[] contenido = Base64.getDecoder().decode(doc.getContenidoBase64());
        String nombre = doc.getNombreTipo().replace(' ', '_') + ".pdf";
        response.setContentType("application/pdf");
        response.setContentLengthLong(contenido.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
        try (OutputStream out = response.getOutputStream()) {
            out.write(contenido);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        int idSolicitud;
        try {
            idSolicitud = Integer.parseInt(request.getParameter("solicitud"));
        } catch (NumberFormatException e) {
            response.sendRedirect("indexSv");
            return;
        }

        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        Solicitud solicitud = solicitudDao.getById(idSolicitud);

        // Los archivos solo los sube el docente dueño de la solicitud
        if (idUsuario == null || solicitud == null
                || solicitud.getIdUsuarioSolicitante() != idUsuario) {
            response.sendRedirect("indexSv");
            return;
        }

        String action = request.getParameter("action");
        String estado = solicitud.getNombreEstado();
        String tipo = null;
        // La zona de carga solo se abre 2 veces en el proceso (y según el estado)
        if ("firmado".equals(action) && "Pendiente".equalsIgnoreCase(estado)) {
            tipo = DetalleSolicitudServlet.TIPO_FO_FIRMADO;
        } else if ("responsiva".equals(action) && "Aprobada".equalsIgnoreCase(estado)) {
            tipo = DetalleSolicitudServlet.TIPO_CARTA_RESPONSIVA;
        }
        if (tipo == null) {
            response.sendRedirect("detalle?id=" + idSolicitud);
            return;
        }

        Part archivo = request.getPart("archivo");
        String error = validarPdf(archivo);
        if (error != null) {
            response.sendRedirect("detalle?id=" + idSolicitud + "&error=" + error);
            return;
        }

        byte[] contenido;
        try (InputStream in = archivo.getInputStream()) {
            contenido = in.readAllBytes();
        }
        boolean guardado = documentoDao.guardarParaSolicitud(idSolicitud, tipo,
                Base64.getEncoder().encodeToString(contenido));

        // Al subir la carta responsiva firmada la solicitud se cierra como
        // Completada y se abre su reporte pendiente (RN-05, RN-06)
        if (guardado && "responsiva".equals(action)) {
            solicitudDao.cambiarEstado(idSolicitud, "Completada");
            if (!reporteDao.existePorSolicitud(idSolicitud)) {
                reporteDao.crearPendiente(idSolicitud);
            }
        }

        // El parámetro subido= muestra la confirmación de carga en los detalles
        response.sendRedirect("detalle?id=" + idSolicitud + (guardado ? "&subido=" + action : "&error=guardar"));
    }

    /** Solo PDF y máximo 10 MB (RN-07). Devuelve la clave del error o null si es válido. */
    private String validarPdf(Part archivo) {
        if (archivo == null || archivo.getSize() == 0) {
            return "vacio";
        }
        if (archivo.getSize() > MAX_PDF_BYTES) {
            return "tamano";
        }
        String nombre = archivo.getSubmittedFileName();
        boolean esPdf = "application/pdf".equalsIgnoreCase(archivo.getContentType())
                || (nombre != null && nombre.toLowerCase().endsWith(".pdf"));
        return esPdf ? null : "tipo";
    }

    /** Vista imprimible del documento generado con los datos de la solicitud. */
    private void generarFormato(String gen, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idSolicitud;
        try {
            idSolicitud = Integer.parseInt(request.getParameter("solicitud"));
        } catch (NumberFormatException e) {
            response.sendRedirect("indexSv");
            return;
        }

        Solicitud solicitud = solicitudPermitida(request, idSolicitud);
        if (solicitud == null) {
            response.sendRedirect("indexSv");
            return;
        }

        String estado = solicitud.getNombreEstado();
        boolean aprobadaOMas = "Aprobada".equalsIgnoreCase(estado) || "Completada".equalsIgnoreCase(estado);
        // El oficio y la carta responsiva se generan hasta que Estadías aprueba
        if (("oficio".equals(gen) || "responsiva".equals(gen)) && !aprobadaOMas) {
            response.sendRedirect("detalle?id=" + idSolicitud);
            return;
        }
        if (!"fo".equals(gen) && !"oficio".equals(gen) && !"responsiva".equals(gen)) {
            response.sendRedirect("detalle?id=" + idSolicitud);
            return;
        }

        request.setAttribute("solicitud", solicitud);
        request.setAttribute("tipoFormato", gen);
        request.getRequestDispatcher("documento-impreso.jsp").forward(request, response);
    }

    /** Mismas reglas de acceso que la página de detalles. */
    private Solicitud solicitudPermitida(HttpServletRequest request, int idSolicitud) {
        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        if (idUsuario == null) {
            return null;
        }
        Solicitud solicitud = solicitudDao.getById(idSolicitud);
        if (solicitud == null) {
            return null;
        }
        String rol = (String) session.getAttribute("rol");
        boolean esDocente = rol == null || "Docente".equalsIgnoreCase(rol);
        if (esDocente) {
            return solicitud.getIdUsuarioSolicitante() == idUsuario ? solicitud : null;
        }
        return "Pendiente".equalsIgnoreCase(solicitud.getNombreEstado()) ? null : solicitud;
    }
}
