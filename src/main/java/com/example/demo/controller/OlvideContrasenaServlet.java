package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.model.dao.TokenRecuperacionDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.EmailSender;
import com.example.demo.utils.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Base64;

@WebServlet(name = "OlvideContrasenaServlet", value = "/olvide-contrasena")
public class OlvideContrasenaServlet extends HttpServlet {

    private static final String MENSAJE_GENERICO =
            "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.";

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final TokenRecuperacionDao tokenDao = new TokenRecuperacionDao();
    private final SecureRandom random = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("olvide-contrasena.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Se valida el formato antes de ir a la BD, pero un correo mal escrito
        // NO se delata con un error propio: la respuesta es siempre la misma para
        // no revelar qué correos están registrados.
        String correo = request.getParameter("correo");
        if (Validador.correoValido(correo)) {
            Usuario usuario = usuarioDao.getByCorreo(Validador.limpiar(correo));
            if (usuario != null) {
                String token = generarToken();
                boolean guardado = tokenDao.crear(usuario.getId(), token);
                if (guardado) {
                    enviarCorreoRecuperacion(request, usuario, token);
                }
            }
        }

        request.setAttribute("mensaje", MENSAJE_GENERICO);
        request.getRequestDispatcher("olvide-contrasena.jsp").forward(request, response);
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void enviarCorreoRecuperacion(HttpServletRequest request, Usuario usuario, String token) {
        String enlace = urlBase(request) + "/restablecer-contrasena?token=" + token;

        String plantillaHtml = """
        <html>
            <body style="font-family: Arial, sans-serif; color: #333333;">
                <h2 style="color: #183052;">Recupera tu contraseña</h2>
                <p>Hola {0}, recibimos una solicitud para restablecer tu contraseña.</p>
                <p><a href="{1}" style="color: #183052;">Haz click aquí para crear una nueva contraseña</a></p>
                <p>Este enlace es válido durante <strong>{2} horas</strong>. Si tú no solicitaste esto, puedes ignorar este correo.</p>
                <p style="font-size: 12px; color: #777777;">Sistema de Gestión de Visitas Académicas - UTEZ</p>
            </body>
        </html>
        """;
        String cuerpo = MessageFormat.format(plantillaHtml, usuario.getNombre(), enlace,
                String.valueOf(TokenRecuperacionDao.HORAS_VIGENCIA));

        try {
            EmailSender.sendMail(usuario.getCorreo(),
                    "Recupera tu contraseña - Visitas Académicas", cuerpo);
        } catch (RuntimeException e) {
            System.err.println("No se pudo enviar el correo de recuperación: " + e.getMessage());
        }
    }

    private String urlBase(HttpServletRequest request) {
        int puerto = request.getServerPort();
        String sufijoPuerto = (puerto == 80 || puerto == 443) ? "" : ":" + puerto;
        return request.getScheme() + "://" + request.getServerName() + sufijoPuerto + request.getContextPath();
    }
}