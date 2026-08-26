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

/**
 * Servlet encargado de gestionar la autenticación de usuarios.
 * Maneja el despliegue del formulario de inicio de sesión y la
 * validación de credenciales para iniciar una sesión de usuario.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    /** Instancia de UsuarioDao para gestionar las consultas a la base de datos. */
    private final UsuarioDao usuarioDao = new UsuarioDao();

    /**
     * Muestra la vista del formulario de inicio de sesión.
     *
     * @param request  Objeto {@link HttpServletRequest} con la petición del cliente.
     * @param response Objeto {@link HttpServletResponse} con la respuesta del servidor.
     * @throws ServletException Si ocurre un error interno en el Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida al redirigir la petición.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    /**
     * Procesa las credenciales enviadas desde el formulario de inicio de sesión.
     * Si las credenciales son correctas, crea la sesión de usuario y redirige al dashboard.
     * De lo contrario, retorna un mensaje de error a la vista.
     *
     * @param request  Objeto {@link HttpServletRequest} que contiene las credenciales ("correo" y "contrasena").
     * @param response Objeto {@link HttpServletResponse} para realizar la redirección o el reenvío de la petición.
     * @throws ServletException Si ocurre un error interno en el Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida.
     */
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
            session.setAttribute("idUsuario", usuario.getId());
            session.setAttribute("rol", usuario.getNombreRol());
            // Redirigimos al servlet (no al JSP) para que cargue las solicitudes del usuario
            response.sendRedirect("indexSv");
        } else {
            request.setAttribute("error", "Correo o contraseña incorrectos. Inténtalo de nuevo.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}