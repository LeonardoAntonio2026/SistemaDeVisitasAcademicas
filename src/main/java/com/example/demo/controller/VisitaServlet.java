package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Visita;
import com.example.demo.dao.VisitaDao;
import java.io.IOException;

@WebServlet("/VisitaServlet")
public class VisitaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Recorrer y mapear los campos (Sincronizados con los 'name' de tu JSP)
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

        // 2. Llenar el objeto Visita
        Visita nuevaVisita = new Visita();
        nuevaVisita.setNombreEmpresa(nombreEmpresa);
        nuevaVisita.setDireccion(direccion);
        nuevaVisita.setTelefono(telefono);
        nuevaVisita.setCorreo(correo);
        nuevaVisita.setFechaInicio(fechaInicio);
        nuevaVisita.setFechaTermino(fechaTermino);
        nuevaVisita.setHoraVisita(horaVisita);
        nuevaVisita.setObjetivo(objetivo);
        nuevaVisita.setAreaSolicitante(areaSolicitante);
        nuevaVisita.setDocenteResponsable(docenteResponsable);
        nuevaVisita.setCelularDocente(celularDocente);
        nuevaVisita.setDocentesAcompanantes(docentesAcompanantes);

        // 3. Llamar al DAO para guardar en Oracle Cloud
        VisitaDao dao = new VisitaDao();
        boolean guardado = dao.insertar(nuevaVisita);

        if (guardado) {
            response.sendRedirect("solicitudes.jsp?status=success");
        } else {
            response.sendRedirect("nueva_solicitud.jsp?status=error");
        }
    }
}