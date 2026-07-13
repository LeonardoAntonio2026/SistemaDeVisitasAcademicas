package com.example.demo.model;

import java.io.Serializable;

/**
 * Reporte post-visita. Se crea automáticamente (estado Pendiente) cuando la
 * solicitud se completa; el docente lo llena después de realizar la visita.
 */
public class Reporte implements Serializable {
    private int idReporte;
    private int idSolicitud;
    private String fecha;
    private String resultados;
    private String observaciones;
    private String fechaCreacion;
    private int idEstado;
    private String motivo;

    // Campos de apoyo para la vista (vienen de joins, no de REPORTE)
    private String nombreEstado;
    private String nombreEmpresaActividad;
    private String lugarDireccion;
    private int idUsuarioSolicitante;
    private String nombreSolicitante;
    private int totalEstudiantes;
    private String fechaSolicitud;

    public Reporte() {}

    public int getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getResultados() {
        return resultados;
    }

    public void setResultados(String resultados) {
        this.resultados = resultados;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getNombreEstado() {
        return nombreEstado;
    }

    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    public String getNombreEmpresaActividad() {
        return nombreEmpresaActividad;
    }

    public void setNombreEmpresaActividad(String nombreEmpresaActividad) {
        this.nombreEmpresaActividad = nombreEmpresaActividad;
    }

    public String getLugarDireccion() {
        return lugarDireccion;
    }

    public void setLugarDireccion(String lugarDireccion) {
        this.lugarDireccion = lugarDireccion;
    }

    public int getIdUsuarioSolicitante() {
        return idUsuarioSolicitante;
    }

    public void setIdUsuarioSolicitante(int idUsuarioSolicitante) {
        this.idUsuarioSolicitante = idUsuarioSolicitante;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public int getTotalEstudiantes() {
        return totalEstudiantes;
    }

    public void setTotalEstudiantes(int totalEstudiantes) {
        this.totalEstudiantes = totalEstudiantes;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}
