package com.example.demo.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Fechas escritas con letra, como las piden los formatos oficiales
 * ("12 de agosto de 2026"). La base guarda y las vistas manejan las fechas en
 * yyyy-MM-dd, que sirve para ordenar y para los &lt;input type="date"&gt;, pero
 * no es como se redacta un documento que se firma.
 *
 * Los meses van en una tabla y no en un DateTimeFormatter con Locale("es")
 * porque entonces el nombre del mes dependería del locale que tenga instalado
 * el servidor, y el Tomcat de producción no tiene por qué estar en español.
 */
public final class FechaTexto {

    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    private FechaTexto() {
    }

    /** "2026-08-12" &rarr; "12 de agosto de 2026". Cadena vacía si la fecha no sirve. */
    public static String largo(String iso) {
        LocalDate fecha = parse(iso);
        return (fecha == null) ? "" : largo(fecha);
    }

    /** La fecha de hoy con letra, para el encabezado de los documentos. */
    public static String hoyLargo() {
        return largo(LocalDate.now());
    }

    private static String largo(LocalDate fecha) {
        return fecha.getDayOfMonth() + " de " + MESES[fecha.getMonthValue() - 1] + " de " + fecha.getYear();
    }

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
