package com.example.demo.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Fechas escritas con letra, como las piden los formatos oficiales
 * ("12 de agosto de 2026"). La base guarda y las vistas manejan las fechas en
 * yyyy-MM-dd, que sirve para ordenar y para los &lt;input type="date"&gt;, pero
 * no es como se redacta un documento que se firma.
 * <p>
 * Los meses van en una tabla y no en un DateTimeFormatter con Locale("es")
 * porque entonces el nombre del mes dependería del locale que tenga instalado
 * el servidor, y el Tomcat de producción no tiene por qué estar en español.
 *
 * @author Alan Esteban Zarinana Arizmendi
 * @since 2026-08-25
 */
public final class FechaTexto {

    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    private FechaTexto() {
    }

    /**
     * "2026-08-12" &rarr; "12 de agosto de 2026". Cadena vacía si la fecha no sirve.
     *
     * @param iso fecha en formato ISO (yyyy-MM-dd)
     * @return la fecha en formato largo con letra, o cadena vacía si {@code iso} es inválida o nula
     */
    public static String largo(String iso) {
        LocalDate fecha = parse(iso);
        return (fecha == null) ? "" : largo(fecha);
    }

    /**
     * La fecha de hoy con letra, para el encabezado de los documentos.
     *
     * @return la fecha actual en formato largo con letra
     */
    public static String hoyLargo() {
        return largo(LocalDate.now());
    }

    /**
     * Formatea una fecha ya parseada al formato largo con letra.
     *
     * @param fecha fecha a formatear
     * @return la fecha en formato "día de mes de año"
     */
    private static String largo(LocalDate fecha) {
        return fecha.getDayOfMonth() + " de " + MESES[fecha.getMonthValue() - 1] + " de " + fecha.getYear();
    }

    /**
     * Convierte un texto ISO a {@link LocalDate}, sin lanzar excepción si no sirve.
     *
     * @param iso fecha en formato ISO (yyyy-MM-dd)
     * @return la fecha parseada, o {@code null} si {@code iso} es nula, vacía o inválida
     */
    private static LocalDate parse(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(iso.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}