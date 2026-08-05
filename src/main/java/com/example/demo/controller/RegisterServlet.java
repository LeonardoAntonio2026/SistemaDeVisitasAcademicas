package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Usuario;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.EmailSender;
import com.example.demo.utils.Validador;

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

        // Validaciones: las mismas que el navegador, porque un POST directo se las salta
        if (Validador.vacio(nombre) || Validador.vacio(correo1) || Validador.vacio(correo2)
                || contra1 == null || contra1.isBlank() || contra2 == null || contra2.isBlank()) {
            regresarConError(request, response, "Por favor, completa todos los campos obligatorios.");
            return;
        }
        // La tabla USUARIO guarda nombre y apellidos juntos en una columna de 100
        if (nombreCompleto(nombre, apellidos).length() > 100) {
            regresarConError(request, response, "El nombre y los apellidos juntos no deben pasar de 100 caracteres.");
            return;
        }
        if (!Validador.correoValido(correo1)) {
            regresarConError(request, response, "El correo electrónico no tiene un formato válido.");
            return;
        }
        if (!correo1.equals(correo2)) {
            regresarConError(request, response, "Los correos no coinciden.");
            return;
        }
        if (!Validador.contrasenaValida(contra1)) {
            regresarConError(request, response, Validador.REGLA_CONTRASENA);
            return;
        }
        if (!contra1.equals(contra2)) {
            regresarConError(request, response, "Las contraseñas no coinciden.");
            return;
        }
        if (usuarioDao.existeCorreo(correo1)) {
            regresarConError(request, response, "Ya existe una cuenta registrada con ese correo.");
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombreCompleto(nombre, apellidos));
        nuevoUsuario.setCorreo(correo1);
        nuevoUsuario.setContrasena(contra1);

        boolean creado = usuarioDao.create(nuevoUsuario);

        if (creado) {
            String plantillaHtml = """
            <html>
                <body style="font-family: Arial, sans-serif; color: #333333;">
                    <h2 style="color: #183052;">¡Hola, {0}!</h2>
                    <p>Tu cuenta en el Sistema de Gestión de Visitas Académicas se creó correctamente.</p>
                    <p>Ya puedes iniciar sesión y registrar tus solicitudes de visita.</p>
                    <p style="font-size: 12px; color: #777777;">Si no realizaste este registro, puedes ignorar este mensaje.</p>
                </body>
            </html>
            """;

            String cuerpoCorreo = MessageFormat.format(plantillaHtml, nuevoUsuario.getNombre());

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
            regresarConError(request, response, "Hubo un problema interno al crear tu cuenta. Intenta de nuevo.");
        }
    }

    /** La tabla USUARIO solo tiene una columna NOMBRE, así que juntamos nombre y apellidos. */
    private String nombreCompleto(String nombre, String apellidos) {
        String limpio = Validador.limpiar(nombre);
        return Validador.vacio(apellidos) ? limpio : limpio + " " + Validador.limpiar(apellidos);
    }

    /** Vuelve al formulario con el mensaje; los datos capturados se recuperan de param en la vista. */
    private void regresarConError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.getRequestDispatcher("registro.jsp").forward(request, response);
    }
}
