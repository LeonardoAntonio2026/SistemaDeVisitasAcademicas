
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
import com.example.demo.model.dao.DocumentoDao;
import com.example.demo.model.dao.ImagenReporteDao;
import com.example.demo.model.dao.ReporteDao;
import com.example.demo.utils.EmailSender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Detalle y flujo completo del reporte de visita (RF-08):
 *  - GET  ?id=N             detalle del reporte según estado/rol/sub-fase.
 *  - GET  ?imagen=N         sirve una imagen del reporte (para <img src>).
 *  - POST action=generar    guarda resultados/observaciones + 3 imágenes y
 *                           deja el reporte listo para firmar (Pendiente).
 *  - POST action=enviar     con el PDF firmado ya subido: Pendiente -> Completado.
 *  - POST action=aprobar|rechazar  Estadías evalúa: Completado -> Aprobado/Rechazado.
 *
 * El flujo imita al de la solicitud (DetalleSolicitudServlet + DocumentoServlet):
 * el docente genera el formato imprimible, lo firma, sube el PDF y lo envía.
 */
@WebServlet(name = "ReporteDetalleServlet", value = "/reporte")
@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 18L * 1024 * 1024)
public class ReporteDetalleServlet extends HttpServlet {

/** Tipo del PDF firmado del reporte (fila de TIPO_DOCUMENTO, ver sql/). */
public static final String TIPO_REPORTE_FIRMADO = "Reporte de visita firmado";

private static final long MAX_IMG_BYTES = 5L * 1024 * 1024;
/** El reporte lleva exactamente 3 imágenes de evidencia (RN-07). */
private static final int IMAGENES_REQUERIDAS = 3;

private final ReporteDao reporteDao = new ReporteDao();
private final ImagenReporteDao imagenReporteDao = new ImagenReporteDao();
private final DocumentoDao documentoDao = new DocumentoDao();

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
int idReporte = reporte.getIdReporte();

HttpSession session = request.getSession(false);
Integer idUsuario = (Integer) session.getAttribute("idUsuario");
boolean esDueno = reporte.getIdUsuarioSolicitante() == idUsuario;

request.setAttribute("reporte", reporte);
request.setAttribute("esDueno", esDueno);
request.setAttribute("imagenes", imagenReporteDao.getByReporte(idReporte));
request.setAttribute("documentos", documentoDao.getByReporte(idReporte));
request.setAttribute("existeFirmado",
documentoDao.existeTipoEnReporte(idReporte, TIPO_REPORTE_FIRMADO));
request.setAttribute("subFase", calcularSubFase(request, reporte, esDueno));
request.getRequestDispatcher("reporte-detalle.jsp").forward(request, response);
}

/**
 * Sub-fase del docente dentro del estado Pendiente (y la edición de un
 * Rechazado). El estado en BD no cambia hasta generar/enviar:
 *  - "formulario": captura o corrección (Pendiente sin resultados, o
 *    ?editar=1 del dueño en Pendiente/Rechazado).
 *  - "firmar": ya generó; descarga el formato, sube el firmado y envía.
 *  - null: no aplica (otros estados o quien mira no es el dueño).
 */
private String calcularSubFase(HttpServletRequest request, Reporte reporte, boolean esDueno) {
if (!esDueno) {
return null;
}
String estado = reporte.getNombreEstado();
boolean tieneResultados = reporte.getResultados() != null && !reporte.getResultados().isBlank();
boolean editar = "1".equals(request.getParameter("editar"));

if ("Pendiente".equalsIgnoreCase(estado)) {
return (!tieneResultados || editar) ? "formulario" : "firmar";
}
if ("Rechazado".equalsIgnoreCase(estado) && editar) {
return "formulario";
}
return null;
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
String rol = (String) session.getAttribute("rol");
boolean esDocente = rol == null || "Docente".equalsIgnoreCase(rol);
boolean esDueno = reporte.getIdUsuarioSolicitante() == idUsuario;
String estado = reporte.getNombreEstado();
String action = request.getParameter("action");

if ("generar".equals(action)) {
// El dueño llena/corrige el formulario: desde Pendiente o Rechazado
if (esDueno && ("Pendiente".equalsIgnoreCase(estado) || "Rechazado".equalsIgnoreCase(estado))) {
generarReporte(request, response, reporte);
return;
}
} else if ("enviar".equals(action)) {
// Enviar exige formulario generado y el PDF firmado ya subido (RN-02)
boolean tieneResultados = reporte.getResultados() != null && !reporte.getResultados().isBlank();
if (esDueno && "Pendiente".equalsIgnoreCase(estado) && tieneResultados
&& documentoDao.existeTipoEnReporte(idReporte, TIPO_REPORTE_FIRMADO)) {
boolean ok = reporteDao.enviar(idReporte);
response.sendRedirect("reporte?id=" + idReporte + (ok ? "&enviado=1" : "&error=guardar"));
return;
}
} else if ("aprobar".equals(action) || "rechazar".equals(action)) {
// Solo Estadías/Admin evalúa, y solo un reporte enviado (Completado)
String motivo = request.getParameter("motivo");
boolean esRechazo = "rechazar".equals(action);
if (!esDocente && "Completado".equalsIgnoreCase(estado)
&& (!esRechazo || (motivo != null && !motivo.isBlank()))) {

String nuevoEstado = esRechazo ? "Rechazado" : "Aprobado";
boolean ok = reporteDao.decidir(idReporte, nuevoEstado,
motivo != null ? motivo.trim() : null);
if (ok) {
notificarDecisionReporte(reporte, nuevoEstado, motivo);
}
response.sendRedirect("reporte?id=" + idReporte);
return;
}
}

response.sendRedirect("reporte?id=" + idReporte);
}

/**
 * action=generar: guarda resultados/observaciones, aplica altas/bajas de
 * imágenes (deben quedar exactamente 3) y borra el PDF firmado anterior,
 * que queda obsoleto porque el formato se regenera con los datos nuevos.
 */
private void generarReporte(HttpServletRequest request, HttpServletResponse response,
Reporte reporte) throws ServletException, IOException {
int idReporte = reporte.getIdReporte();

String resultados = request.getParameter("resultados");
if (resultados == null || resultados.isBlank()) {
response.sendRedirect("reporte?id=" + idReporte + "&error=vacio");
return;
}
String observaciones = request.getParameter("observaciones");

// Imágenes nuevas: varias partes con el mismo name="imagenes"
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

for (Part p : partesImagen) {
String error = validarImagen(p);
if (error != null) {
response.sendRedirect("reporte?id=" + idReporte + "&error=" + error);
return;
}
}

// Imágenes existentes que el docente quitó en el formulario
List<Integer> aEliminar = new ArrayList<>();
String[] idsEliminar = request.getParameterValues("eliminarImagen");
if (idsEliminar != null) {
for (String idParam : idsEliminar) {
try {
aEliminar.add(Integer.parseInt(idParam));
} catch (NumberFormatException ignored) {
}
}
}

// Al final deben quedar exactamente 3 imágenes (RN-07)
int existentes = imagenReporteDao.contarPorReporte(idReporte);
int finales = existentes - aEliminar.size() + partesImagen.size();
if (finales > IMAGENES_REQUERIDAS) {
response.sendRedirect("reporte?id=" + idReporte + "&error=maximo");
return;
}
if (finales < IMAGENES_REQUERIDAS) {
response.sendRedirect("reporte?id=" + idReporte + "&error=minimo");
return;
}

for (Integer idImagen : aEliminar) {
imagenReporteDao.eliminar(idImagen, idReporte);
}

for (Part p : partesImagen) {
byte[] contenido;
try (InputStream in = p.getInputStream()) {
contenido = in.readAllBytes();
}
// Si el INSERT falla no seguimos en silencio: el docente debe saberlo
if (!imagenReporteDao.guardar(idReporte, Base64.getEncoder().encodeToString(contenido))) {
response.sendRedirect("reporte?id=" + idReporte + "&error=imagen");
return;
}
}

boolean ok = reporteDao.guardarFormulario(idReporte, resultados.trim(),
observaciones != null ? observaciones.trim() : null);

// El firmado anterior queda obsoleto: hay que volver a firmar el formato
documentoDao.eliminarTipoDeReporte(idReporte, TIPO_REPORTE_FIRMADO);

// Patrón PRG: cae en la sub-fase "firmar"
response.sendRedirect("reporte?id=" + idReporte + (ok ? "&generado=1" : "&error=guardar"));
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
// La tabla IMAGEN no guarda el tipo MIME: se detecta con los magic
// bytes (solo se aceptan JPG y PNG al subir, ver validarImagen)
response.setContentType(detectarTipoMime(contenido));
response.setContentLengthLong(contenido.length);
try (OutputStream out = response.getOutputStream()) {
out.write(contenido);
}
}

/** PNG empieza con 0x89 'P' 'N' 'G'; cualquier otro caso aquí es JPG. */
private String detectarTipoMime(byte[] contenido) {
boolean esPng = contenido.length > 3 && (contenido[0] & 0xFF) == 0x89
&& contenido[1] == 'P' && contenido[2] == 'N' && contenido[3] == 'G';
return esPng ? "image/png" : "image/jpeg";
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

/** Correo automático al docente cuando su reporte es aprobado o rechazado. */
private void notificarDecisionReporte(Reporte reporte, String nuevoEstado, String motivo) {
if (reporte.getCorreoSolicitante() == null) {
return;
}
boolean aprobado = "Aprobado".equals(nuevoEstado);
String plantillaHtml = """
<html>
    <body style="font-family: Arial, sans-serif; color: #333333;">
        <h2 style="color: #183052;">Tu reporte de visita fue {0}</h2>
        <p>El reporte de la visita a <strong>{1}</strong> fue <strong>{0}</strong> por el área de Estadías.</p>
        {2}
        <p>{3}</p>
        <p style="font-size: 12px; color: #777777;">Sistema de Gestión de Visitas Académicas - UTEZ</p>
    </body>
</html>
""";
String bloqueMotivo = (motivo != null && !motivo.isBlank())
? "<p><strong>Motivo:</strong> " + motivo + "</p>"
: "";
String siguientePaso = aprobado
? "Con esto el proceso de la visita queda cerrado. Puedes consultar el reporte desde el Histórico."
: "Entra al reporte en el sistema, corrígelo con \"Editar formulario\" y vuelve a enviarlo.";
String cuerpo = MessageFormat.format(plantillaHtml,
aprobado ? "aprobado" : "rechazado",
reporte.getNombreEmpresaActividad(), bloqueMotivo, siguientePaso);
try {
EmailSender.sendMail(reporte.getCorreoSolicitante(),
"Reporte de visita " + (aprobado ? "aprobado" : "rechazado") + " - Visitas Académicas",
cuerpo);
} catch (RuntimeException e) {
// La decisión ya quedó guardada; si falla el correo no bloqueamos el flujo
System.err.println("No se pudo enviar la notificación: " + e.getMessage());
}
}
}
