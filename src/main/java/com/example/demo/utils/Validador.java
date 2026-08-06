package com.example.demo.utils; // AJUSTA esto al paquete real donde lo coloques (ej. com.sgva.util)

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Utilidades de validación para el lado del servidor.
 *
 * Todos los métodos son estáticos y tolerantes a null: un valor null nunca
 * provoca NullPointerException, simplemente se considera inválido/vacío.
 *
 * La clase es final y con constructor privado porque no tiene estado:
 * no hay razón para instanciarla ni heredarla.
 */
public final class Validador {

    private Validador() {
        // Clase de utilidades: no se instancia.
    }

    // ------------------------------------------------------------------
    // Mensajes reutilizables (para mostrarlos igual en todas las pantallas)
    // ------------------------------------------------------------------

    /** Regla que debe cumplir una contraseña. Se muestra tal cual al usuario. */
    public static final String REGLA_CONTRASENA =
            "La contraseña debe tener al menos 8 caracteres e incluir una mayúscula, "
                    + "una minúscula y un número, sin espacios.";

    /** Regla de teléfono, por si la necesitas en la vista. */
    public static final String REGLA_TELEFONO =
            "El teléfono debe tener 10 dígitos (puedes incluir la lada +52).";

    /** Regla de correo. */
    public static final String REGLA_CORREO =
            "Escribe un correo electrónico válido, por ejemplo: nombre@dominio.com";

    // ------------------------------------------------------------------
    // Constantes internas
    // ------------------------------------------------------------------

    /** Longitud mínima de contraseña. */
    public static final int LARGO_MIN_CONTRASENA = 8;

    /** Longitud máxima de contraseña (evita hashes absurdamente largos). */
    public static final int LARGO_MAX_CONTRASENA = 64;

    /**
     * Patrón de correo pragmático: parte local sin espacios, arroba, dominio con
     * al menos un punto y TLD de 2+ letras. No pretende cubrir el RFC 5322
     * completo (nadie lo hace en producción); cubre lo que un usuario real teclea.
     */
    private static final Pattern PATRON_CORREO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Solo letras (incluye acentos y ñ), espacios, apóstrofes y guiones. */
    private static final Pattern PATRON_SOLO_LETRAS = Pattern.compile(
            "^[\\p{L} '\\-]+$");

    /** Formato de fecha que manda el input type=date del navegador. */
    private static final DateTimeFormatter FORMATO_ISO =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Límite razonable para el largo de un correo (estándar: 254). */
    private static final int LARGO_MAX_CORREO = 254;

    // ------------------------------------------------------------------
    // Básicos: vacío y limpieza
    // ------------------------------------------------------------------

    /**
     * Indica si un texto está ausente para efectos prácticos:
     * null, cadena vacía o solo espacios en blanco.
     */
    public static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    /**
     * Normaliza un texto antes de guardarlo o compararlo:
     * quita espacios al inicio y al final, y colapsa los espacios internos
     * repetidos en uno solo. Un null se convierte en cadena vacía, así que
     * el resultado nunca es null.
     */
    public static String limpiar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().replaceAll("\\s+", " ");
    }

    /**
     * Igual que limpiar(), pero devuelve null cuando el resultado queda vacío.
     * Útil para columnas opcionales donde prefieres NULL en lugar de ''.
     */
    public static String limpiarONull(String valor) {
        String limpio = limpiar(valor);
        return limpio.isEmpty() ? null : limpio;
    }

    // ------------------------------------------------------------------
    // Correo
    // ------------------------------------------------------------------

    /**
     * Valida un correo electrónico. Se limpia antes de evaluar, así que
     * " juan@utez.edu.mx " se considera válido.
     */
    public static boolean correoValido(String correo) {
        String limpio = limpiar(correo);
        if (limpio.isEmpty() || limpio.length() > LARGO_MAX_CORREO) {
            return false;
        }
        return PATRON_CORREO.matcher(limpio).matches();
    }

    /**
     * Valida que el correo pertenezca a un dominio institucional concreto.
     * Ejemplo: correoDeDominio(correo, "utez.edu.mx")
     */
    public static boolean correoDeDominio(String correo, String dominio) {
        if (!correoValido(correo) || vacio(dominio)) {
            return false;
        }
        return limpiar(correo).toLowerCase().endsWith("@" + limpiar(dominio).toLowerCase());
    }

    // ------------------------------------------------------------------
    // Teléfono
    // ------------------------------------------------------------------

    /**
     * Valida un número telefónico mexicano. Acepta separadores comunes
     * (espacios, guiones, paréntesis, puntos) y la lada +52 opcional:
     *
     *   "7771234567"      -> válido
     *   "777 123 45 67"   -> válido
     *   "+52 777 1234567" -> válido
     *   "(777) 123-4567"  -> válido
     *   "12345"           -> inválido
     */
    public static boolean telefonoValido(String telefono) {
        if (vacio(telefono)) {
            return false;
        }
        String digitos = soloDigitos(telefono);

        // Quita la lada de país si viene incluida.
        if (digitos.length() == 12 && digitos.startsWith("52")) {
            digitos = digitos.substring(2);
        } else if (digitos.length() == 13 && digitos.startsWith("521")) {
            digitos = digitos.substring(3);
        }

        return digitos.length() == 10 && !digitos.startsWith("0");
    }

    /**
     * Devuelve el teléfono en formato canónico de 10 dígitos, listo para
     * guardarse en la base. Si el número no es válido, devuelve null.
     */
    public static String normalizarTelefono(String telefono) {
        if (!telefonoValido(telefono)) {
            return null;
        }
        String digitos = soloDigitos(telefono);
        return digitos.substring(digitos.length() - 10);
    }

    /** Extrae únicamente los dígitos de un texto. */
    private static String soloDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    // ------------------------------------------------------------------
    // Fechas
    // ------------------------------------------------------------------

    /**
     * Convierte una fecha recibida como texto en formato ISO (uuuu-MM-dd),
     * que es lo que envía un &lt;input type="date"&gt;.
     *
     * Devuelve null si el texto está vacío o no representa una fecha real,
     * así que sirve como validación y conversión en un solo paso:
     *
     *     LocalDate inicio = Validador.fecha(s.getFechaInicio());
     *     if (inicio == null) { ... }
     *
     * La validación es estricta: "2026-02-30" devuelve null porque ese día
     * no existe, en lugar de "corregirse" solo al 2 de marzo.
     */
    public static LocalDate fecha(String valor) {
        String limpio = limpiar(valor);
        if (limpio.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(limpio, FORMATO_ISO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convierte una java.sql.Date / java.util.Date del DAO a LocalDate.
     * Devuelve null si el valor es null, para que el código que la consume
     * se vea igual que el de la versión con String.
     */
    public static LocalDate fecha(java.util.Date valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof java.sql.Date) {
            return ((java.sql.Date) valor).toLocalDate();
        }
        return valor.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    /** Convierte un LocalDate a texto ISO para rellenar un &lt;input type="date"&gt;. */
    public static String fechaATexto(LocalDate valor) {
        return valor == null ? "" : valor.format(FORMATO_ISO);
    }

    /**
     * Verifica que un rango de fechas tenga sentido: ambas válidas y la
     * fecha de inicio no posterior a la de fin. Un mismo día es válido.
     */
    public static boolean rangoFechasValido(String inicio, String fin) {
        LocalDate d1 = fecha(inicio);
        LocalDate d2 = fecha(fin);
        return d1 != null && d2 != null && !d1.isAfter(d2);
    }

    /** Verifica que la fecha sea hoy o posterior (para solicitudes nuevas). */
    public static boolean fechaNoPasada(String valor) {
        LocalDate d = fecha(valor);
        return d != null && !d.isBefore(LocalDate.now());
    }

    /** Verifica que la fecha no esté en el futuro (para fechas de nacimiento, etc.). */
    public static boolean fechaNoFutura(String valor) {
        LocalDate d = fecha(valor);
        return d != null && !d.isAfter(LocalDate.now());
    }

    // ------------------------------------------------------------------
    // Contraseña
    // ------------------------------------------------------------------

    /**
     * Valida una contraseña contra REGLA_CONTRASENA: mínimo 8 caracteres,
     * al menos una mayúscula, una minúscula y un dígito, sin espacios.
     *
     * Ojo: aquí NO se aplica limpiar(), porque los espacios son parte de la
     * contraseña y recortarlos cambiaría lo que el usuario escribió.
     */
    public static boolean contrasenaValida(String contrasena) {
        if (contrasena == null) {
            return false;
        }
        int largo = contrasena.length();
        if (largo < LARGO_MIN_CONTRASENA || largo > LARGO_MAX_CONTRASENA) {
            return false;
        }

        boolean mayuscula = false;
        boolean minuscula = false;
        boolean digito = false;

        for (int i = 0; i < largo; i++) {
            char c = contrasena.charAt(i);
            if (Character.isWhitespace(c)) {
                return false;
            }
            if (Character.isUpperCase(c)) {
                mayuscula = true;
            } else if (Character.isLowerCase(c)) {
                minuscula = true;
            } else if (Character.isDigit(c)) {
                digito = true;
            }
        }

        return mayuscula && minuscula && digito;
    }

    /**
     * Compara dos capturas de contraseña (campo y confirmación).
     * Distingue mayúsculas y minúsculas, como debe ser.
     */
    public static boolean coinciden(String a, String b) {
        return a != null && a.equals(b);
    }

    /**
     * Compara dos correos ignorando mayúsculas y espacios sobrantes,
     * porque "Juan@Utez.mx" y "juan@utez.mx " son el mismo buzón.
     */
    public static boolean correosCoinciden(String a, String b) {
        return limpiar(a).equalsIgnoreCase(limpiar(b)) && !limpiar(a).isEmpty();
    }

    // ------------------------------------------------------------------
    // Texto y números
    // ------------------------------------------------------------------

    /** Verifica que el texto (ya limpio) no exceda el largo de la columna. */
    public static boolean largoValido(String valor, int maxLargo) {
        return limpiar(valor).length() <= maxLargo;
    }

    /** Verifica que el texto contenga solo letras, espacios, guiones y apóstrofes. */
    public static boolean soloLetras(String valor) {
        String limpio = limpiar(valor);
        return !limpio.isEmpty() && PATRON_SOLO_LETRAS.matcher(limpio).matches();
    }

    /** Verifica que el texto sea un número entero. */
    public static boolean enteroValido(String valor) {
        return aEntero(valor) != null;
    }

    /**
     * Verifica que el texto sea un entero dentro del rango indicado (inclusivo).
     * Útil para "número de alumnos", "duración en horas", etc.
     */
    public static boolean enteroEnRango(String valor, int min, int max) {
        Integer n = aEntero(valor);
        return n != null && n >= min && n <= max;
    }

    /**
     * Convierte un texto a Integer, o devuelve null si no es un entero válido.
     * Evita tener que envolver Integer.parseInt en try/catch en cada servlet.
     */
    public static Integer aEntero(String valor) {
        String limpio = limpiar(valor);
        if (limpio.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Igual que aEntero pero con valor por defecto en lugar de null. */
    public static int aEntero(String valor, int porDefecto) {
        Integer n = aEntero(valor);
        return n == null ? porDefecto : n;
    }
}