package com.example.demo.model;

import java.io.Serializable;

/**
 * Un grupo del desglose de la solicitud: qué programa educativo va, de qué
 * cuatrimestre, qué grupo y cuántos estudiantes.
 *
 * Ojo con el nombre de la columna: DIVISION_ACADEMICA guarda el PROGRAMA
 * educativo (así nació el formulario) y la división académica se deduce del
 * catálogo. Se deja tal cual para no tocar la base de datos ni las solicitudes
 * ya capturadas; usa {@link #getPrograma()} y {@link #getDivision()} en las
 * vistas, que sí dicen lo que son.
 */
public class ProgramaEducativo implements Serializable {
    private int idPrograma;
    private int idSolicitud;
    private String divisionAcademica;
    private int cuatrimestre;
    private String grupo;
    private int noEstudiantes;

    public ProgramaEducativo() {}

    public ProgramaEducativo(String divisionAcademica, int cuatrimestre, String grupo, int noEstudiantes) {
        this.divisionAcademica = divisionAcademica;
        this.cuatrimestre = cuatrimestre;
        this.grupo = grupo;
        this.noEstudiantes = noEstudiantes;
    }

    public int getIdPrograma() {
        return idPrograma;
    }

    public void setIdPrograma(int idPrograma) {
        this.idPrograma = idPrograma;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getDivisionAcademica() {
        return divisionAcademica;
    }

    public void setDivisionAcademica(String divisionAcademica) {
        this.divisionAcademica = divisionAcademica;
    }

    /** Nombre del programa educativo (lo que de verdad guarda la columna). */
    public String getPrograma() {
        return divisionAcademica;
    }

    public void setPrograma(String programa) {
        this.divisionAcademica = programa;
    }

    /** División académica del programa; null si el programa no está en el catálogo. */
    public String getDivision() {
        return CatalogoAcademico.divisionDe(divisionAcademica);
    }

    /** División para mostrar: las siglas, o un guion si el programa es texto viejo. */
    public String getDivisionMostrable() {
        String division = getDivision();
        return division != null ? division : "—";
    }

    public int getCuatrimestre() {
        return cuatrimestre;
    }

    public void setCuatrimestre(int cuatrimestre) {
        this.cuatrimestre = cuatrimestre;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public int getNoEstudiantes() {
        return noEstudiantes;
    }

    public void setNoEstudiantes(int noEstudiantes) {
        this.noEstudiantes = noEstudiantes;
    }
}
