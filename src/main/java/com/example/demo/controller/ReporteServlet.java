package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.demo.model.Reporte;
import com.example.demo.model.dao.ReporteDao;

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
        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
        String rol = (session != null) ? (String) session.getAttribute("rol") : null;

        List<Reporte> reportes;
        if (idUsuario == null) {
            reportes = new ArrayList<>();
        } else if (rol != null && !"Docente".equalsIgnoreCase(rol)) {
            reportes = reporteDao.getAll();
        } else {
            reportes = reporteDao.getBySolicitante(idUsuario);
        }
        request.setAttribute("listaReportes", reportes);

        request.getRequestDispatcher("reportes.jsp").forward(request, response);
    }
}
