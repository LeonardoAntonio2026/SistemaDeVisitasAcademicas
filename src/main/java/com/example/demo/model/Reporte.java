package com.example.demo.model;

import java.io.Serializable;

/**
 * Modelo de datos que representa un Reporte post-visita.
 * <p>
 * Se crea automáticamente con estado "Pendiente" cuando una solicitud se completa;
 * posteriormente, el docente llena los campos requeridos tras realizar la visita.
 * </p>
 *
 * @author Alan Esteban Zariñana Arizmendi
 * @version 1.0
 * @since 25/08/2026
 */
public class Reporte implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Atributos de la tabla REPORTE ---

    /** Identificador único del reporte. */
    private int idReporte;

    /** Identificador de la solicitud a la que está vinculado el reporte. */
    private int idSolicitud;

    /** Fecha de realización del reporte o visita. */
    private String fecha;

    /** Resultados obtenidos durante la visita. */
    private String resultados;

    /** Observaciones adicionales capturadas por el docente. */
    private String observaciones;

    /** Fecha de creación del registro en el sistema. */
    private String fechaCreacion;

    /** Identificador del estado actual del reporte. */
    private int idEstado;

    /** Motivo en caso de rechazo o aclaración del estado. */
    private String motivo;

    // --- Campos de apoyo para la vista (Obtenidos vía JOINs, no persisten en la tabla REPORTE) ---

    /** Nombre del estado original del reporte. */
    private String nombreEstado;

    /** Nombre de la empresa o actividad asociada a la visita. */
    private String nombreEmpresaActividad;

    /** Dirección o lugar donde se realizó la visita. */
    private String lugarDireccion;

    /** Identificador del usuario docente que realizó la solicitud. */
    private int idUsuarioSolicitante;

    /** Nombre completo del docente solicitante. */
    private String nombreSolicitante;

    /** Correo electrónico del docente solicitante. */
    private String correoSolicitante;

    /** Total de estudiantes que asistieron o participan en la visita. */
    private int totalEstudiantes;

    /** Fecha en la que se creó la solicitud original. */
    private String fechaSolicitud;

    /**
     * Constructor por defecto de la clase.
     */
    public Reporte() {}

    // --- Getters y Setters ---

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

    /**
     * Mapea el estado registrado en la base de datos a un texto comprensible para el usuario.
     * <p>
     * Estandariza la nomenclatura con "Reporte" como prefijo para distinguirlo visualmente
     * de las solicitudes y mantiene consistencia con {@code Solicitud.getEstadoLegible()}.
     * </p>
     *
     * @return El nombre del estado formateado para la interfaz de usuario.
     */
    public String getEstadoLegible() {
        if ("Pendiente".equalsIgnoreCase(nombreEstado)) {
            return "Reporte por completar";
        }
        if ("Completado".equalsIgnoreCase(nombreEstado)) {
            return "Reporte en revisión";
        }
        if ("Aprobado".equalsIgnoreCase(nombreEstado)) {
            return "Reporte aprobado";
        }
        if ("Rechazado".equalsIgnoreCase(nombreEstado)) {
            return "Reporte rechazado";
        }
        return nombreEstado;
    }

    /**
     * Obtiene el sufijo CSS correspondiente a la etiqueta de estado (.estado-...)
     * para la renderización de badges en la vista.
     *
     * @return Nombre de la clase CSS del estado.
     */
    public String getClaseEstado() {
        if ("Pendiente".equalsIgnoreCase(nombreEstado)) {
            return "reporte-pendiente";
        }
        if ("Completado".equalsIgnoreCase(nombreEstado)) {
            return "en-revision";
        }
        return "Aprobado".equalsIgnoreCase(nombreEstado) ? "aprobado" : "rechazado";
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

    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    public void setCorreoSolicitante(String correoSolicitante) {
        this.correoSolicitante = correoSolicitante;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}