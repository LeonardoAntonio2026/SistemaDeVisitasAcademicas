package com.example.demo.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Lectura de los datos que LoginServlet guardó en la sesión ("idUsuario" y
 * "rol"). Antes cada servlet repetía las mismas tres líneas para sacarlos, y
 * la regla de "quién es docente" estaba escrita de dos formas distintas según
 * el archivo; si algún día cambian los roles, aquí es el único lugar a tocar.
 *
 * Todos los métodos aguantan que no haya sesión: devuelven null o false en vez
 * de reventar, porque FiltroAutenticacion ya rebotó al login mucho antes.
 */
public final class SesionUtils {

    /** Rol del coordinador de Estadías y del Administrador se comparan por nombre. */
    private static final String ROL_DOCENTE = "Docente";
    private static final String ROL_ADMIN = "Administrador";

    private SesionUtils() {}

    /** Id del usuario en sesión, o null si no ha iniciado sesión. */
    public static Integer idUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
    }

    /** Nombre del rol en sesión, o null si no ha iniciado sesión. */
    public static String rol(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("rol") : null;
    }

    /**
     * Docente = el rol dice "Docente", o no se pudo leer el rol.
     *
     * Que un rol nulo cuente como docente NO es un descuido: es el permiso más
     * restringido de los tres, así que ante la duda se cae del lado que menos
     * deja ver. Quien de verdad no tenga sesión ya fue rebotado por el filtro.
     */
    public static boolean esDocente(HttpServletRequest request) {
        String rol = rol(request);
        return rol == null || ROL_DOCENTE.equalsIgnoreCase(rol);
    }

    /**
     * Estadías o Administrador: los que revisan solicitudes y reportes ajenos.
     * Es justo lo contrario de esDocente(), con nombre propio para que las
     * condiciones de los servlets se lean solas.
     */
    public static boolean esRevisor(HttpServletRequest request) {
        return !esDocente(request);
    }

    /**
     * Quién puede levantar sus propias solicitudes y reportes: el Docente y
     * también el Administrador, que además de revisar da clases y sale de
     * visita como cualquiera.
     *
     * Ojo: el Administrador es lo único que cae de los dos lados, esRevisor()
     * y puedeSolicitar(). Por eso, en lo que toca a UNA solicitud concreta, el
     * permiso no se decide por el rol sino por si es suya: el dueño hace lo del
     * docente (subir, enviar, corregir) y el revisor evalúa las de los demás.
     */
    public static boolean puedeSolicitar(HttpServletRequest request) {
        return esDocente(request) || esAdministrador(request);
    }

    /** Solo el Administrador; la gestión de usuarios (RF-12) no la abre nadie más. */
    public static boolean esAdministrador(HttpServletRequest request) {
        return ROL_ADMIN.equalsIgnoreCase(rol(request));
    }
}
