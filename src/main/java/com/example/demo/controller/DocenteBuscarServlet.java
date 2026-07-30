package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.model.dao.UsuarioDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Sugerencias para el autocompletado de docentes acompañantes del formulario
 * de solicitud. Devuelve JSON: [{"id":1,"nombre":"...","correo":"..."}]
 */
@WebServlet(name = "DocenteBuscarServlet", value = "/docentes")
public class DocenteBuscarServlet extends HttpServlet {

    private static final int MAX_SUGERENCIAS = 8;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String texto = request.getParameter("q");
        if (texto == null || texto.trim().length() < 2) {
            response.getWriter().write("[]");
            return;
        }

        HttpSession session = request.getSession(false);
        Integer idUsuario = (session != null) ? (Integer) session.getAttribute("idUsuario") : null;

        List<Usuario> docentes = usuarioDao.buscarDocentes(texto, idUsuario, MAX_SUGERENCIAS);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < docentes.size(); i++) {
            Usuario d = docentes.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"id\":").append(d.getId())
                .append(",\"nombre\":\"").append(escapar(d.getNombre())).append('"')
                .append(",\"correo\":\"").append(escapar(d.getCorreo())).append("\"}");
        }
        json.append(']');

        try (PrintWriter out = response.getWriter()) {
            out.write(json.toString());
        }
    }

    /** Escapa lo mínimo para que un nombre con comillas no rompa el JSON. */
    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }
}
