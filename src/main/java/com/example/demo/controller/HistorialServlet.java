package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Solicitud;
import com.example.demo.model.dao.SolicitudDao;
import com.example.demo.utils.SesionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Histórico de solicitudes: muestra las que ya terminaron su proceso
 * (Completadas y Rechazadas). Las que aún están activas viven en el inicio;
 * al completarse o rechazarse salen de ahí y aparecen aquí (RN-05, RF-09).
 */
@WebServlet(name = "HistorialServlet", value = "/historico")
public class HistorialServlet extends HttpServlet {

    private final SolicitudDao solicitudDao = new SolicitudDao();
    /**
     * Carga el histórico correspondiente al usuario en sesión: quien revisa
     * (Estadías/Administrador) ve el histórico completo de todos los
     * docentes, mientras que un docente ve solo el suyo. Sin sesión
     * iniciada, la lista queda vacía.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer idUsuario = SesionUtils.idUsuario(request);

        List<Solicitud> solicitudes;
        if (idUsuario == null) {
            solicitudes = new ArrayList<>();
        } else if (SesionUtils.esRevisor(request)) {
            solicitudes = solicitudDao.getHistorico(null);
        } else {
            solicitudes = solicitudDao.getHistorico(idUsuario);
        }
        request.setAttribute("listaHistorico", solicitudes);

        request.getRequestDispatcher("historico.jsp").forward(request, response);
    }
}
