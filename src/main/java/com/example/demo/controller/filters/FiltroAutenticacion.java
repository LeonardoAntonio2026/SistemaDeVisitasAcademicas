package com.example.demo.controller.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtro de seguridad encargado de interceptar todas las peticiones HTTP de la aplicación.
 * Evalúa el estado de autenticación de las sesiones y controla el acceso a rutas protegidas,
 * recursos estáticos y vistas públicas como el inicio de sesión y recuperación de contraseña.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
@WebFilter("/*")
public class FiltroAutenticacion extends HttpFilter {

    /**
     * Intercepta la petición HTTP y determina si el cliente tiene acceso al recurso solicitado.
     * <p>
     * Utiliza {@link HttpServletRequest#getServletPath()} para obtener la ruta normalizada y prevenir
     * vulnerabilidades de salto de directorio (Path Traversal).
     * </p>
     *
     * @param request  Objeto {@link HttpServletRequest} con la petición del cliente.
     * @param response Objeto {@link HttpServletResponse} para controlar redirecciones de acceso.
     * @param chain    Objeto {@link FilterChain} para invocar el siguiente filtro o servlet en la cadena.
     * @throws IOException      Si ocurre un error de entrada/salida durante la redirección o filtrado.
     * @throws ServletException Si ocurre un error interno dentro del contenedor de servlets.
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Usamos getServletPath() y NO getRequestURI(): getRequestURI() devuelve la URI
        // tal cual venía en la petición, sin normalizar. Con ella, una ruta como
        // "/js/../docentes" pasaba el chequeo de recursos estáticos de abajo (contiene
        // "/js/") pero el servidor la resolvía a "/docentes", saltándose el filtro.
        // getServletPath() ya viene normalizada por el contenedor (sin "..", sin
        // ";jsessionid=" y sin el context path), así que no se puede engañar.
        String path = request.getServletPath();
        HttpSession session = request.getSession(false); // false = no crea una sesión nueva

        // 1. ¿El usuario ya inició sesión? (guardamos el atributo "usuario" al hacer login)
        boolean loggedIn = (session != null && session.getAttribute("usuario") != null);

        // 2. Ruta pública: solo el login.
        // Aquí estaban también "/registro.jsp" y "/register": se quitaron junto con
        // el registro público porque nadie se da de alta solo, las cuentas las crea
        // el Administrador desde /usuarios. Mientras estuvo abierto, cualquiera con
        // la URL podía crearse una cuenta Docente sin que el Administrador se
        // enterara.
        // Comparación exacta, no endsWith(): endsWith aceptaba cualquier ruta que
        // terminara en "login.jsp".
        boolean loginRequest =
                path.equals("/login.jsp") ||
                        path.equals("/login");

        // 2b. Recuperación de contraseña (RF-02): públicas TAMBIÉN con sesión abierta.
        // El enlace llega por correo y es normal abrirlo desde el navegador donde ya
        // se inició sesión (propia o de alguien más en un equipo compartido); si aquí
        // se redirige a index.jsp, el enlace del correo simplemente no funciona.
        boolean recuperacionRequest =
                path.equals("/olvide-contrasena.jsp") ||
                        path.equals("/olvide-contrasena") ||
                        path.equals("/restablecer-contrasena.jsp") ||
                        path.equals("/restablecer-contrasena");

        // 3. Recursos estáticos que deben cargar aunque no haya sesión (para que el login tenga estilos).
        // startsWith() y no contains(): con contains() bastaba con que la ruta mencionara
        // "/js/" en cualquier parte para que se tratara como recurso público.
        // "/layout/" ya no va aquí: esos JSP no son recursos estáticos, son includes
        // (<%@ include %>) que se resuelven al compilar, así que el filtro nunca los ve.
        boolean isResource =
                path.startsWith("/css/") ||
                        path.startsWith("/js/") ||
                        path.startsWith("/img/") ||
                        path.startsWith("/fonts/");

        if (loggedIn) {
            // CON sesión: si intenta ir al login, lo mandamos al inicio.
            // Al SERVLET (/indexSv), no al JSP: el JSP solo no carga las
            // solicitudes y se veía "No tienes ninguna solicitud" en falso.
            if (loginRequest) {
                response.sendRedirect(request.getContextPath() + "/indexSv");
            } else {
                chain.doFilter(request, response);
            }
        } else {
            // SIN sesión: solo dejamos pasar el login, la recuperación y los recursos públicos
            if (loginRequest || recuperacionRequest || isResource) {
                chain.doFilter(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
        }
    }
}