package com.example.demo.controller.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// El filtro se aplica a todas las URLs de la app
@WebFilter("/*")
public class FiltroAutenticacion extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false); // false = no crea una sesión nueva

        // 1. ¿El usuario ya inició sesión? (guardamos el atributo "usuario" al hacer login)
        boolean loggedIn = (session != null && session.getAttribute("usuario") != null);

        // 2. Rutas públicas: login, registro y sus servlets
        boolean loginRequest =
                requestURI.endsWith("login.jsp") ||
                requestURI.endsWith("/login") ||
                requestURI.endsWith("registro.jsp") ||
                requestURI.endsWith("/register");

        // 3. Recursos estáticos que deben cargar aunque no haya sesión (para que el login tenga estilos)
        boolean isResource =
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/img/") ||
                requestURI.contains("/fonts/") ||
                requestURI.contains("/layout/");

        if (loggedIn) {
            // CON sesión: si intenta ir al login/registro, lo mandamos al inicio
            if (loginRequest) {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            } else {
                chain.doFilter(request, response);
            }
        } else {
            // SIN sesión: solo dejamos pasar login/registro y recursos públicos
            if (loginRequest || isResource) {
                chain.doFilter(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
        }
   }
}
