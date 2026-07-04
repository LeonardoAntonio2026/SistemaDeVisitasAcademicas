package com.example.demo.model;

import java.io.Serializable;
public class Visita implements Serializable {
    private int id;
    private String nombreEmpresa;
    private String direccion;
    private String telefono;
    private String correo;
    private String fechaInicio;
    private String fechaTermino;
    private String horaVisita;
    private String objetivo;
    private String areaSolicitante;
    private String docenteResponsable;
    private String celularDocente;
    private String docentesAcompanantes;

    public Visita() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(String fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public String getHoraVisita() {
        return horaVisita;
    }

    public void setHoraVisita(String horaVisita) {
        this.horaVisita = horaVisita;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getAreaSolicitante() {
        return areaSolicitante;
    }

    public void setAreaSolicitante(String areaSolicitante) {
        this.areaSolicitante = areaSolicitante;
    }

    public String getDocenteResponsable() {
        return docenteResponsable;
    }

    public void setDocenteResponsable(String docenteResponsable) {
        this.docenteResponsable = docenteResponsable;
    }

    public String getCelularDocente() {
        return celularDocente;
    }

    public void setCelularDocente(String celularDocente) {
        this.celularDocente = celularDocente;
    }

    public String getDocentesAcompanantes() {
        return docentesAcompanantes;
    }

    public void setDocentesAcompanantes(String docentesAcompanantes) {
        this.docentesAcompanantes = docentesAcompanantes;
    }
}