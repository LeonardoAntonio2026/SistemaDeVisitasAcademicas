package com.example.demo.controller;

import com.example.demo.model.AsignaturaReforzar;
import com.example.demo.model.CatalogoAcademico;
import com.example.demo.model.ProgramaEducativo;
import com.example.demo.model.Solicitud;
import com.example.demo.model.Usuario;
import com.example.demo.model.dao.DesgloseDao;
import com.example.demo.model.dao.DocumentoDao;
import com.example.demo.model.dao.SolicitudDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.SesionUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Desglose académico de una solicitud: sus grupos, sus asignaturas a reforzar y
 * sus docentes acompañantes.
 *
 * <p><b>Qué problema resuelve.</b> Hasta ahora el desglose solo se podía tocar
 * volviendo a abrir el formulario completo de la solicitud: corregir el número
 * de estudiantes de un grupo obligaba a reenviar los quince campos del lugar,
 * los participantes y todo el desglose, y por dentro se borraban todos los
 * hijos para reinsertarlos. Aquí cada renglón se agrega, se corrige y se quita
 * por su cuenta, sin tocar el resto de la solicitud.</p>
 *
 * <p><b>Cómo se comunica.</b> Nada de campos de formulario: el navegador manda
 * un mensaje JSON por POST y recibe otro mensaje JSON de vuelta. Un solo
 * endpoint atiende las tres entidades; lo que cambia es el contenido del
 * mensaje.</p>
 *
 * <pre>
 * Mensaje que entra:
 *   { "accion": "listar|crear|actualizar|eliminar",
 *     "entidad": "grupo|asignatura|docente",
 *     "idSolicitud": 7,
 *     "datos": { ... lo que pida esa entidad ... } }
 *
 * Mensaje que sale:
 *   { "ok": true,
 *     "mensaje": "Grupo agregado.",
 *     "foInvalidado": false,
 *     "desglose": { "grupos": [...], "asignaturas": [...], "docentes": [...],
 *                   "totalEstudiantes": 80, "divisiones": [...] } }
 * </pre>
 *
 * <p><b>Por qué toda respuesta trae el desglose completo.</b> Sale más barato
 * en errores que mandar solo el renglón que cambió: el total de estudiantes y
 * el resumen por división dependen de TODOS los grupos, así que si el
 * navegador fuera armando el estado a pedazos, cualquier operación perdida lo
 * dejaría mostrando totales que no cuadran con la base. Con el desglose
 * completo, la vista se vuelve a pintar entera y siempre coincide con lo que
 * hay guardado.</p>
 *
 * <p><b>Permisos.</b> El id de la solicitud viaja en el mensaje y el navegador
 * lo puede cambiar, así que en CADA mensaje se vuelve a cargar la solicitud de
 * la base y se revisa que sea del usuario en sesión (o que sea el
 * Administrador) y que siga en Pendiente. Solo se edita el desglose de una
 * solicitud que todavía no se manda a Estadías: una vez enviada, el trámite ya
 * va sobre datos que alguien más está revisando.</p>
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 25/08/2026
 */
@WebServlet(name = "DesgloseServlet", value = "/desglose")
public class DesgloseServlet extends HttpServlet {

    /** Tope por grupo, el mismo que aplica el formulario completo. */
    private static final int MAX_ESTUDIANTES = 999;

    /** La UTEZ maneja hasta 11 cuatrimestres entre TSU e Ingeniería. */
    private static final int MAX_CUATRIMESTRE = 11;

    /** Largo de la columna NOMBRE de ASIGNATURA_REFORZAR_SOLICITUD. */
    private static final int MAX_ASIGNATURA = 100;

    private final DesgloseDao desgloseDao = new DesgloseDao();
    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final DocumentoDao documentoDao = new DocumentoDao();
    private final Gson gson = new Gson();

    // ==================== Entrada de la página ====================

    /**
     * Pinta la pantalla una sola vez. No manda ni un grupo en el HTML: la
     * página llega vacía y lo primero que hace desglose.js es pedir el
     * desglose con un mensaje JSON, igual que hace después de cada cambio.
     *
     * @param request  petición con el parámetro {@code id} de la solicitud
     * @param response respuesta donde se escribe el JSP
     * @throws ServletException si falla el forward
     * @throws IOException      si falla la escritura de la respuesta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Solicitud solicitud = cargarEditable(request, parseId(request.getParameter("id")));
        if (solicitud == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setAttribute("solicitud", solicitud);
        // Los catálogos sí van en el HTML: son fijos, no cambian con las
        // operaciones y así los <select> ya están armados desde la primera carga.
        // Son los mismos tres que arma SolicitudDocente.jsp, porque el desglose
        // se captura aquí igual que allá.
        request.setAttribute("divisiones", CatalogoAcademico.DIVISIONES);
        request.setAttribute("programasPorDivision", CatalogoAcademico.getProgramas());
        request.setAttribute("nombresDivision", CatalogoAcademico.getNombres());

        request.getRequestDispatcher("desglose.jsp").forward(request, response);
    }

    // ==================== Paso de mensajes JSON ====================

    /**
     * Único punto de entrada de las operaciones. Lee el mensaje JSON, revisa
     * permisos contra la base y responde con otro mensaje JSON.
     *
     * @param request  petición con el mensaje JSON en el cuerpo
     * @param response respuesta donde se escribe el mensaje de vuelta
     * @throws ServletException si falla el procesamiento
     * @throws IOException      si falla la escritura de la respuesta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Mensaje mensaje = leerMensaje(request);
        if (mensaje == null) {
            responder(response, Respuesta.error("No se entendió el mensaje enviado. Recarga la página."));
            return;
        }

        // El id viene del navegador: se vuelve a resolver contra la base en cada
        // mensaje, nunca se confía en que sea el de la página que se abrió
        Solicitud solicitud = cargarEditable(request, mensaje.idSolicitud());
        if (solicitud == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int id = solicitud.getIdSolicitud();
        Datos datos = mensaje.datosSeguros();
        String accion = mensaje.accionSegura();

        Respuesta respuesta = switch (accion) {
            case "listar" -> Respuesta.exito(null);
            case "crear" -> crear(mensaje.entidadSegura(), id, datos);
            case "actualizar" -> actualizar(mensaje.entidadSegura(), id, datos);
            case "eliminar" -> eliminar(mensaje.entidadSegura(), id, datos);
            default -> Respuesta.error("Operación no reconocida. Recarga la página.");
        };

        // Cambiar el desglose cambia el FO-UTEZ-EST-08 que se imprime, así que
        // el que ya estaba firmado y subido deja de servir: se quita para que el
        // docente descargue el formato nuevo y lo vuelva a firmar. Misma regla
        // que aplica SolicitudServlet al guardar el formulario completo.
        boolean foInvalidado = respuesta.ok() && !"listar".equals(accion)
                && documentoDao.eliminarTipoDeSolicitud(id, DetalleSolicitudServlet.TIPO_FO_FIRMADO);

        // El desglose se vuelve a leer YA con el cambio aplicado: es lo que
        // repinta la vista, y así no puede quedar desfasada de la base
        responder(response, new Respuesta(respuesta.ok(), respuesta.mensaje(), foInvalidado,
                respuesta.ok() ? leerDesglose(solicitud) : null));
    }

    // ==================== Registrar ====================

    private Respuesta crear(String entidad, int idSolicitud, Datos datos) {
        return switch (entidad) {
            case "grupo" -> crearGrupo(idSolicitud, datos);
            case "asignatura" -> crearAsignatura(idSolicitud, datos);
            case "docente" -> agregarDocente(idSolicitud, datos);
            default -> Respuesta.error("Operación no reconocida. Recarga la página.");
        };
    }

    private Respuesta crearGrupo(int idSolicitud, Datos datos) {
        String programa = limpiar(datos.programa());
        String grupo = limpiar(datos.grupo());
        int cuatrimestre = valor(datos.cuatrimestre());
        int estudiantes = valor(datos.noEstudiantes());

        String error = validarGrupo(idSolicitud, programa, cuatrimestre, grupo, estudiantes, null);
        if (error != null) {
            return Respuesta.error(error);
        }

        ProgramaEducativo nuevo = new ProgramaEducativo();
        nuevo.setIdSolicitud(idSolicitud);
        nuevo.setPrograma(programa);
        nuevo.setCuatrimestre(cuatrimestre);
        nuevo.setGrupo(grupo);
        nuevo.setNoEstudiantes(estudiantes);

        if (!desgloseDao.crearGrupo(nuevo)) {
            return Respuesta.error("No se pudo agregar el grupo. Inténtalo de nuevo.");
        }
        return Respuesta.exito("Grupo agregado al desglose.");
    }

    private Respuesta crearAsignatura(int idSolicitud, Datos datos) {
        String nombre = limpiar(datos.nombre());

        String error = validarAsignatura(idSolicitud, nombre, null);
        if (error != null) {
            return Respuesta.error(error);
        }

        AsignaturaReforzar nueva = new AsignaturaReforzar(idSolicitud, nombre);
        if (!desgloseDao.crearAsignatura(nueva)) {
            return Respuesta.error("No se pudo agregar la asignatura. Inténtalo de nuevo.");
        }
        return Respuesta.exito("Asignatura agregada.");
    }

    private Respuesta agregarDocente(int idSolicitud, Datos datos) {
        Integer idUsuario = datos.idUsuario();

        String error = validarDocente(idSolicitud, idUsuario);
        if (error != null) {
            return Respuesta.error(error);
        }

        if (!desgloseDao.agregarDocente(idSolicitud, idUsuario)) {
            return Respuesta.error("No se pudo agregar al docente. Inténtalo de nuevo.");
        }
        return Respuesta.exito("Docente acompañante agregado.");
    }

    // ==================== Actualizar ====================

    private Respuesta actualizar(String entidad, int idSolicitud, Datos datos) {
        return switch (entidad) {
            case "grupo" -> actualizarGrupo(idSolicitud, datos);
            case "asignatura" -> actualizarAsignatura(idSolicitud, datos);
            case "docente" -> cambiarDocente(idSolicitud, datos);
            default -> Respuesta.error("Operación no reconocida. Recarga la página.");
        };
    }

    private Respuesta actualizarGrupo(int idSolicitud, Datos datos) {
        Integer id = datos.id();
        if (id == null || desgloseDao.getGrupo(idSolicitud, id) == null) {
            return Respuesta.error("Ese grupo ya no está en el desglose.");
        }

        String programa = limpiar(datos.programa());
        String grupo = limpiar(datos.grupo());
        int cuatrimestre = valor(datos.cuatrimestre());
        int estudiantes = valor(datos.noEstudiantes());

        String error = validarGrupo(idSolicitud, programa, cuatrimestre, grupo, estudiantes, id);
        if (error != null) {
            return Respuesta.error(error);
        }

        ProgramaEducativo editado = new ProgramaEducativo();
        editado.setIdPrograma(id);
        editado.setIdSolicitud(idSolicitud);
        editado.setPrograma(programa);
        editado.setCuatrimestre(cuatrimestre);
        editado.setGrupo(grupo);
        editado.setNoEstudiantes(estudiantes);

        if (!desgloseDao.actualizarGrupo(editado)) {
            return Respuesta.error("No se pudieron guardar los cambios del grupo. Inténtalo de nuevo.");
        }
        return Respuesta.exito("Grupo actualizado.");
    }

    private Respuesta actualizarAsignatura(int idSolicitud, Datos datos) {
        Integer id = datos.id();
        if (id == null) {
            return Respuesta.error("Esa asignatura ya no está en el desglose.");
        }

        String nombre = limpiar(datos.nombre());
        String error = validarAsignatura(idSolicitud, nombre, id);
        if (error != null) {
            return Respuesta.error(error);
        }

        AsignaturaReforzar editada = new AsignaturaReforzar(idSolicitud, nombre);
        editada.setIdAsignatura(id);

        if (!desgloseDao.actualizarAsignatura(editada)) {
            return Respuesta.error("Esa asignatura ya no está en el desglose.");
        }
        return Respuesta.exito("Asignatura actualizada.");
    }

    private Respuesta cambiarDocente(int idSolicitud, Datos datos) {
        Integer anterior = datos.id();
        Integer nuevo = datos.idUsuario();

        if (anterior == null || !desgloseDao.existeDocente(idSolicitud, anterior)) {
            return Respuesta.error("Ese docente ya no está en la lista de acompañantes.");
        }
        if (nuevo != null && nuevo.equals(anterior)) {
            return Respuesta.error("Ese docente ya es el acompañante de este renglón.");
        }
        String error = validarDocente(idSolicitud, nuevo);
        if (error != null) {
            return Respuesta.error(error);
        }

        if (!desgloseDao.cambiarDocente(idSolicitud, anterior, nuevo)) {
            return Respuesta.error("No se pudo cambiar al docente. Inténtalo de nuevo.");
        }
        return Respuesta.exito("Docente acompañante actualizado.");
    }

    // ==================== Eliminar ====================

    private Respuesta eliminar(String entidad, int idSolicitud, Datos datos) {
        return switch (entidad) {
            case "grupo" -> eliminarGrupo(idSolicitud, datos);
            case "asignatura" -> eliminarAsignatura(idSolicitud, datos);
            case "docente" -> quitarDocente(idSolicitud, datos);
            default -> Respuesta.error("Operación no reconocida. Recarga la página.");
        };
    }

    private Respuesta eliminarGrupo(int idSolicitud, Datos datos) {
        Integer id = datos.id();
        if (id == null) {
            return Respuesta.error("Ese grupo ya no está en el desglose.");
        }
        // Una solicitud sin grupos no tiene a quién llevar de visita: el
        // formulario oficial exige por lo menos uno. Se bloquea aquí y no al
        // enviar la solicitud, para que el docente se entere en el momento
        if (desgloseDao.getGrupos(idSolicitud).size() <= 1) {
            return Respuesta.error("La solicitud debe llevar al menos un grupo. "
                    + "Agrega el que va a sustituirlo antes de quitar este.");
        }
        if (!desgloseDao.eliminarGrupo(idSolicitud, id)) {
            return Respuesta.error("Ese grupo ya no está en el desglose.");
        }
        return Respuesta.exito("Grupo eliminado del desglose.");
    }

    private Respuesta eliminarAsignatura(int idSolicitud, Datos datos) {
        Integer id = datos.id();
        if (id == null) {
            return Respuesta.error("Esa asignatura ya no está en el desglose.");
        }
        if (desgloseDao.getAsignaturas(idSolicitud).size() <= 1) {
            return Respuesta.error("La solicitud debe llevar al menos una asignatura a reforzar. "
                    + "Agrega la que va a sustituirla antes de quitar esta.");
        }
        if (!desgloseDao.eliminarAsignatura(idSolicitud, id)) {
            return Respuesta.error("Esa asignatura ya no está en el desglose.");
        }
        return Respuesta.exito("Asignatura eliminada.");
    }

    private Respuesta quitarDocente(int idSolicitud, Datos datos) {
        Integer idUsuario = datos.idUsuario() != null ? datos.idUsuario() : datos.id();
        if (idUsuario == null || !desgloseDao.quitarDocente(idSolicitud, idUsuario)) {
            return Respuesta.error("Ese docente ya no está en la lista de acompañantes.");
        }
        // Los acompañantes son opcionales: quedarse sin ninguno es válido, la
        // visita la puede hacer el docente responsable solo
        return Respuesta.exito("Docente acompañante quitado.");
    }

    // ==================== Reglas de negocio ====================

    /**
     * Revisa un grupo con las mismas reglas que aplica el formulario completo.
     *
     * @param idSolicitud  solicitud a la que pertenece
     * @param programa     programa educativo elegido
     * @param cuatrimestre cuatrimestre capturado
     * @param grupo        letra del grupo
     * @param estudiantes  número de estudiantes
     * @param idExcluir    id del grupo que se edita, o {@code null} si es nuevo
     * @return el mensaje del primer problema encontrado, o {@code null} si todo está bien
     */
    private String validarGrupo(int idSolicitud, String programa, int cuatrimestre,
                                String grupo, int estudiantes, Integer idExcluir) {
        if (programa.isEmpty()) {
            return "Elige el programa educativo del grupo.";
        }
        // El catálogo es la fuente de verdad del formato oficial: un programa
        // escrito a mano imprimiría una división en blanco en el FO-UTEZ-EST-08
        if (!CatalogoAcademico.existePrograma(programa)) {
            return "El programa educativo seleccionado no está en el catálogo de la UTEZ.";
        }
        if (cuatrimestre < 1 || cuatrimestre > MAX_CUATRIMESTRE) {
            return "El cuatrimestre debe estar entre 1 y " + MAX_CUATRIMESTRE + ".";
        }
        if (grupo.isEmpty()) {
            return "Elige el grupo.";
        }
        if (estudiantes < 1) {
            return "El grupo debe llevar al menos 1 estudiante.";
        }
        if (estudiantes > MAX_ESTUDIANTES) {
            return "El grupo no puede pasar de " + MAX_ESTUDIANTES + " estudiantes.";
        }
        if (desgloseDao.existeGrupo(idSolicitud, programa, cuatrimestre, grupo, idExcluir)) {
            return "El grupo " + cuatrimestre + "-" + grupo + " de ese programa ya está en el desglose.";
        }
        return null;
    }

    /**
     * Revisa una asignatura a reforzar.
     *
     * @param idSolicitud solicitud a la que pertenece
     * @param nombre      nombre capturado
     * @param idExcluir   id de la asignatura que se edita, o {@code null} si es nueva
     * @return el mensaje del problema encontrado, o {@code null} si todo está bien
     */
    private String validarAsignatura(int idSolicitud, String nombre, Integer idExcluir) {
        if (nombre.isEmpty()) {
            return "Escribe el nombre de la asignatura.";
        }
        if (nombre.length() > MAX_ASIGNATURA) {
            return "El nombre de la asignatura no debe pasar de " + MAX_ASIGNATURA + " caracteres.";
        }
        if (desgloseDao.existeAsignatura(idSolicitud, nombre, idExcluir)) {
            return "Esa asignatura ya está en la lista.";
        }
        return null;
    }

    /**
     * Revisa que el docente exista y pueda acompañar esta visita.
     *
     * @param idSolicitud solicitud a la que se quiere ligar
     * @param idUsuario   docente elegido en el autocompletado
     * @return el mensaje del problema encontrado, o {@code null} si todo está bien
     */
    private String validarDocente(int idSolicitud, Integer idUsuario) {
        if (idUsuario == null) {
            return "Elige un docente de la lista de sugerencias.";
        }
        Usuario docente = usuarioDao.getById(idUsuario);
        if (docente == null) {
            return "Ese docente ya no existe en el sistema.";
        }
        // El solicitante ya va por definición: si además se agregara como
        // acompañante, saldría dos veces en el formato oficial
        Solicitud solicitud = solicitudDao.getById(idSolicitud);
        if (solicitud != null && solicitud.getIdUsuarioSolicitante() == idUsuario.intValue()) {
            return "Quien levantó la solicitud ya va en la visita, no se agrega como acompañante.";
        }
        if (desgloseDao.existeDocente(idSolicitud, idUsuario)) {
            return "Ese docente ya está en la lista de acompañantes.";
        }
        return null;
    }

    // ==================== Armado del mensaje de salida ====================

    /**
     * Lee de la base las tres listas y saca los totales que la vista muestra.
     *
     * @param solicitud solicitud dueña del desglose
     * @return el desglose completo, tal como se serializa en el mensaje
     */
    private Desglose leerDesglose(Solicitud solicitud) {
        int id = solicitud.getIdSolicitud();

        List<GrupoJson> grupos = new ArrayList<>();
        int totalEstudiantes = 0;
        // LinkedHashMap: el resumen sale en el orden en que se capturaron los
        // grupos, que es el mismo en el que se ven en la tabla de arriba
        Map<String, Integer> porDivision = new LinkedHashMap<>();

        for (ProgramaEducativo p : desgloseDao.getGrupos(id)) {
            grupos.add(new GrupoJson(p.getIdPrograma(), p.getPrograma(), p.getDivisionMostrable(),
                    p.getCuatrimestre(), p.getGrupo(), p.getNoEstudiantes()));
            totalEstudiantes += p.getNoEstudiantes();
            porDivision.merge(p.getDivisionMostrable(), p.getNoEstudiantes(), Integer::sum);
        }

        List<DivisionJson> divisiones = new ArrayList<>();
        for (Map.Entry<String, Integer> division : porDivision.entrySet()) {
            divisiones.add(new DivisionJson(division.getKey(),
                    CatalogoAcademico.nombreDe(division.getKey()), division.getValue()));
        }

        List<AsignaturaJson> asignaturas = new ArrayList<>();
        for (AsignaturaReforzar a : desgloseDao.getAsignaturas(id)) {
            asignaturas.add(new AsignaturaJson(a.getIdAsignatura(), a.getNombre()));
        }

        List<DocenteJson> docentes = new ArrayList<>();
        for (Usuario u : desgloseDao.getDocentes(id)) {
            docentes.add(new DocenteJson(u.getId(), u.getNombre(), u.getCorreo()));
        }

        return new Desglose(id, grupos, asignaturas, docentes, totalEstudiantes, divisiones);
    }

    // ==================== Utilidades ====================

    /**
     * Carga la solicitud del mensaje y decide si el usuario en sesión puede
     * tocar su desglose.
     *
     * @param request petición en curso, de donde sale la sesión
     * @param id      id de la solicitud, o {@code null} si no venía
     * @return la solicitud si se puede editar, o {@code null} en cualquier otro caso
     */
    private Solicitud cargarEditable(HttpServletRequest request, Integer id) {
        Integer idUsuario = SesionUtils.idUsuario(request);
        if (idUsuario == null || id == null) {
            return null;
        }

        Solicitud solicitud = solicitudDao.getById(id);
        if (solicitud == null) {
            return null;
        }
        // El dueño la corrige; el Administrador también, porque le toca
        // corregir las de los demás
        boolean puedeTocarla = solicitud.getIdUsuarioSolicitante() == idUsuario.intValue()
                || SesionUtils.esAdministrador(request);
        if (!puedeTocarla) {
            return null;
        }
        // Solo antes de mandarla a Estadías: ya enviada, el desglose es lo que
        // el revisor está evaluando y no puede cambiar bajo sus pies
        return "Pendiente".equalsIgnoreCase(solicitud.getNombreEstado()) ? solicitud : null;
    }

    /**
     * Convierte el cuerpo de la petición en un mensaje.
     *
     * @param request petición con el JSON en el cuerpo
     * @return el mensaje leído, o {@code null} si el cuerpo no era JSON válido
     */
    private Mensaje leerMensaje(HttpServletRequest request) {
        try (BufferedReader entrada = request.getReader()) {
            return gson.fromJson(entrada, Mensaje.class);
        } catch (IOException | JsonParseException e) {
            return null;
        }
    }

    /**
     * Escribe el mensaje de respuesta.
     *
     * @param response  respuesta HTTP en curso
     * @param respuesta mensaje a serializar
     * @throws IOException si falla la escritura
     */
    private void responder(HttpServletResponse response, Respuesta respuesta) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            gson.toJson(respuesta, out);
        }
    }

    private Integer parseId(String valor) {
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private String limpiar(String valor) {
        return (valor == null) ? "" : valor.trim();
    }

    private int valor(Integer numero) {
        return (numero == null) ? 0 : numero;
    }

    // ==================== Forma de los mensajes ====================

    /**
     * Mensaje que llega del navegador. Gson deja en null lo que no venga en el
     * JSON, así que cada campo se lee con su método "seguro" y un mensaje
     * incompleto termina en "operación no reconocida" en vez de en un
     * NullPointerException.
     */
    private record Mensaje(String accion, String entidad, Integer idSolicitud, Datos datos) {
        String accionSegura() {
            return accion != null ? accion : "";
        }

        String entidadSegura() {
            return entidad != null ? entidad : "";
        }

        Datos datosSeguros() {
            return datos != null ? datos : Datos.VACIOS;
        }
    }

    /**
     * Contenido del mensaje. Es uno solo para las tres entidades: cada
     * operación lee los campos que le tocan y deja los demás en null. Separarlo
     * en tres tipos obligaría a mirar la entidad antes de deserializar, para
     * ahorrarse unos campos vacíos que Gson ni siquiera escribe.
     */
    private record Datos(Integer id, String programa, Integer cuatrimestre, String grupo,
                         Integer noEstudiantes, String nombre, Integer idUsuario) {
        static final Datos VACIOS = new Datos(null, null, null, null, null, null, null);
    }

    /**
     * Mensaje que se devuelve. Gson omite los campos nulos: cuando algo sale
     * mal viaja solo el motivo, sin desglose.
     */
    private record Respuesta(boolean ok, String mensaje, boolean foInvalidado, Desglose desglose) {
        static Respuesta error(String mensaje) {
            return new Respuesta(false, mensaje, false, null);
        }

        /** Éxito de una operación de escritura; el desglose se agrega después. */
        static Respuesta exito(String mensaje) {
            return new Respuesta(true, mensaje, false, null);
        }
    }

    /** Las tres listas relacionadas más los totales que se calculan con ellas. */
    private record Desglose(int idSolicitud, List<GrupoJson> grupos, List<AsignaturaJson> asignaturas,
                            List<DocenteJson> docentes, int totalEstudiantes,
                            List<DivisionJson> divisiones) {}

    private record GrupoJson(int id, String programa, String division, int cuatrimestre,
                             String grupo, int noEstudiantes) {}

    private record AsignaturaJson(int id, String nombre) {}

    /** Del docente sale lo que la lista pinta; nada de su rol ni de su acceso. */
    private record DocenteJson(int id, String nombre, String correo) {}

    private record DivisionJson(String sigla, String nombre, int estudiantes) {}
}
