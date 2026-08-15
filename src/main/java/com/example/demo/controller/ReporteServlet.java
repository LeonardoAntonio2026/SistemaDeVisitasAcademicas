package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Reporte;
import com.example.demo.model.dao.ReporteDao;
import com.example.demo.utils.SesionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bandeja de reportes de visita. El reporte se crea automáticamente (estado
 * Pendiente) cuando una solicitud se completa; el docente lo llena después de
 * realizar su visita y Estadías lo evalúa (RF-08, RF-12).
 */
@WebServlet(name = "ReporteServlet", value = "/reportes")
public class ReporteServlet extends HttpServlet {

    private final ReporteDao reporteDao = new ReporteDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer idUsuario = SesionUtils.idUsuario(request);

        List<Reporte> reportes;
        if (idUsuario == null) {
            reportes = new ArrayList<>();
        } else if (SesionUtils.esRevisor(request)) {
            reportes = reporteDao.getAll();
        } else {
            reportes = reporteDao.getBySolicitante(idUsuario);
        }
        request.setAttribute("listaReportes", reportes);

        request.getRequestDispatcher("reportes.jsp").forward(request, response);
    }
}
