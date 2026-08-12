package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.demo.model.CatalogoAcademico;
import com.example.demo.model.ProgramaEducativo;
import com.example.demo.model.Solicitud;
import com.example.demo.model.Usuario;
import com.example.demo.model.dao.DocumentoDao;
import com.example.demo.model.dao.SolicitudDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.Validador;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@WebServlet(name = "SolicitudServlet", value = "/solicitud")
public class SolicitudServlet extends HttpServlet {

    /** Tope por celda para atajar capturas absurdas (un grupo no llega a 999 alumnos). */
    private static final int MAX_ESTUDIANTES = 999;

    private final SolicitudDao solicitudDao = new SolicitudDao();
    private final DocumentoDao documentoDao = new DocumentoDao();
    private final UsuarioDao usuarioDao = new UsuarioDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("nueva".equals(request.getParameter("action"))) {
            request.setAttribute("editando", false);
            request.getRequestDispatcher("SolicitudDocente.jsp").forward(request, response);
            return;
        }

        // Editar: el mismo formulario de nueva solicitud pero precargado.
        // Solo el docente dueño y solo mientras siga Pendiente (aún no se envía)
        if ("editar".equals(request.getParameter("action"))) {
            Solicitud solicitud = cargarEditablePorDueno(request);
            if (solicitud == null) {
                // O no es suya, o ya se envió y por eso dejó de ser editable
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.setAttribute("solicitud", solicitud);
            request.setAttribute("editando", true);
            request.getRequestDispatcher("SolicitudDocente.jsp").forward(request, response);
            return;
        }

        // Lista de solicitudes: el docente ve las suyas, Estadías/Administrador las de todos
        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        String rol = (session != null) ? (String) session.getAttribute("rol") : null;

        // Mismas consultas que el inicio (IndexSv): solo las ACTIVAS. Antes se
        // traían todas y la vista escondía las terminadas, así que un docente
        // con puras solicitudes rechazadas veía la página vacía sin el mensaje
        // de "No tienes ninguna solicitud".
        List<Solicitud> solicitudes;
        if (idUsuario == null) {
            solicitudes = new ArrayList<>();
        } else if (rol != null && !"Docente".equalsIgnoreCase(rol)) {
            solicitudes = solicitudDao.getActivasParaRevision();
        } else {
            solicitudes = solicitudDao.getActivasBySolicitante(idUsuario);
        }
        request.setAttribute("listaSolicitudes", solicitudes);

        request.getRequestDispatcher("solicitudes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            // Mismas reglas que editar: solo el docente dueño y solo mientras
            // siga Pendiente; una vez enviada a Estadías ya no se elimina (RF-11)
            Solicitud aEliminar = cargarEditablePorDueno(request);
            if (aEliminar != null) {
                solicitudDao.delete(aEliminar.getIdSolicitud());
            }
        } else if ("create".equals(action)) {
            HttpSession session = request.getSession(false);
            Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
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
            Solicitud solicitud = cargarEditablePorDueno(request);
            if (solicitud == null) {
                // O no es suya, o ya se envió y por eso dejó de ser editable
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

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
                response.sendRedirect("detalle?id=" + solicitud.getIdSolicitud() + "&actualizado=1");
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
     * Reglas de la solicitud. Devuelve la lista de mensajes a mostrar; vacía si
     * todo está bien. El navegador ya valida lo mismo, pero un POST directo se
     * lo salta: esta es la validación que de verdad protege los datos.
     *
     * Los largos máximos son los de las columnas VARCHAR2 del esquema: si se
     * cambia una columna hay que mover también el maxlength de la vista.
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

    /** Campo de texto obligatorio: ni vacío ni más largo de lo que acepta la columna. */
    private void exigirTexto(List<String> errores, String valor, String etiqueta, int maxLargo) {
        if (Validador.vacio(valor)) {
            errores.add("Completa el campo \"" + etiqueta + "\".");
        } else if (Validador.limpiar(valor).length() > maxLargo) {
            errores.add("El campo \"" + etiqueta + "\" no debe pasar de " + maxLargo + " caracteres.");
        }
    }

    /**
     * Vuelve a pintar el formulario con lo que el docente había capturado y la
     * lista de errores, para que no pierda el trabajo por un dato mal escrito.
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

    /** Copia al modelo los campos capturados en el formulario (crear y editar). */
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
     * Docentes acompañantes elegidos con el autocompletado; llegan como ids en
     * inputs ocultos. Se ignoran repetidos y los que no sean números.
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
     * Carga la solicitud del parámetro id solo si el usuario en sesión es el
     * docente que la creó y sigue Pendiente; una vez enviada ya no se edita.
     */
    private Solicitud cargarEditablePorDueno(HttpServletRequest request) {
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
        if (solicitud == null || solicitud.getIdUsuarioSolicitante() != idUsuario
                || !"Pendiente".equalsIgnoreCase(solicitud.getNombreEstado())) {
            return null;
        }
        return solicitud;
    }

    /**
     * Arma las filas del desglose por programa educativo; ignora las filas vacías.
     * La división académica no viaja en el POST: se deduce del programa elegido
     * (el select de división solo sirve para filtrar la lista en el navegador).
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
