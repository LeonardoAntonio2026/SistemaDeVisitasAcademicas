package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.model.dao.TokenRecuperacionDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.EmailSender;
import com.example.demo.utils.EnlaceContrasena;
import com.example.demo.utils.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Servlet encargado de gestionar la solicitud de recuperación de contraseña.
 * Genera tokens temporales de seguridad y envía los correos electrónicos correspondientes
 * para el restablecimiento de credenciales sin revelar la existencia previa del correo en la base de datos.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
@WebServlet(name = "OlvideContrasenaServlet", value = "/olvide-contrasena")
public class OlvideContrasenaServlet extends HttpServlet {

    /** Mensaje de respuesta unificado para evitar la enumeración de usuarios registrados. */
    private static final String MENSAJE_GENERICO =
            "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.";

    /** Instancia de UsuarioDao para consultar la existencia del usuario. */
    private final UsuarioDao usuarioDao = new UsuarioDao();

    /** Instancia de TokenRecuperacionDao para gestionar la persistencia de tokens de recuperación. */
    private final TokenRecuperacionDao tokenDao = new TokenRecuperacionDao();

    /**
     * Redirige al usuario hacia la vista del formulario de recuperación de contraseña.
     *
     * @param request  Objeto {@link HttpServletRequest} con la petición del cliente.
     * @param response Objeto {@link HttpServletResponse} con la respuesta del servidor.
     * @throws ServletException Si ocurre un error interno en el Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida al despachar la vista.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("olvide-contrasena.jsp").forward(request, response);
    }

    /**
     * Procesa la solicitud de recuperación enviada desde el formulario.
     * Valida la estructura del correo, busca la coincidencia en la base de datos,
     * genera el token seguro y notifica al usuario por correo sin dar pistas de existencia en caso fallido.
     *
     * @param request  Objeto {@link HttpServletRequest} que contiene el parámetro "correo".
     * @param response Objeto {@link HttpServletResponse} para retornar la vista con la confirmación.
     * @throws ServletException Si ocurre un error interno en el Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida.
     */
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
                String token = EnlaceContrasena.generarToken();
                boolean guardado = tokenDao.crear(usuario.getId(), token);
                if (guardado) {
                    enviarCorreoRecuperacion(request, usuario, token);
                }
            }
        }

        request.setAttribute("mensaje", MENSAJE_GENERICO);
        request.getRequestDispatcher("olvide-contrasena.jsp").forward(request, response);
    }

    /**
     * Construye la plantilla HTML del correo e invoca el servicio de envío de correos.
     *
     * @param request Objeto {@link HttpServletRequest} utilizado para construir la URL dinámica.
     * @param usuario Objeto {@link Usuario} receptor del mensaje.
     * @param token   Cadena de texto con el token asignado para el restablecimiento.
     */
    private void enviarCorreoRecuperacion(HttpServletRequest request, Usuario usuario, String token) {
        String enlace = EnlaceContrasena.construirUrl(request, token);

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
}