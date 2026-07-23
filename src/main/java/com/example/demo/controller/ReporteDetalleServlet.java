
package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import com.example.demo.model.ImagenReporte;
import com.example.demo.model.Reporte;
import com.example.demo.model.dao.ImagenReporteDao;
import com.example.demo.model.dao.ReporteDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Detalle y llenado del reporte de visita (RF-08a+b).
 *  - GET  ?id=N        muestra el detalle/formulario del reporte.
 *  - GET  ?imagen=N     muestra una imagen del reporte (para <img src>).
 *  - POST ?id=N         guarda resultados/observaciones y hasta 3 imágenes
 *                        (RN-07). Solo el docente dueño, y solo si el
 *                        reporte sigue Pendiente.
 *
 * Patrón de acceso igual al de DetalleSolicitudServlet.cargarSolicitudPermitida()
 * y de carga de archivos igual al de DocumentoServlet (@MultipartConfig +
 * validarImagen() como su validarPdf()).
 */
@WebServlet(name = "ReporteDetalleServlet", value = "/reporte")
@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 18L * 1024 * 1024)
public class ReporteDetalleServlet extends HttpServlet {

/**
 * TODO CONFIRMAR: nombre del estado en ESTADO_REPORTE al que pasa el
 * reporte cuando el docente guarda el formulario. No aparece en el
 * código existente (solo 'Pendiente'); si en la BD real se llama
 * distinto (ej. "Enviado"), cambiar solo esta constante.
 */
public static final String ESTADO_COMPLETADO = "Completado";

private static final long MAX_IMG_BYTES = 5L * 1024 * 1024;
private static final int MAX_IMAGENES = 3;

private final ReporteDao reporteDao = new ReporteDao();
private final ImagenReporteDao imagenReporteDao = new ImagenReporteDao();

@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException {

String idImagen = request.getParameter("imagen");
if (idImagen != null) {
mostrarImagen(idImagen, request, response);
return;
}

Reporte reporte = cargarReportePermitido(request);
if (reporte == null) {
response.sendRedirect("indexSv");
return;
}

HttpSession session = request.getSession(false);
Integer idUsuario = (Integer) session.getAttribute("idUsuario");
boolean esDueno = reporte.getIdUsuarioSolicitante() == idUsuario;

request.setAttribute("reporte", reporte);
request.setAttribute("esDueno", esDueno);
request.setAttribute("imagenes", imagenReporteDao.getByReporte(reporte.getIdReporte()));
request.getRequestDispatcher("reporte-detalle.jsp").forward(request, response);
}

@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException {
request.setCharacterEncoding("UTF-8");

Reporte reporte = cargarReportePermitido(request);
if (reporte == null) {
response.sendRedirect("indexSv");
return;
}
int idReporte = reporte.getIdReporte();

HttpSession session = request.getSession(false);
Integer idUsuario = (Integer) session.getAttribute("idUsuario");
boolean esDueno = reporte.getIdUsuarioSolicitante() == idUsuario;
boolean pendiente = "Pendiente".equalsIgnoreCase(reporte.getNombreEstado());

// Solo el docente dueño puede llenar el reporte, y solo si sigue Pendiente
if (!esDueno || !pendiente) {
response.sendRedirect("reporte?id=" + idReporte);
return;
}

String resultados = request.getParameter("resultados");
if (resultados == null || resultados.isBlank()) {
response.sendRedirect("reporte?id=" + idReporte + "&error=vacio");
return;
}
String observaciones = request.getParameter("observaciones");

// Imágenes nuevas: el input del formulario manda varias partes con
// el mismo name="imagenes" (una por cada archivo seleccionado)
List<Part> partesImagen = new ArrayList<>();
try {
for (Part p : request.getParts()) {
if ("imagenes".equals(p.getName()) && p.getSize() > 0) {
partesImagen.add(p);
}
}
} catch (IllegalStateException e) {
// El contenedor rechazó una parte por exceder maxFileSize (RN-07)
response.sendRedirect("reporte?id=" + idReporte + "&error=tamano");
return;
}

int existentes = imagenReporteDao.contarPorReporte(idReporte);
if (existentes + partesImagen.size() > MAX_IMAGENES) {
response.sendRedirect("reporte?id=" + idReporte + "&error=maximo");
return;
}

for (Part p : partesImagen) {
String error = validarImagen(p);
if (error != null) {
response.sendRedirect("reporte?id=" + idReporte + "&error=" + error);
return;
}
}

boolean ok = reporteDao.completarFormulario(idReporte, resultados.trim(),
observaciones != null ? observaciones.trim() : null);

for (Part p : partesImagen) {
byte[] contenido;
try (InputStream in = p.getInputStream()) {
contenido = in.readAllBytes();
}
imagenReporteDao.guardar(idReporte, Base64.getEncoder().encodeToString(contenido),
p.getContentType());
}

// Patrón PRG
response.sendRedirect("reporte?id=" + idReporte + (ok ? "&guardado=1" : "&error=guardar"));
}

/** Solo JPG/PNG y máximo 5 MB por imagen (RN-07). */
private String validarImagen(Part imagen) {
if (imagen.getSize() > MAX_IMG_BYTES) {
return "tamano";
}
String tipo = imagen.getContentType();
String nombre = imagen.getSubmittedFileName();
boolean tipoValido = "image/jpeg".equalsIgnoreCase(tipo) || "image/png".equalsIgnoreCase(tipo);
boolean extensionValida = nombre != null && (nombre.toLowerCase().endsWith(".jpg")
|| nombre.toLowerCase().endsWith(".jpeg") || nombre.toLowerCase().endsWith(".png"));
return (tipoValido && extensionValida) ? null : "tipo";
}

/** Sirve el binario de una imagen para <img src="reporte?imagen=ID">. */
private void mostrarImagen(String idParam, HttpServletRequest request, HttpServletResponse response)
throws IOException {
int idImagen;
try {
idImagen = Integer.parseInt(idParam);
} catch (NumberFormatException e) {
response.sendError(HttpServletResponse.SC_BAD_REQUEST);
return;
}

ImagenReporte img = imagenReporteDao.getById(idImagen);
if (img == null) {
response.sendError(HttpServletResponse.SC_NOT_FOUND);
return;
}

Reporte reporte = reporteDao.getById(img.getIdReporte());
if (reporte == null || !puedeVer(request, reporte)) {
response.sendError(HttpServletResponse.SC_FORBIDDEN);
return;
}

byte[] contenido = Base64.getDecoder().decode(img.getContenidoBase64());
response.setContentType(img.getTipoMime());
response.setContentLengthLong(contenido.length);
try (OutputStream out = response.getOutputStream()) {
out.write(contenido);
}
}

/**
 * Carga el reporte validando el acceso (RNF-08): el docente solo ve los
 * de sus propias solicitudes; Estadías/Admin pueden ver cualquiera.
 * Mismo patrón que DetalleSolicitudServlet.cargarSolicitudPermitida().
 */
private Reporte cargarReportePermitido(HttpServletRequest request) {
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

Reporte reporte = reporteDao.getById(id);
if (reporte == null) {
return null;
}
return puedeVer(request, reporte) ? reporte : null;
}

/** Regla de acceso de solo lectura: docente dueño, o Estadías/Admin (cualquiera). */
private boolean puedeVer(HttpServletRequest request, Reporte reporte) {
HttpSession session = request.getSession(false);
Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
if (idUsuario == null) {
return false;
}
String rol = (String) session.getAttribute("rol");
boolean esDocente = rol == null || "Docente".equalsIgnoreCase(rol);
if (esDocente) {
return reporte.getIdUsuarioSolicitante() == idUsuario;
}
return true;
}
}