package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet encargado de gestionar el cierre de sesión de los usuarios.
 * Invalida la sesión activa actual y redirige al usuario hacia la página de inicio de sesión.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
@WebServlet(name = "LogoutServlet", value = "/logout")
public class LogoutServlet extends HttpServlet {

    /**
     * Procesa la petición GET para cerrar la sesión del usuario.
     * Verifica si existe una sesión activa, la destruye mediante {@link HttpSession#invalidate()}
     * y redirige la respuesta hacia la vista "login.jsp".
     *
     * @param request  Objeto {@link HttpServletRequest} con la petición del cliente.
     * @param response Objeto {@link HttpServletResponse} para realizar la redirección.
     * @throws ServletException Si ocurre un error interno en el Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida al redirigir la petición.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("login.jsp");
    }
}