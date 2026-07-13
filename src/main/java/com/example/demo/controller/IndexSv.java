package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.demo.model.Solicitud;
import com.example.demo.model.dao.SolicitudDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "indexSv", value = "/indexSv")
public class IndexSv extends HttpServlet {

    private final SolicitudDao solicitudDao = new SolicitudDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        String rol = (session != null) ? (String) session.getAttribute("rol") : null;

        // En el inicio solo van las ACTIVAS (las terminadas viven en el histórico, RN-05).
        // El docente ve las suyas; Estadías ve las enviadas por los docentes
        // (las Pendientes aún no se envían, por eso no le aparecen)
        List<Solicitud> solicitudes;
        if (idUsuario == null) {
            solicitudes = new ArrayList<>();
        } else if (rol != null && !"Docente".equalsIgnoreCase(rol)) {
            solicitudes = solicitudDao.getActivasParaRevision();
        } else {
            solicitudes = solicitudDao.getActivasBySolicitante(idUsuario);
        }
        req.setAttribute("listaSolicitudes", solicitudes);

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("indexSv");
    }
}
