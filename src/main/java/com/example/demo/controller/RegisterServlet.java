package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Usuario;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.EmailSender;

import java.io.IOException;
import java.text.MessageFormat;

@WebServlet(name = "RegisterServlet", value = "/register")
public class RegisterServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("registro.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String nombre = request.getParameter("nombre");
        String apellidos = request.getParameter("apellidos");
        String correo1 = request.getParameter("correo1");
        String correo2 = request.getParameter("correo2");
        String contra1 = request.getParameter("contra1");
        String contra2 = request.getParameter("contra2");

        // Validaciones básicas
        if (nombre == null || nombre.isBlank()
                || correo1 == null || correo1.isBlank() || correo2 == null || correo2.isBlank()
                || contra1 == null || contra1.isBlank() || contra2 == null || contra2.isBlank()) {
            request.setAttribute("error", "Por favor, completa todos los campos obligatorios.");
            request.getRequestDispatcher("registro.jsp").forward(request, response);
            return;
        }
        if (!correo1.equals(correo2)) {
            request.setAttribute("error", "Los correos no coinciden.");
            request.getRequestDispatcher("registro.jsp").forward(request, response);
            return;
        }
        if (!contra1.equals(contra2)) {
            request.setAttribute("error", "Las contraseñas no coinciden.");
            request.getRequestDispatcher("registro.jsp").forward(request, response);
            return;
        }
        if (usuarioDao.existeCorreo(correo1)) {
            request.setAttribute("error", "Ya existe una cuenta registrada con ese correo.");
            request.getRequestDispatcher("registro.jsp").forward(request, response);
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setApellidos(apellidos);
        nuevoUsuario.setCorreo(correo1);
        nuevoUsuario.setContrasena(contra1);

        boolean creado = usuarioDao.create(nuevoUsuario);

        if (creado) {
            String plantillaHtml = """
            <html>
                <body style="font-family: Arial, sans-serif; color: #333333;">
                    <h2 style="color: #183052;">¡Hola, {0} {1}!</h2>
                    <p>Tu cuenta en el Sistema de Gestión de Visitas Académicas se creó correctamente.</p>
                    <p>Ya puedes iniciar sesión y registrar tus solicitudes de visita.</p>
                    <p style="font-size: 12px; color: #777777;">Si no realizaste este registro, puedes ignorar este mensaje.</p>
                </body>
            </html>
            """;

            String cuerpoCorreo = MessageFormat.format(
                    plantillaHtml,
                    nuevoUsuario.getNombre(),
                    nuevoUsuario.getApellidos() == null ? "" : nuevoUsuario.getApellidos());

            try {
                EmailSender.sendMail(
                        nuevoUsuario.getCorreo(),
                        "Bienvenido al Sistema de Visitas Académicas",
                        cuerpoCorreo);
            } catch (RuntimeException e) {
                // La cuenta ya se creó; si falla el correo no bloqueamos el registro
                System.err.println("No se pudo enviar el correo de bienvenida: " + e.getMessage());
            }

            request.setAttribute("mensaje", "¡Cuenta creada con éxito! Ahora puedes iniciar sesión.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Hubo un problema interno al crear tu cuenta. Intenta de nuevo.");
            request.getRequestDispatcher("registro.jsp").forward(request, response);
        }
    }
}
