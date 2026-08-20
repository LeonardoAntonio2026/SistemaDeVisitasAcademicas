package com.example.demo.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utilidad centralizada para la validación y sanitización de datos de entrada en formularios del lado del servidor.
 * <p>
 * Proporciona una capa defensiva de seguridad contra peticiones manipuladas o directas (evadiendo
 * las reglas HTML5 del navegador), verificando la sintaxis y formato de correos, teléfonos, fechas y contraseñas.
 * </p>
 *
 * @author Eder Gabriel García Vázquez
 * @since 20/08/2026
 */
public final class Validador {

    /** Mínimo de caracteres requeridos para una contraseña segura; alineado con el atributo {@code minlength} en vistas. */
    public static final int MIN_CONTRASENA = 8;

    /** Texto descriptivo de la regla de contraseña para retroalimentación uniforme en servlets y vistas. */
    public static final String REGLA_CONTRASENA =
            "La contraseña debe tener al menos " + MIN_CONTRASENA + " caracteres e incluir letras y números.";

    /** Expresión regular compilada para la validación de formato de correo electrónico. */
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    /** Expresión regular compilada para verificar secuencias de exactamente 10 dígitos numéricos. */
    private static final Pattern DIEZ_DIGITOS = Pattern.compile("^\\d{10}$");

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidades estáticas.
     *
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    private Validador() {
    }

    /**
     * Sanitiza una cadena de texto eliminando espacios en blanco al inicio y al final.
     *
     * @param valor la cadena de texto a limpiar.
     * @return la cadena recortada, o una cadena vacía ({@code ""}) si el parámetro es {@code null}.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static String limpiar(String valor) {
        return (valor == null) ? "" : valor.trim();
    }

    /**
     * Evalúa si una cadena de texto es nula o contiene únicamente espacios en blanco.
     *
     * @param valor el texto a evaluar.
     * @return {@code true} si la cadena está vacía tras ser sanitizada; {@code false} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static boolean vacio(String valor) {
        return limpiar(valor).isEmpty();
    }

    /**
     * Valida si la cadena proporcionada cumple con una sintaxis correcta de dirección de correo electrónico
     * y no excede la longitud máxima permitida de 100 caracteres.
     *
     * @param valor la dirección de correo a evaluar.
     * @return {@code true} si la dirección es válida y no supera los 100 caracteres; {@code false} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static boolean correoValido(String valor) {
        String correo = limpiar(valor);
        return correo.length() <= 100 && CORREO.matcher(correo).matches();
    }

    /**
     * Valida que un número telefónico o celular conste de exactamente 10 dígitos numéricos.
     * Ignores caracteres de formato comunes como espacios, paréntesis, guiones y el signo más (+).
     *
     * @param valor la cadena con el número telefónico.
     * @return {@code true} si contiene exactamente 10 dígitos numéricos válidos; {@code false} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static boolean telefonoValido(String valor) {
        return DIEZ_DIGITOS.matcher(limpiar(valor).replaceAll("[\\s()+-]", "")).matches();
    }

    /**
     * Convierte una cadena de texto a un objeto {@link LocalDate} si cumple con el formato estándar ISO (yyyy-MM-dd).
     *
     * @param valor el texto con la representación de la fecha.
     * @return el objeto {@link LocalDate} resultante, o {@code null} si el formato es inválido o el valor es nulo.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static LocalDate fecha(String valor) {
        try {
            return LocalDate.parse(limpiar(valor));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Evalúa si una contraseña cumple con los estándares mínimos de complejidad requeridos.
     * <p>
     * Criterios: Al menos {@value #MIN_CONTRASENA} caracteres de longitud y debe contener
     * de forma combinada al menos una letra y un dígito numérico.
     * </p>
     *
     * @param valor la cadena de contraseña a verificar.
     * @return {@code true} si la contraseña cumple con la política de seguridad; {@code false} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
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