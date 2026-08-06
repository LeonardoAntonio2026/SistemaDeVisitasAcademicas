package com.example.demo.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validaciones de formulario compartidas por los servlets.
 *
 * El navegador ya valida con required/type/pattern, pero eso solo protege a
 * quien usa el formulario: un POST directo (o el usuario quitando el atributo
 * desde las herramientas del navegador) se lo salta. Por eso cada servlet
 * vuelve a validar aquí antes de tocar la base de datos.
 */
public final class Validador {

    /** Mínimo de caracteres de una contraseña; el mismo número va en el minlength de las vistas. */
    public static final int MIN_CONTRASENA = 8;

    /** Texto de la regla, para no reescribirlo en cada servlet. */
    public static final String REGLA_CONTRASENA =
            "La contraseña debe tener al menos " + MIN_CONTRASENA + " caracteres e incluir letras y números.";

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern DIEZ_DIGITOS = Pattern.compile("^\\d{10}$");

    private Validador() {
    }

    public static String limpiar(String valor) {
        return (valor == null) ? "" : valor.trim();
    }

    public static boolean vacio(String valor) {
        return limpiar(valor).isEmpty();
    }

    public static boolean correoValido(String valor) {
        String correo = limpiar(valor);
        return correo.length() <= 100 && CORREO.matcher(correo).matches();
    }

    /** Teléfonos y celulares de 10 dígitos; se ignoran espacios, guiones y paréntesis. */
    public static boolean telefonoValido(String valor) {
        return DIEZ_DIGITOS.matcher(limpiar(valor).replaceAll("[\\s()+-]", "")).matches();
    }

    /** Devuelve la fecha si el texto viene en formato yyyy-MM-dd; null si no es válida. */
    public static LocalDate fecha(String valor) {
        try {
            return LocalDate.parse(limpiar(valor));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Al menos MIN_CONTRASENA caracteres, con letras y números. */
    public static boolean contrasenaValida(String valor) {
        if (valor == null || valor.length() < MIN_CONTRASENA) {
            return false;
        }
        boolean letra = false;
        boolean digito = false;
        for (char c : valor.toCharArray()) {
            if (Character.isLetter(c)) {
                letra = true;
            } else if (Character.isDigit(c)) {
                digito = true;
            }
        }
        return letra && digito;
    }
}
