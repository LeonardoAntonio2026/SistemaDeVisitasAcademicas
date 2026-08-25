package com.example.demo.controller;

import com.example.demo.model.TokenRecuperacion;
import com.example.demo.model.dao.TokenRecuperacionDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet encargado de procesar el restablecimiento de contraseñas de los usuarios.
 * Valida los tokens de seguridad enviados por enlace, verifica los requisitos de las
 * nuevas credenciales y actualiza la información en el sistema.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
@WebServlet(name = "RestablecerContrasenaServlet", value = "/restablecer-contrasena")
public class RestablecerContrasenaServlet extends HttpServlet {

    /** Instancia de TokenRecuperacionDao para consultar y actualizar el estado de los tokens. */
    private final TokenRecuperacionDao tokenDao = new TokenRecuperacionDao();

    /** Instancia de UsuarioDao para actualizar las credenciales del usuario. */
    private final UsuarioDao usuarioDao = new UsuarioDao();

    /**
     * Procesa las peticiones GET verificando si el token adjunto en la URL es válido.
     * Carga la vista del formulario para la nueva contraseña.
     *
     * @param request  Objeto {@link HttpServletRequest} que contiene el parámetro "token".
     * @param response Objeto {@link HttpServletResponse} con la respuesta del servidor.
     * @throws ServletException Si ocurre un error interno en el Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida al despachar la vista.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");

        if (validarToken(token) == null) {
            request.setAttribute("tokenInvalido", true);
        } else {
            request.setAttribute("token", token);
        }
        request.getRequestDispatcher("restablecer-contrasena.jsp").forward(request, response);
    }

    /**
     * Procesa el cambio de contraseña enviando las nuevas credenciales vía POST.
     * Valida la vigencia del token, las reglas de complejidad de la contraseña,
     * actualiza la base de datos, invalida el token y finaliza cualquier sesión previa.
     *
     * @param request  Objeto {@link HttpServletRequest} con los parámetros "token", "contra1" y "contra2".
     * @param response Objeto {@link HttpServletResponse} para redirigir al login o volver a la vista.
     * @throws ServletException Si ocurre un error interno en el Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");
        String contra1 = request.getParameter("contra1");
        String contra2 = request.getParameter("contra2");

        TokenRecuperacion tokenRecuperacion = validarToken(token);
        if (tokenRecuperacion == null) {
            request.setAttribute("tokenInvalido", true);
            request.getRequestDispatcher("restablecer-contrasena.jsp").forward(request, response);
            return;
        }

        if (!Validador.contrasenaValida(contra1)) {
            request.setAttribute("error", Validador.REGLA_CONTRASENA);
            request.setAttribute("token", token);
            request.getRequestDispatcher("restablecer-contrasena.jsp").forward(request, response);
            return;
        }
        if (!contra1.equals(contra2)) {
            request.setAttribute("error", "Las contraseñas no son iguales.");
            request.setAttribute("token", token);
            request.getRequestDispatcher("restablecer-contrasena.jsp").forward(request, response);
            return;
        }

        boolean actualizado = usuarioDao.actualizarContrasena(tokenRecuperacion.getIdUsuario(), contra1);
        if (actualizado) {
            tokenDao.marcarUsado(token);

            // Quien recupera la contraseña vuelve a entrar desde cero: si había una
            // sesión abierta en este navegador (propia o de otro), se cierra.
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }

            // Redirect y no forward: así la URL deja de ser el POST y refrescar la
            // página no reenvía el formulario con un token ya gastado.
            response.sendRedirect(request.getContextPath() + "/login.jsp?mensaje=restablecida");
        } else {
            request.setAttribute("error", "Hubo un problema al actualizar tu contraseña. Intenta de nuevo.");
            request.setAttribute("token", token);
            request.getRequestDispatcher("restablecer-contrasena.jsp").forward(request, response);
        }
    }

    /**
     * Método auxiliar encargado de comprobar la integridad y vigencia de un token.
     *
     * @param token Cadena con la clave única a evaluar.
     * @return Objeto {@link TokenRecuperacion} si el token existe, no ha sido usado ni está vencido;
     *         {@code null} en caso contrario.
     */
    private TokenRecuperacion validarToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        TokenRecuperacion t = tokenDao.buscarPorToken(token);
        if (t == null || t.isUsado() || t.estaVencido()) {
            return null;
        }
        return t;
    }
}