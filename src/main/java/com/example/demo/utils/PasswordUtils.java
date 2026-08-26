package com.example.demo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase utilitaria encargada del procesamiento seguro de contraseñas.
 * Proporciona métodos estáticos para la generación de hashes de cifrado.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
public class PasswordUtils {

    /**
     * Constructor privado para prevenir la instanciación de la clase utilitaria.
     *
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    private PasswordUtils() {}

    /**
     * Devuelve el hash SHA-256 en hexadecimal, para guardar/comparar contra
     * CONTRASENA.HASH_PASSWORD (las contraseñas nunca se guardan en texto plano).
     *
     * @param texto Cadena en texto plano a convertir en hash.
     * @return {@link String} con la representación en hexadecimal del hash SHA-256.
     * @throws IllegalStateException Si el algoritmo SHA-256 no está disponible en la JVM.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public static String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}