package com.example.demo.model;

import java.io.Serializable;

/**
 * Modelo de datos que representa un Reporte post-visita.
 * <p>
 * Se crea automáticamente con estado "Pendiente" cuando una solicitud se completa;
 * posteriormente, el docente llena los campos requeridos tras realizar la visita.
 * </p>
 *
 * @author Alan Esteban Zarinana Arizmendi
 * @since 2026-08-25
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

    /**
     * @return el identificador único del reporte
     */
    public int getIdReporte() {
        return idReporte;
    }

    /**
     * @param idReporte identificador único del reporte
     */
    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    /**
     * @return el identificador de la solicitud a la que está vinculado el reporte
     */
    public int getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * @param idSolicitud identificador de la solicitud a la que está vinculado el reporte
     */
    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * @return la fecha de realización del reporte o visita
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * @param fecha fecha de realización del reporte o visita
     */
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    /**
     * @return los resultados obtenidos durante la visita
     */
    public String getResultados() {
        return resultados;
    }

    /**
     * @param resultados resultados obtenidos durante la visita
     */
    public void setResultados(String resultados) {
        this.resultados = resultados;
    }

    /**
     * @return las observaciones adicionales capturadas por el docente
     */
    public String getObservaciones() {
        return observaciones;
    }

    /**
     * @param observaciones observaciones adicionales capturadas por el docente
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * @return la fecha de creación del registro en el sistema
     */
    public String getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * @param fechaCreacion fecha de creación del registro en el sistema
     */
    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * @return el identificador del estado actual del reporte
     */
    public int getIdEstado() {
        return idEstado;
    }

    /**
     * @param idEstado identificador del estado actual del reporte
     */
    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    /**
     * @return el motivo en caso de rechazo o aclaración del estado
     */
    public String getMotivo() {
        return motivo;
    }

    /**
     * @param motivo motivo en caso de rechazo o aclaración del estado
     */
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    /**
     * @return el nombre del estado original del reporte, tal como está en la base de datos
     */
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
     * @return el nombre del estado formateado para la interfaz de usuario
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
     * @return el nombre de la clase CSS del estado
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

    /**
     * @param nombreEstado nombre del estado original del reporte
     */
    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    /**
     * @return el nombre de la empresa o actividad asociada a la visita
     */
    public String getNombreEmpresaActividad() {
        return nombreEmpresaActividad;
    }

    /**
     * @param nombreEmpresaActividad nombre de la empresa o actividad asociada a la visita
     */
    public void setNombreEmpresaActividad(String nombreEmpresaActividad) {
        this.nombreEmpresaActividad = nombreEmpresaActividad;
    }

    /**
     * @return la dirección o lugar donde se realizó la visita
     */
    public String getLugarDireccion() {
        return lugarDireccion;
    }

    /**
     * @param lugarDireccion dirección o lugar donde se realizó la visita
     */
    public void setLugarDireccion(String lugarDireccion) {
        this.lugarDireccion = lugarDireccion;
    }

    /**
     * @return el identificador del usuario docente que realizó la solicitud
     */
    public int getIdUsuarioSolicitante() {
        return idUsuarioSolicitante;
    }

    /**
     * @param idUsuarioSolicitante identificador del usuario docente que realizó la solicitud
     */
    public void setIdUsuarioSolicitante(int idUsuarioSolicitante) {
        this.idUsuarioSolicitante = idUsuarioSolicitante;
    }

    /**
     * @return el nombre completo del docente solicitante
     */
    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    /**
     * @param nombreSolicitante nombre completo del docente solicitante
     */
    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    /**
     * @return el total de estudiantes que asistieron o participan en la visita
     */
    public int getTotalEstudiantes() {
        return totalEstudiantes;
    }

    /**
     * @param totalEstudiantes total de estudiantes que asistieron o participan en la visita
     */
    public void setTotalEstudiantes(int totalEstudiantes) {
        this.totalEstudiantes = totalEstudiantes;
    }

    /**
     * @return el correo electrónico del docente solicitante
     */
    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    /**
     * @param correoSolicitante correo electrónico del docente solicitante
     */
    public void setCorreoSolicitante(String correoSolicitante) {
        this.correoSolicitante = correoSolicitante;
    }

    /**
     * @return la fecha en la que se creó la solicitud original
     */
    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    /**
     * @param fechaSolicitud fecha en la que se creó la solicitud original
     */
    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}