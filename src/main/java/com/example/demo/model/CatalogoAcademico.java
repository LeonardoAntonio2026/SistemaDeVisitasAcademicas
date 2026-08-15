package com.example.demo.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catálogo de divisiones académicas y programas educativos de la UTEZ.
 *
 * Es la fuente única de verdad del desglose de la solicitud: el docente ya no
 * escribe a mano el nombre del programa ni cuadra los totales por división, los
 * elige de una lista y la división se deduce del programa.
 */
public final class CatalogoAcademico {

    /** Siglas de las divisiones, en el orden en que se imprimen en el FO-UTEZ-EST-08. */
    public static final List<String> DIVISIONES = List.of("DACEA", "DATEFI", "DATID", "DAMI");

    /** Nombre completo de cada división, para mostrarlo junto a las siglas. */
    private static final Map<String, String> NOMBRES = new LinkedHashMap<>();

    /** Programas educativos que ofrece cada división. */
    private static final Map<String, List<String>> PROGRAMAS = new LinkedHashMap<>();

    /** Índice inverso programa -> división, para deducir la división al leer de la BD. */
    private static final Map<String, String> DIVISION_POR_PROGRAMA = new LinkedHashMap<>();

    static {
        registrar("DACEA", "División Académica de Ciencias Económico Administrativas", List.of(
                "TSU en Administración área Capital Humano",
                "TSU en Desarrollo de Negocios área Mercadotecnia",
                "Licenciatura en Innovación de Negocios y Mercadotecnia",
                "Licenciatura en Gestión de Capital Humano"));

        registrar("DATEFI", "División Académica de Terapia Física", List.of(
                "TSU en Terapia Física área Rehabilitación",
                "TSU en Terapia Física área Turismo de Salud y Bienestar",
                "Licenciatura en Terapia Física"));

        registrar("DATID", "División Académica de Tecnologías de la Información y Diseño", List.of(
                "TSU en Tecnologías de la Información área Desarrollo de Software Multiplataforma",
                "TSU en Tecnologías de la Información área Infraestructura de Redes Digitales",
                "TSU en Diseño Digital",
                "TSU en Diseño y Moda Industrial área Producción",
                "Ingeniería en Tecnologías de la Información",
                "Ingeniería en Diseño Textil y Moda"));

        registrar("DAMI", "División Académica de Mecánica Industrial", List.of(
                "TSU en Procesos Industriales área Manufactura",
                "TSU en Mantenimiento área Industrial",
                "TSU en Mecatrónica área Automatización",
                "TSU en Nanotecnología área Materiales",
                "Ingeniería Industrial",
                "Ingeniería en Mecatrónica",
                "Ingeniería en Mantenimiento Industrial",
                "Ingeniería en Nanotecnología",
                "Licenciatura en Diseño Digital"));
    }

    private CatalogoAcademico() {}

    private static void registrar(String sigla, String nombre, List<String> programas) {
        NOMBRES.put(sigla, nombre);
        PROGRAMAS.put(sigla, programas);
        for (String programa : programas) {
            DIVISION_POR_PROGRAMA.put(programa, sigla);
        }
    }

    /** Mapa sigla -> nombre completo (para los <option> y los encabezados). */
    public static Map<String, String> getNombres() {
        return NOMBRES;
    }

    /** Mapa sigla -> programas educativos de esa división. */
    public static Map<String, List<String>> getProgramas() {
        return PROGRAMAS;
    }

    /** Todos los programas del catálogo, agrupados por división en el orden del mapa. */
    public static List<String> todosLosProgramas() {
        List<String> todos = new ArrayList<>();
        for (List<String> programas : PROGRAMAS.values()) {
            todos.addAll(programas);
        }
        return todos;
    }

    /**
     * División a la que pertenece un programa educativo, o null si el nombre no
     * está en el catálogo (solicitudes viejas capturadas con texto libre).
     */
    public static String divisionDe(String programa) {
        return programa != null ? DIVISION_POR_PROGRAMA.get(programa.trim()) : null;
    }

    /** true si el programa existe tal cual en el catálogo. */
    public static boolean existePrograma(String programa) {
        return divisionDe(programa) != null;
    }

    /** Nombre completo de la división; devuelve las siglas si no la conocemos. */
    public static String nombreDe(String sigla) {
        return NOMBRES.getOrDefault(sigla, sigla);
    }
}
