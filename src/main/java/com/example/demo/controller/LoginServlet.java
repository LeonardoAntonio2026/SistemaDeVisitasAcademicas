package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.demo.model.Usuario;
import com.example.demo.model.dao.UsuarioDao;

import java.io.IOException;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        Usuario usuario = usuarioDao.login(correo, contrasena);

        if (usuario != null) {
            HttpSession session = request.getSession(true); // true = crea la sesión si no existe
            session.setAttribute("usuario", usuario.getCorreo());
            session.setAttribute("nombreUsuario", usuario.getNombre());
            response.sendRedirect("index.jsp");
        } else {
            request.setAttribute("error", "Correo o contraseña incorrectos. Inténtalo de nuevo.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
