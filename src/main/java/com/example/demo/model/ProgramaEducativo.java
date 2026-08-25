package com.example.demo.model;

import java.io.Serializable;

/**
 * Entidad de modelo que representa un grupo del desglose académico de una solicitud de visita.
 * <p>
 * Especifica el programa educativo participante, su cuatrimestre, grupo y la cantidad de estudiantes.
 * </p>
 * <p>
 * <b>Nota sobre persistencia:</b> Por compatibilidad con el esquema de base de datos existente y
 * registros previos, el atributo {@code divisionAcademica} almacena internamente el nombre del
 * <i>programa educativo</i>. Se recomienda el uso de {@link #getPrograma()} y {@link #getDivision()}
 * en la capa de presentación para reflejar con precisión semántica la información.
 * </p>
 *
 * @author Eder Gabriel García Vázquez
 * @since 20/08/2026
 */
public class ProgramaEducativo implements Serializable {

    /** Identificador único del programa educativo en la base de datos. */
    private int idPrograma;

    /** Identificador de la solicitud a la que pertenece este grupo. */
    private int idSolicitud;

    /**
     * Nombre del programa educativo almacenado en la columna histórica {@code DIVISION_ACADEMICA}.
     */
    private String divisionAcademica;

    /** Cuatrimestre que cursa el grupo solicitante. */
    private int cuatrimestre;

    /** Letra o identificador del grupo (ej. "A", "B"). */
    private String grupo;

    /** Cantidad de estudiantes que asistirán a la visita por este grupo. */
    private int noEstudiantes;

    /**
     * Constructor por defecto requerido para la instanciación réflex y frameworks.
     *
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public ProgramaEducativo() {}

    /**
     * Constructor sobrecargado para la creación rápida de un registro de programa educativo.
     *
     * @param divisionAcademica el nombre del programa educativo.
     * @param cuatrimestre el cuatrimestre en curso.
     * @param grupo el grupo asignado.
     * @param noEstudiantes el número de alumnos participantes.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public ProgramaEducativo(String divisionAcademica, int cuatrimestre, String grupo, int noEstudiantes) {
        this.divisionAcademica = divisionAcademica;
        this.cuatrimestre = cuatrimestre;
        this.grupo = grupo;
        this.noEstudiantes = noEstudiantes;
    }

    /**
     * Obtiene el ID del registro de programa educativo.
     *
     * @return el identificador del registro.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public int getIdPrograma() {
        return idPrograma;
    }

    /**
     * Asigna el ID del registro de programa educativo.
     *
     * @param idPrograma el identificador a establecer.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public void setIdPrograma(int idPrograma) {
        this.idPrograma = idPrograma;
    }

    /**
     * Obtiene el ID de la solicitud asociada.
     *
     * @return el identificador de la solicitud padre.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public int getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * Establece el ID de la solicitud asociada.
     *
     * @param idSolicitud el identificador de la solicitud.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * Obtiene el valor directamente mapeado del campo de base de datos {@code divisionAcademica}.
     *
     * @return el valor almacenado en la columna (nombre del programa educativo).
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public String getDivisionAcademica() {
        return divisionAcademica;
    }

    /**
     * Asigna el valor del campo de base de datos {@code divisionAcademica}.
     *
     * @param divisionAcademica el nombre del programa a almacenar.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public void setDivisionAcademica(String divisionAcademica) {
        this.divisionAcademica = divisionAcademica;
    }

    /**
     * Nombre del programa educativo (alias semántico de {@link #getDivisionAcademica()}).
     *
     * @return el nombre del programa educativo.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public String getPrograma() {
        return divisionAcademica;
    }

    /**
     * Establece el nombre del programa educativo (alias semántico de {@link #setDivisionAcademica(String)}).
     *
     * @param programa el nombre del programa educativo.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public void setPrograma(String programa) {
        this.divisionAcademica = programa;
    }

    /**
     * Deduce la sigla de la división académica consultando el {@link CatalogoAcademico}
     * a partir del programa educativo guardado.
     *
     * @return la sigla de la división (ej. "DATID"), o {@code null} si el programa no pertenece al catálogo.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public String getDivision() {
        return CatalogoAcademico.divisionDe(divisionAcademica);
    }

    /**
     * Obtiene la sigla de la división académica formateada para su despliegue visual en vistas o tablas.
     *
     * @return la sigla de la división, o un guion ("—") si no se pudo deducir del catálogo.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public String getDivisionMostrable() {
        String division = getDivision();
        return division != null ? division : "—";
    }

    /**
     * Obtiene el número de cuatrimestre del grupo.
     *
     * @return el cuatrimestre numérico.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public int getCuatrimestre() {
        return cuatrimestre;
    }

    /**
     * Asigna el número de cuatrimestre del grupo.
     *
     * @param cuatrimestre el cuatrimestre a establecer.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public void setCuatrimestre(int cuatrimestre) {
        this.cuatrimestre = cuatrimestre;
    }

    /**
     * Obtiene la letra o identificador del grupo.
     *
     * @return el grupo.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public String getGrupo() {
        return grupo;
    }

    /**
     * Establece el identificador del grupo.
     *
     * @param grupo el grupo a asignar.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    /**
     * Obtiene la cantidad de estudiantes participantes.
     *
     * @return el número de estudiantes.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public int getNoEstudiantes() {
        return noEstudiantes;
    }

    /**
     * Asigna la cantidad de estudiantes participantes.
     *
     * @param noEstudiantes el número de estudiantes.
     * @author Eder Gabriel García Vázquez
     * @since 20/08/2026
     */
    public void setNoEstudiantes(int noEstudiantes) {
        this.noEstudiantes = noEstudiantes;
    }
}