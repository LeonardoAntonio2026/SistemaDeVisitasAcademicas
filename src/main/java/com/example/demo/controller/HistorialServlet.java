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

/**
 * Histórico: solicitudes terminadas (Completadas y Rechazadas). Las
 * completadas salen del inicio y viven aquí (RN-05, RF-09).
 */
@WebServlet(name = "HistorialServlet", value = "/historico")
public class HistorialServlet extends HttpServlet {

    private final SolicitudDao solicitudDao = new SolicitudDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        String rol = (session != null) ? (String) session.getAttribute("rol") : null;

        List<Solicitud> solicitudes;
        if (idUsuario == null) {
            solicitudes = new ArrayList<>();
        } else if (rol != null && !"Docente".equalsIgnoreCase(rol)) {
            solicitudes = solicitudDao.getHistorico(null);
        } else {
            solicitudes = solicitudDao.getHistorico(idUsuario);
        }
        request.setAttribute("listaHistorico", solicitudes);

        request.getRequestDispatcher("historico.jsp").forward(request, response);
    }
}
