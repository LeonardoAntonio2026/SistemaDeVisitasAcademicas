package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Visita;
import com.example.demo.model.dao.VisitaDao;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "VisitaServlet", value = "/visita")
public class VisitaServlet extends HttpServlet {

    private final VisitaDao visitaDao = new VisitaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Visita> lista = visitaDao.getAll();
        request.setAttribute("listaVisitas", lista);

        request.getRequestDispatcher("SolicitudDocente.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            visitaDao.delete(id);
        } else if ("create".equals(action) || "update".equals(action)) {
            String nombreEmpresa = request.getParameter("nombreEmpresa");
            String direccion = request.getParameter("direccionLugar");
            String telefono = request.getParameter("telefonoContacto");
            String correo = request.getParameter("correoContacto");
            String fechaInicio = request.getParameter("fechaInicio");
            String fechaTermino = request.getParameter("fechaTermino");
            String horaVisita = request.getParameter("horaVisita");
            String objetivo = request.getParameter("objetivoVisita");
            String areaSolicitante = request.getParameter("areaSolicitante");
            String docenteResponsable = request.getParameter("docenteResponsable");
            String celularDocente = request.getParameter("celularDocente");
            String docentesAcompanantes = request.getParameter("docentesAcompanantes");

            Visita visita = new Visita();
            visita.setNombreEmpresa(nombreEmpresa);
            visita.setDireccion(direccion);
            visita.setTelefono(telefono);
            visita.setCorreo(correo);
            visita.setFechaInicio(fechaInicio);
            visita.setFechaTermino(fechaTermino);
            visita.setHoraVisita(horaVisita);
            visita.setObjetivo(objetivo);
            visita.setAreaSolicitante(areaSolicitante);
            visita.setDocenteResponsable(docenteResponsable);
            visita.setCelularDocente(celularDocente);
            visita.setDocentesAcompanantes(docentesAcompanantes);

            if ("create".equals(action)) {
                visitaDao.create(visita);
            } else {
                visita.setId(Integer.parseInt(request.getParameter("id")));
                visitaDao.update(visita);
            }
        }

        // Patrón PRG: Redirigir al GET evita que al recargar la página se repita la operación
        response.sendRedirect("visita");
    }
}