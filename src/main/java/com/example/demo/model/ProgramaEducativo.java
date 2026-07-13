package com.example.demo.model;

import java.io.Serializable;

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
