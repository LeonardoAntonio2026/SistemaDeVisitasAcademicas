package com.example.demo.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Arma el enlace de un solo uso con el que una persona define su contraseña.
 *
 * Vive aparte porque lo usan los dos correos que mandan ese enlace: el de
 * "olvidé mi contraseña" y el de bienvenida de una cuenta que acaba de crear el
 * administrador. En los dos casos el token se guarda con TokenRecuperacionDao.
 */
public class EnlaceContrasena {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Genera el token aleatorio que viaja en la URL.
     *
     * @return 32 bytes al azar en Base64 apto para URL, sin relleno
     */
    public static String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Construye la URL absoluta de restablecimiento para ese token.
     *
     * Se saca de la petición y no de una constante porque la misma app corre en
     * localhost:8080 al probar y en el servidor sin puerto; una URL fija manda a
     * la mitad del equipo a un enlace que no abre.
     *
     * @param request petición en curso, de donde salen esquema, host y contexto
     * @param token   token que ya se guardó en la base
     * @return URL completa, lista para el href del correo
     */
    public static String construirUrl(HttpServletRequest request, String token) {
        int puerto = request.getServerPort();
        String sufijoPuerto = (puerto == 80 || puerto == 443) ? "" : ":" + puerto;
        return request.getScheme() + "://" + request.getServerName() + sufijoPuerto
                + request.getContextPath() + "/restablecer-contrasena?token=" + token;
    }
}
