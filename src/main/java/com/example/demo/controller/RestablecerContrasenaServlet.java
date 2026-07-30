package com.example.demo.controller;

import com.example.demo.model.TokenRecuperacion;
import com.example.demo.model.dao.TokenRecuperacionDao;
import com.example.demo.model.dao.UsuarioDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "RestablecerContrasenaServlet", value = "/restablecer-contrasena")
public class RestablecerContrasenaServlet extends HttpServlet {

    private final TokenRecuperacionDao tokenDao = new TokenRecuperacionDao();
    private final UsuarioDao usuarioDao = new UsuarioDao();

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

        if (contra1 == null || contra1.isBlank() || contra2 == null || !contra1.equals(contra2)) {
            request.setAttribute("error", "Las contraseñas no son iguales.");
            request.setAttribute("token", token);
            request.getRequestDispatcher("restablecer-contrasena.jsp").forward(request, response);
            return;
        }

        boolean actualizado = usuarioDao.actualizarContrasena(tokenRecuperacion.getIdUsuario(), contra1);
        if (actualizado) {
            tokenDao.marcarUsado(token);
            request.setAttribute("mensaje", "Tu contraseña se actualizó con éxito. Ya puedes iniciar sesión.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Hubo un problema al actualizar tu contraseña. Intenta de nuevo.");
            request.setAttribute("token", token);
            request.getRequestDispatcher("restablecer-contrasena.jsp").forward(request, response);
        }
    }

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