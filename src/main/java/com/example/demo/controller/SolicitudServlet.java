package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.demo.model.ProgramaEducativo;
import com.example.demo.model.Solicitud;
import com.example.demo.model.dao.SolicitudDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "SolicitudServlet", value = "/solicitud")
public class SolicitudServlet extends HttpServlet {

    private final SolicitudDao solicitudDao = new SolicitudDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("nueva".equals(request.getParameter("action"))) {
            request.getRequestDispatcher("SolicitudDocente.jsp").forward(request, response);
            return;
        }

        // Lista de solicitudes: el docente ve las suyas, Estadías/Administrador las de todos
        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        String rol = (session != null) ? (String) session.getAttribute("rol") : null;

        List<Solicitud> solicitudes;
        if (idUsuario == null) {
            solicitudes = new ArrayList<>();
        } else if (rol != null && !"Docente".equalsIgnoreCase(rol)) {
            // Estadías/Admin ven todas las ya enviadas (las Pendientes aún no)
            solicitudes = solicitudDao.getEnviadas();
        } else {
            solicitudes = solicitudDao.getBySolicitante(idUsuario);
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
            int id = Integer.parseInt(request.getParameter("id"));
            solicitudDao.delete(id);
        } else if ("create".equals(action)) {
            HttpSession session = request.getSession(false);
            Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
            if (idUsuario == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            Solicitud solicitud = new Solicitud();
            solicitud.setIdUsuarioSolicitante(idUsuario);
            solicitud.setNombreEmpresaActividad(request.getParameter("nombreEmpresa"));
            solicitud.setLugarDireccion(request.getParameter("direccionLugar"));
            solicitud.setTelefonoContacto(request.getParameter("telefonoContacto"));
            solicitud.setCorreoContacto(request.getParameter("correoContacto"));
            solicitud.setFechaInicio(request.getParameter("fechaInicio"));
            solicitud.setObjetivo(request.getParameter("objetivoVisita"));
            solicitud.setAreaSolicitante(request.getParameter("areaSolicitante"));
            solicitud.setProgramas(leerProgramas(request));
            solicitud.setAsignaturas(leerAsignaturas(request));

            if (solicitudDao.create(solicitud)) {
                // Crear NO envía a Estadías: el docente cae en los detalles para
                // descargar el formato, firmarlo, subirlo y ahí dar ENVIAR (RN-02)
                response.sendRedirect("detalle?id=" + solicitud.getIdSolicitud());
                return;
            }
        }

        // Patrón PRG: Redirigir al GET evita que al recargar la página se repita la operación
        response.sendRedirect("indexSv");
    }

    /**
     * Arma las filas del desglose por programa educativo; ignora las filas vacías.
     */
    private List<ProgramaEducativo> leerProgramas(HttpServletRequest request) {
        List<ProgramaEducativo> programas = new ArrayList<>();
        String[] divisiones = request.getParameterValues("programaEducativo");
        String[] cuatrimestres = request.getParameterValues("cuatrimestre");
        String[] grupos = request.getParameterValues("grupo");
        String[] estudiantes = request.getParameterValues("numEstudiantesGrupo");

        if (divisiones == null) {
            return programas;
        }
        for (int i = 0; i < divisiones.length; i++) {
            String division = divisiones[i] != null ? divisiones[i].trim() : "";
            if (division.isEmpty()) {
                continue;
            }
            ProgramaEducativo p = new ProgramaEducativo();
            p.setDivisionAcademica(division);
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
