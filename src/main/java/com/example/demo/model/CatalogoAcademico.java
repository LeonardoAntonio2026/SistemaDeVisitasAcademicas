package com.example.demo.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catálogo centralizado de divisiones académicas y programas educativos de la UTEZ.
 * <p>
 * Funciona como la fuente única de verdad para el desglose académico de las solicitudes.
 * Evita la captura libre por parte del docente, permitiendo deducir automáticamente la división
 * a partir del programa educativo seleccionado y garantizando la integridad de los datos para
 * la impresión del formato oficial FO-UTEZ-EST-08.
 * </p>
 *
 * @author Eder Gabriel García Vázquez
 * @since 20/08/2026
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

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidades estáticas.
     *
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    private CatalogoAcademico() {}

    /**
     * Registra una división académica en los mapas estáticos de catálogo e índice inverso.
     *
     * @param sigla la sigla identificadora de la división (ej. DACEA).
     * @param nombre el nombre completo descriptivo de la división.
     * @param programas la lista de programas educativos pertenecientes a dicha división.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    private static void registrar(String sigla, String nombre, List<String> programas) {
        NOMBRES.put(sigla, nombre);
        PROGRAMAS.put(sigla, programas);
        for (String programa : programas) {
            DIVISION_POR_PROGRAMA.put(programa, sigla);
        }
    }

    /**
     * Obtiene el mapa que asocia las siglas de las divisiones con sus nombres completos.
     * Utilizado para poblar elementos {@code <option>} y encabezados en las vistas.
     *
     * @return un {@link Map} con las siglas como clave y el nombre completo de la división como valor.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static Map<String, String> getNombres() {
        return NOMBRES;
    }

    /**
     * Obtiene la estructura jerárquica de programas educativos agrupados por división académica.
     *
     * @return un {@link Map} estructurado con las siglas como clave y las listas de programas como valor.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static Map<String, List<String>> getProgramas() {
        return PROGRAMAS;
    }

    /**
     * Retorna una lista plana con la totalidad de los programas educativos registrados en el catálogo,
     * respetando el orden de inserción de las divisiones.
     *
     * @return una {@link List} de cadenas con todos los programas académicos.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static List<String> todosLosProgramas() {
        List<String> todos = new ArrayList<>();
        for (List<String> programas : PROGRAMAS.values()) {
            todos.addAll(programas);
        }
        return todos;
    }

    /**
     * Determina la sigla de la división académica a la que pertenece un programa educativo determinado.
     *
     * @param programa el nombre del programa educativo a consultar.
     * @return las siglas de la división académica correspondiente, o {@code null} si el programa
     *         no existe en el catálogo (por ejemplo, en registros legados).
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static String divisionDe(String programa) {
        return programa != null ? DIVISION_POR_PROGRAMA.get(programa.trim()) : null;
    }

    /**
     * Evalúa si un programa educativo existe formalmente dentro del catálogo vigente.
     *
     * @param programa el nombre del programa académico a verificar.
     * @return {@code true} si el programa se encuentra registrado; {@code false} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static boolean existePrograma(String programa) {
        return divisionDe(programa) != null;
    }

    /**
     * Obtiene el nombre completo de una división académica a partir de sus siglas.
     *
     * @param sigla la sigla identificadora de la división académica.
     * @return el nombre completo registrado, o la misma sigla si no se encuentra en el mapa.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public static String nombreDe(String sigla) {
        return NOMBRES.getOrDefault(sigla, sigla);
    }
}