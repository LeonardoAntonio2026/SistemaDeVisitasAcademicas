package com.example.demo.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Utilería de solo lectura sobre los datos de la sesión del usuario.
 * <p>
 * Lectura de los datos que LoginServlet guardó en la sesión ("idUsuario" y
 * "rol"). Único lugar donde se decide quién es docente y quién revisa.
 * </p>
 * <p>
 * Todos los métodos aguantan que no haya sesión: devuelven null o false en vez
 * de reventar, porque FiltroAutenticacion ya rebotó al login mucho antes.
 * </p>
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 24/08/2026
 */
public final class SesionUtils {

    /** Rol del coordinador de Estadías y del Administrador se comparan por nombre. */
    private static final String ROL_DOCENTE = "Docente";
    private static final String ROL_ADMIN = "Administrador";

    private SesionUtils() {}

    /**
     * Id del usuario en sesión.
     *
     * @param request petición HTTP de la que se lee la sesión
     * @return el id del usuario, o {@code null} si no ha iniciado sesión
     */
    public static Integer idUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
    }

    /**
     * Nombre del rol en sesión.
     *
     * @param request petición HTTP de la que se lee la sesión
     * @return el nombre del rol ("Docente", "Estadias" o "Administrador"),
     *         o {@code null} si no ha iniciado sesión
     */
    public static String rol(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("rol") : null;
    }

    /**
     * Indica si el usuario en sesión es Docente.
     *
     * Docente = el rol dice "Docente", o no se pudo leer el rol.
     *
     * Que un rol nulo cuente como docente es a propósito: es el permiso más
     * restringido de los tres, así que ante la duda se cae del lado que menos
     * deja ver.
     *
     * @param request petición HTTP de la que se lee la sesión
     * @return {@code true} si el rol es "Docente" o no hay rol en sesión
     */
    public static boolean esDocente(HttpServletRequest request) {
        String rol = rol(request);
        return rol == null || ROL_DOCENTE.equalsIgnoreCase(rol);
    }

    /**
     * Indica si el usuario en sesión revisa el trabajo de los demás.
     *
     * Estadías o Administrador: los que revisan solicitudes y reportes ajenos.
     * Es lo contrario de esDocente(), con nombre propio.
     *
     * @param request petición HTTP de la que se lee la sesión
     * @return {@code true} si el rol es "Estadias" o "Administrador"
     */
    public static boolean esRevisor(HttpServletRequest request) {
        return !esDocente(request);
    }

    /**
     * Indica si el usuario en sesión puede levantar solicitudes propias.
     *
     * Quién puede levantar sus propias solicitudes y reportes: el Docente y
     * también el Administrador, que además de revisar da clases y sale de
     * visita como cualquiera.
     *
     * Ojo: el Administrador es lo único que cae de los dos lados, esRevisor()
     * y puedeSolicitar(). Por eso, en lo que toca a UNA solicitud concreta, el
     * permiso no se decide por el rol sino por si es suya: el dueño hace lo del
     * docente (subir, enviar, corregir) y el revisor evalúa las de los demás.
     *
     * @param request petición HTTP de la que se lee la sesión
     * @return {@code true} para el Docente y para el Administrador
     */
    public static boolean puedeSolicitar(HttpServletRequest request) {
        return esDocente(request) || esAdministrador(request);
    }

    /**
     * Indica si el usuario en sesión es Administrador.
     *
     * Solo el Administrador; la gestión de usuarios (RF-12) no la abre nadie más.
     *
     * @param request petición HTTP de la que se lee la sesión
     * @return {@code true} solo si el rol es "Administrador"
     */
    public static boolean esAdministrador(HttpServletRequest request) {
        return ROL_ADMIN.equalsIgnoreCase(rol(request));
    }
}
