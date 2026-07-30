package com.example.demo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Solicitud implements Serializable {

    /** Divisiones académicas del formato FO-UTEZ-EST-08, en el orden en que se imprimen. */
    public static final List<String> DIVISIONES = List.of("DACEA", "DATEFI", "DATID", "DAMI");

    private int idSolicitud;
    private int idUsuarioSolicitante;
    private Integer idUsuarioAutoriza;
    private String nombreEmpresaActividad;
    private String lugarDireccion;
    private String telefonoContacto;
    private String correoContacto;
    private String fechaInicio;
    private String objetivo;
    private String areaSolicitante;
    private String docenteResponsable;
    private String celularResponsable;
    private int idEstado;
    private String detallesDecision;
    private String fechaCreacion;

    // Campos de apoyo para la vista (no son columnas de SOLICITUD)
    private String nombreEstado;
    private String estadoReporte;
    private Integer idReporte;
    private String nombreSolicitante;
    private String correoSolicitante;
    private int totalEstudiantes;
    private List<ProgramaEducativo> programas = new ArrayList<>();
    private List<String> asignaturas = new ArrayList<>();
    // Estudiantes por división académica (ESTUDIANTES_DIVISION), siempre con las 4 llaves
    private Map<String, Integer> estudiantesPorDivision = divisionesEnCero();
    // Docentes acompañantes (SOLICITUD_DOCENTE); solo se usan id y nombre
    private List<Usuario> docentesAcompanantes = new ArrayList<>();

    public Solicitud() {}

    /** Mapa con las 4 divisiones en 0, para que la vista siempre encuentre la llave. */
    public static Map<String, Integer> divisionesEnCero() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (String division : DIVISIONES) {
            mapa.put(division, 0);
        }
        return mapa;
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getIdUsuarioSolicitante() {
        return idUsuarioSolicitante;
    }

    public void setIdUsuarioSolicitante(int idUsuarioSolicitante) {
        this.idUsuarioSolicitante = idUsuarioSolicitante;
    }

    public Integer getIdUsuarioAutoriza() {
        return idUsuarioAutoriza;
    }

    public void setIdUsuarioAutoriza(Integer idUsuarioAutoriza) {
        this.idUsuarioAutoriza = idUsuarioAutoriza;
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

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
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

    /** Nombre capturado en el formato; puede no ser el mismo que el del solicitante. */
    public String getDocenteResponsable() {
        return docenteResponsable;
    }

    public void setDocenteResponsable(String docenteResponsable) {
        this.docenteResponsable = docenteResponsable;
    }

    public String getCelularResponsable() {
        return celularResponsable;
    }

    public void setCelularResponsable(String celularResponsable) {
        this.celularResponsable = celularResponsable;
    }

    public int getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    public String getDetallesDecision() {
        return detallesDecision;
    }

    public void setDetallesDecision(String detallesDecision) {
        this.detallesDecision = detallesDecision;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreEstado() {
        return nombreEstado;
    }

    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    /** Estado del reporte de esta solicitud, null si todavía no existe. */
    public String getEstadoReporte() {
        return estadoReporte;
    }

    public void setEstadoReporte(String estadoReporte) {
        this.estadoReporte = estadoReporte;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    public void setCorreoSolicitante(String correoSolicitante) {
        this.correoSolicitante = correoSolicitante;
    }

    public int getTotalEstudiantes() {
        return totalEstudiantes;
    }

    public void setTotalEstudiantes(int totalEstudiantes) {
        this.totalEstudiantes = totalEstudiantes;
    }

    public List<ProgramaEducativo> getProgramas() {
        return programas;
    }

    public void setProgramas(List<ProgramaEducativo> programas) {
        this.programas = programas;
    }

    public List<String> getAsignaturas() {
        return asignaturas;
    }

    public void setAsignaturas(List<String> asignaturas) {
        this.asignaturas = asignaturas;
    }

    public Map<String, Integer> getEstudiantesPorDivision() {
        return estudiantesPorDivision;
    }

    public void setEstudiantesPorDivision(Map<String, Integer> estudiantesPorDivision) {
        this.estudiantesPorDivision = estudiantesPorDivision;
    }

    /** Suma de los estudiantes capturados por división académica. */
    public int getTotalPorDivision() {
        int total = 0;
        for (Integer valor : estudiantesPorDivision.values()) {
            total += (valor != null ? valor : 0);
        }
        return total;
    }

    public List<Usuario> getDocentesAcompanantes() {
        return docentesAcompanantes;
    }

    public void setDocentesAcompanantes(List<Usuario> docentesAcompanantes) {
        this.docentesAcompanantes = docentesAcompanantes;
    }
}
