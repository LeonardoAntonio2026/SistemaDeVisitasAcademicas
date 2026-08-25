package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.CatalogoAcademico;
import com.example.demo.model.ProgramaEducativo;
import com.example.demo.model.Solicitud;
import com.example.demo.model.Usuario;
import com.example.demo.model.dao.DocumentoDao;
import com.example.demo.model.dao.SolicitudDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.SesionUtils;
import com.example.demo.utils.Validador;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Servlet controlador encargado de gestionar el ciclo de las visitas academicas.
 * <p>
 *     Atiende las operaciones HTTP, Get y Post para el registro, eliminación, actualización, edición y consulta
 *     de solicitudes creadas por cualquier usuario para su creación o revisión (Docente, Estadías y Admin)
 * </p>
 * @author Eder Gabriel García Vázquez
 * @since 17/08/2026
 */

@WebServlet(name = "SolicitudServlet", value = "/solicitud")
public class SolicitudServlet extends HttpServlet {

    /** Tope por celda para atajar capturas absurdas (un grupo no llega a 999 alumnos). */
    private static final int MAX_ESTUDIANTES = 999;

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final DocumentoDao documentoDao = new DocumentoDao();
    private final UsuarioDao usuarioDao = new UsuarioDao();


    /**
     * Procesa las peticiones Get {@code GET}
     * <p>
     *     Maneja la navegación hacia el formulario de creación, precarga los datos para edición
     *     y el renderizado de la lista de solicitudes activas según el rol
     * </p>
     * @param request objeto {@link HttpServletRequest} que contiene la petición del cliente.
     * @param response objeto {@link HttpServletResponse} para enviar la respuesta al cliente
     * @throws ServletException Si ocurre un error del procesamiento del Servlet
     * @throws IOException Si sucede un error de entrada/salida
     * @author Eder Gabriel García Vázquez
     * @since 17/08/2026
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("nueva".equals(request.getParameter("action"))) {
            request.setAttribute("editando", false);
            request.getRequestDispatcher("SolicitudDocente.jsp").forward(request, response);
            return;
        }

        // Editar: el mismo formulario de nueva solicitud pero precargado.
        // El dueño (o el Administrador, que corrige las de los demás), y solo
        // si está Pendiente (aún no se envía) o Rechazada (corregirla la reabre)
        if ("editar".equals(request.getParameter("action"))) {
            Solicitud solicitud = cargarEditable(request);
            if (solicitud == null) {
                // O no es suya, o está en un estado que ya no se edita
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.setAttribute("solicitud", solicitud);
            request.setAttribute("editando", true);
            request.getRequestDispatcher("SolicitudDocente.jsp").forward(request, response);
            return;
        }

        // Lista de solicitudes: el docente ve las suyas, Estadías las de todos
        // y el Administrador las de todos MÁS las suyas (también solicita)
        Integer idUsuario = SesionUtils.idUsuario(request);

        // Mismas consultas que el inicio (IndexSv): solo las ACTIVAS. Antes se
        // traían todas y la vista escondía las terminadas, así que un docente
        // con puras solicitudes rechazadas veía la página vacía sin el mensaje
        // de "No tienes ninguna solicitud".
        List<Solicitud> solicitudes;
        if (idUsuario == null) {
            solicitudes = new ArrayList<>();
        } else if (SesionUtils.esRevisor(request)) {
            solicitudes = solicitudDao.getActivasParaRevision(
                    SesionUtils.puedeSolicitar(request) ? idUsuario : null);
        } else {
            solicitudes = solicitudDao.getActivasBySolicitante(idUsuario);
        }
        request.setAttribute("listaSolicitudes", solicitudes);

        request.getRequestDispatcher("solicitudes.jsp").forward(request, response);
    }

    /**
     * Procesa las peticiones HTTP {@code POST}
     * <p>
     *  Procesa las acciones de escritura sobre las solicitudes
     * {@code delete} Elimina solicitudes en estado pendiente
     * {@code create} Registra una nueva solicitud y redirige al detalle para firmas
     * {@code update} Actualiza una solicitud existente e invalida formatos previos si estaba rechazada
     * </p>
     * @param request objeto {@link HttpServletRequest} con la información del formulario
     * @param response objeto {@link HttpServletResponse} para la respuesta o redirección
     * @throws ServletException Si ocurre un error en el procesamiento del servlet
     * @throws IOException Si ocurre un error de entrada/salida
     * @author Eder Gabriel García Vázquez
     * @since 17/08/2026
     */


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            // Solo el docente dueño y solo mientras siga Pendiente; una vez
            // enviada a Estadías ya no se elimina, ni siquiera si la rechazaron:
            // se corrige y se reenvía (RF-11)
            Solicitud aEliminar = cargarBorrablePorDueno(request);
            if (aEliminar != null) {
                solicitudDao.delete(aEliminar.getIdSolicitud());
            }
        } else if ("create".equals(action)) {
            Integer idUsuario = SesionUtils.idUsuario(request);
            if (idUsuario == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            Solicitud solicitud = new Solicitud();
            solicitud.setIdUsuarioSolicitante(idUsuario);
            llenarDesdeFormulario(solicitud, request);

            List<String> errores = validar(solicitud);
            if (!errores.isEmpty()) {
                regresarAlFormulario(request, response, solicitud, errores, false);
                return;
            }

            if (solicitudDao.create(solicitud)) {
                // Crear NO envía a Estadías: el docente cae en los detalles para
                // descargar el formato, firmarlo, subirlo y ahí dar ENVIAR (RN-02)
                response.sendRedirect("detalle?id=" + solicitud.getIdSolicitud());
                return;
            }
            // Falló el INSERT: se regresa al formulario con lo capturado en vez
            // de mandarlo al inicio sin decirle que su solicitud no se guardó
            errores.add("No se pudo guardar la solicitud en la base de datos. "
                    + "No se registró nada; revisa tu conexión e inténtalo de nuevo.");
            regresarAlFormulario(request, response, solicitud, errores, false);
            return;
        } else if ("update".equals(action)) {
            Solicitud solicitud = cargarEditable(request);
            if (solicitud == null) {
                // O no es suya, o está en un estado que ya no se edita
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            // Se guarda antes de sobrescribir: si venía Rechazada, el update la
            // reabre como Pendiente y el aviso de la recarga es otro
            boolean veniaRechazada = "Rechazada".equalsIgnoreCase(solicitud.getNombreEstado());

            llenarDesdeFormulario(solicitud, request);

            List<String> errores = validar(solicitud);
            if (!errores.isEmpty()) {
                regresarAlFormulario(request, response, solicitud, errores, true);
                return;
            }

            if (solicitudDao.update(solicitud)) {
                // El FO firmado que ya estaba subido queda obsoleto: el formato
                // se regenera con los datos nuevos y hay que firmarlo otra vez
                documentoDao.eliminarTipoDeSolicitud(solicitud.getIdSolicitud(),
                        DetalleSolicitudServlet.TIPO_FO_FIRMADO);
                response.sendRedirect("detalle?id=" + solicitud.getIdSolicitud()
                        + (veniaRechazada ? "&corregida=1" : "&actualizado=1"));
                return;
            }
            // Falló el UPDATE: se regresa al formulario con los cambios a la vista
            errores.add("No se pudieron guardar los cambios en la base de datos. "
                    + "La solicitud quedó como estaba; inténtalo de nuevo.");
            regresarAlFormulario(request, response, solicitud, errores, true);
            return;
        }

        // Patrón PRG: Redirigir al GET evita que al recargar la página se repita la operación
        response.sendRedirect("indexSv");
    }

    /**
     * Aplica las reglas de negocio e integridad de datos sobre solicitud
     * <p>
     *     Valida campos obligatorios, longitudes máximas de columnas VARCHAR en la BD,
     *     formato de contactos, vigencia de fechas, congruencia de programas educativos y
     *     duplicidades
     * </p>
     * @param s la {@link Solicitud} a validar
     * @return una lista de {@link String} con los mensajes de error encontrados, estará vacia si la validación es correcta.
     * @author Eder Gabriel García Vázquez
     * @since 17/08/2026
     */
    private List<String> validar(Solicitud s) {
        List<String> errores = new ArrayList<>();

        // ---------- Datos del lugar a visitar ----------
        exigirTexto(errores, s.getNombreEmpresaActividad(), "Nombre de la empresa o actividad", 150);
        exigirTexto(errores, s.getLugarDireccion(), "Lugar o dirección", 200);

        if (!Validador.telefonoValido(s.getTelefonoContacto())) {
            errores.add("El teléfono del contacto debe tener 10 dígitos.");
        }
        if (!Validador.correoValido(s.getCorreoContacto())) {
            errores.add("El correo electrónico del contacto no tiene un formato válido.");
        }

        LocalDate fechaInicio = Validador.fecha(s.getFechaInicio());
        if (fechaInicio == null) {
            errores.add("Elige la fecha de inicio de la visita.");
        } else if (fechaInicio.isBefore(LocalDate.now())) {
            errores.add("La fecha de la visita no puede ser anterior a hoy.");
        }

        exigirTexto(errores, s.getObjetivo(), "Objetivo de la visita", 500);

        // ---------- Participantes ----------
        exigirTexto(errores, s.getAreaSolicitante(), "Área solicitante", 100);
        exigirTexto(errores, s.getDocenteResponsable(), "Docente responsable de la visita", 150);

        if (!Validador.telefonoValido(s.getCelularResponsable())) {
            errores.add("El celular del docente responsable debe tener 10 dígitos.");
        }

        // ---------- Desglose por programa educativo ----------
        // El desglose por división ya no se captura: se calcula sumando estos
        // grupos, así que aquí es donde se valida todo lo de los estudiantes.
        if (s.getProgramas().isEmpty()) {
            errores.add("Agrega al menos un grupo en el desglose por programa educativo.");
        }
        // Un mismo programa + cuatrimestre + grupo no puede ir dos veces
        Set<String> combinaciones = new LinkedHashSet<>();
        for (ProgramaEducativo p : s.getProgramas()) {
            String programa = "\"" + p.getPrograma() + "\"";
            if (!CatalogoAcademico.existePrograma(p.getPrograma())) {
                errores.add("El programa educativo " + programa + " no está en el catálogo de la UTEZ. "
                        + "Selecciónalo de la lista.");
            }
            if (p.getCuatrimestre() < 1 || p.getCuatrimestre() > 11) {
                errores.add("El cuatrimestre del programa " + programa + " debe estar entre 1 y 11.");
            }
            if (Validador.vacio(p.getGrupo())) {
                errores.add("Elige el grupo del programa " + programa + ".");
            }
            if (p.getNoEstudiantes() < 1) {
                errores.add("El programa " + programa + " debe llevar al menos 1 estudiante.");
            } else if (p.getNoEstudiantes() > MAX_ESTUDIANTES) {
                errores.add("El programa " + programa + " no puede pasar de " + MAX_ESTUDIANTES + " estudiantes.");
            }
            if (!combinaciones.add(p.getPrograma() + "|" + p.getCuatrimestre() + "|" + p.getGrupo())) {
                errores.add("El grupo " + p.getCuatrimestre() + "-" + p.getGrupo() + " de " + programa
                        + " está capturado dos veces.");
            }
        }

        // ---------- Asignaturas ----------
        if (s.getAsignaturas().isEmpty()) {
            errores.add("Agrega al menos una asignatura que se reforzará con la visita.");
        }
        for (String asignatura : s.getAsignaturas()) {
            if (asignatura.length() > 100) {
                errores.add("El nombre de la asignatura \"" + asignatura.substring(0, 40)
                        + "…\" no debe pasar de 100 caracteres.");
            }
        }

        return errores;
    }

    /**
     *  Valida que un campo textual no sea nulo ni vacio y que no supere el limite permitido por la BD
     *  @param errores  lista donde se acumularán los mensajes de fallo encontrados.
     *  @param valor    texto capturado en el formulario.
     *  @param etiqueta nombre visible del campo para construir el mensaje de error.
     *  @param maxLargo longitud máxima permitida para la columna en la BD.
     * @author Eder Gabriel García Vázquez
     * @since 17/08/2026
     */

    private void exigirTexto(List<String> errores, String valor, String etiqueta, int maxLargo) {
        if (Validador.vacio(valor)) {
            errores.add("Completa el campo \"" + etiqueta + "\".");
        } else if (Validador.limpiar(valor).length() > maxLargo) {
            errores.add("El campo \"" + etiqueta + "\" no debe pasar de " + maxLargo + " caracteres.");
        }
    }

    /**
     * Redirige nuevamente al JSP del formulario conservando los datos capturados y los errores generados.
     * <p>
     *     Restaura además la información completa de los docentes acompañantes para mostrar de nuevo la lista.
     * </p>
     * @param request objeto de la petición
     * @param response objeto de la respuesta
     * @param solicitud objeto con los datos capturados que se desean mantener
     * @param errores lista de errores a desplegar en la vista
     * @param editando {@code true} si la vista proviene de una edición, {@code false} si es una nueva solicitud
     * @throws ServletException si ocurre un fallo al redirigir mediante {@code forward}
     * @throws IOException si ocurre un un fallo de entrada/salida
     * @author Eder Gabriel García Vázquez
     * @since 17/08/2026
     */
    private void regresarAlFormulario(HttpServletRequest request, HttpServletResponse response,
                                      Solicitud solicitud, List<String> errores, boolean editando)
            throws ServletException, IOException {
        // Los acompañantes llegaron solo como ids: se recuperan los nombres
        // para poder volver a pintar los chips del autocompletado
        List<Usuario> conNombre = new ArrayList<>();
        for (Usuario elegido : solicitud.getDocentesAcompanantes()) {
            Usuario completo = usuarioDao.getById(elegido.getId());
            conNombre.add(completo != null ? completo : elegido);
        }
        solicitud.setDocentesAcompanantes(conNombre);

        request.setAttribute("solicitud", solicitud);
        request.setAttribute("editando", editando);
        request.setAttribute("errores", errores);
        request.getRequestDispatcher("SolicitudDocente.jsp").forward(request, response);
    }

    /**
     * Mapea los parametros recibidos en el {@link HttpServletRequest} hacia las propiedas del objeto {@link Solicitud}
     * @param solicitud objeto de dominio a llenar
     * @param request peticion HTTP que contiene los datos del formulario
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private void llenarDesdeFormulario(Solicitud solicitud, HttpServletRequest request) {
        solicitud.setNombreEmpresaActividad(request.getParameter("nombreEmpresa"));
        solicitud.setLugarDireccion(request.getParameter("direccionLugar"));
        solicitud.setTelefonoContacto(request.getParameter("telefonoContacto"));
        solicitud.setCorreoContacto(request.getParameter("correoContacto"));
        solicitud.setFechaInicio(request.getParameter("fechaInicio"));
        solicitud.setObjetivo(request.getParameter("objetivoVisita"));
        solicitud.setAreaSolicitante(request.getParameter("areaSolicitante"));
        solicitud.setDocenteResponsable(request.getParameter("docenteResponsable"));
        solicitud.setCelularResponsable(request.getParameter("celularResponsable"));
        solicitud.setProgramas(leerProgramas(request));
        solicitud.setAsignaturas(leerAsignaturas(request));
        solicitud.setDocentesAcompanantes(leerAcompanantes(request));
        // El desglose por división ya no se captura: sale de los grupos de arriba
        solicitud.recalcularEstudiantesPorDivision();
    }

    /**
     * Parsea los idetnficadores de los docentes acompañantes enviados en la peticón
     * @param request peticion HTTP que contiene el arreglo de los identificadores
     * @return lista de objetos {@link Usuario} que representan a los docentes acompañantes, sin duplicidades.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private List<Usuario> leerAcompanantes(HttpServletRequest request) {
        List<Usuario> docentes = new ArrayList<>();
        String[] ids = request.getParameterValues("docentesAcompanantes");
        if (ids == null) {
            return docentes;
        }
        Set<Integer> vistos = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                continue;
            }
            try {
                vistos.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
                // Valor manipulado en el formulario: se ignora
            }
        }
        for (Integer id : vistos) {
            Usuario u = new Usuario();
            u.setId(id);
            docentes.add(u);
        }
        return docentes;
    }

    /**
     * Carga la solicitud identificada en los parámetros de la petición solo si pertenece al docente en sesión
     * y se encuentra en estado modificable (Pendiente o Rechazada)
     * @param request petición HTTP de la cual se obtiene el parametro id.
     * @return la {@link Solicitud} correspondiente si cumple las condiciones, o {@code null} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private Solicitud cargarEditable(HttpServletRequest request) {
        return cargarEnEstados(request, false, "Pendiente", "Rechazada");
    }

    /**
     * Carga una solicitud identificada en los parametros solo si pertenece al docente en sesión
     * y su estado es estrictamente "Pendiente"
     * @param request peticion HTTP con el identificador de la solicitud
     * @return la {@link Solicitud} a borrar, o {@code null} si no cumple la regla
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private Solicitud cargarBorrablePorDueno(HttpServletRequest request) {
        return cargarEnEstados(request, true, "Pendiente");
    }

    /**
     * Obtiene una solicitud de la base de datos validando la propiedad de la misma, así cómo su estado actual.
     *
     * @param request petición HTTP con el parámetro Id
     * @param soloDueno {@code true} para restringirla al dueño; {@code false} deja pasar también al Administrador
     * @param estadosPermitidos lista de nombres de estados con los que es válido recuperar la contraseña
     * @return la {@link Solicitud} si existe, pertenece al usuarui actual y coincide con el estado de la solicitud, de lo contrario {@code null}
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private Solicitud cargarEnEstados(HttpServletRequest request, boolean soloDueno,
                                      String... estadosPermitidos) {
        Integer idUsuario = SesionUtils.idUsuario(request);
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
        boolean puedeTocarla = solicitud.getIdUsuarioSolicitante() == idUsuario.intValue()
                || (!soloDueno && SesionUtils.esAdministrador(request));
        if (!puedeTocarla) {
            return null;
        }
        for (String estado : estadosPermitidos) {
            if (estado.equalsIgnoreCase(solicitud.getNombreEstado())) {
                return solicitud;
            }
        }
        return null;
    }

    /**
     * Construye la lista de desglose de programas educativos leyendo los parámetros del formulario
     * @param request peticion HTTP con los arreglos de entrada
     * @return lista de objetos {@link ProgramaEducativo} capturados en el formulario.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private List<ProgramaEducativo> leerProgramas(HttpServletRequest request) {
        List<ProgramaEducativo> programas = new ArrayList<>();
        String[] nombres = request.getParameterValues("programaEducativo");
        String[] cuatrimestres = request.getParameterValues("cuatrimestre");
        String[] grupos = request.getParameterValues("grupo");
        String[] estudiantes = request.getParameterValues("numEstudiantesGrupo");

        if (nombres == null) {
            return programas;
        }
        for (int i = 0; i < nombres.length; i++) {
            String nombre = nombres[i] != null ? nombres[i].trim() : "";
            if (nombre.isEmpty()) {
                continue;
            }
            ProgramaEducativo p = new ProgramaEducativo();
            p.setPrograma(nombre);
            p.setCuatrimestre(parseEntero(cuatrimestres, i));
            p.setGrupo(grupos != null && i < grupos.length ? grupos[i].trim() : null);
            p.setNoEstudiantes(parseEntero(estudiantes, i));
            programas.add(p);
        }
        return programas;
    }

    /**
     * Extrae el listado de asignaturas a reforzar a partir de los campos del formulario
     * @param request peticion HTTP
     * @return lista de nombres de asignaturas
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private List<String> leerAsignaturas(HttpServletRequest request) {
        List<String> asignaturas = new ArrayList<>();
        String[] valores = request.getParameterValues("asignaturas");
        if (valores != null) {
            for (String valor : valores) {
                if (valor != null && !valor.isBlank()) {
                    asignaturas.add(valor.trim());
                }
            }
        }
        return asignaturas;
    }

    /**
     * Convierte un arreglo de cadenas a un número entero.
     * @param valores arreglo de cadenas
     * @param indice posición dentro del arreglo a parsear (convertir a numerico)
     * @return el valor entero del texto o {@code null} si el índice es inválido
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */

    private int parseEntero(String[] valores, int indice) {
        if (valores == null || indice >= valores.length || valores[indice] == null) {
            return 0;
        }
        try {
            return Integer.parseInt(valores[indice].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
