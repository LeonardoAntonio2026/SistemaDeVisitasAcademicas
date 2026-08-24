package com.example.demo.model;

import com.example.demo.utils.FechaTexto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solicitud implements Serializable {

    /** Divisiones académicas del formato FO-UTEZ-EST-08, en el orden en que se imprimen. */
    public static final List<String> DIVISIONES = CatalogoAcademico.DIVISIONES;

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
    private String nombreAutoriza;
    private int totalEstudiantes;
    private List<ProgramaEducativo> programas = new ArrayList<>();
    private List<String> asignaturas = new ArrayList<>();
    // Estudiantes por división académica; no se guarda, se suma de los programas
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

    /**
     * Estado tal como se le muestra al usuario. El nombre que guarda la base
     * describe la fila, no el trámite: "Pendiente" es una solicitud que el
     * docente todavía no envía, y "Completada" solo significa que ya subió la
     * carta responsiva, con el reporte de la visita aún por hacer. Por eso el
     * estado visible de una solicitud cerrada depende del reporte.
     *
     * Todo estado que hable del reporte empieza con la palabra "Reporte" y usa
     * el mismo texto que Reporte.getEstadoLegible(), para que la misma visita
     * no se llame distinto en Reportes y en el Histórico.
     */
    public String getEstadoLegible() {
        if ("Pendiente".equalsIgnoreCase(nombreEstado)) {
            return "Sin enviar";
        }
        if (!"Completada".equalsIgnoreCase(nombreEstado)) {
            return nombreEstado; // En revisión / Aprobada / Rechazada se leen bien
        }
        if ("Aprobado".equalsIgnoreCase(estadoReporte)) {
            return "Finalizada";
        }
        if ("Rechazado".equalsIgnoreCase(estadoReporte)) {
            return "Reporte rechazado";
        }
        if ("Completado".equalsIgnoreCase(estadoReporte)) {
            return "Reporte en revisión";
        }
        return "Reporte por completar";
    }

    /** Sufijo de la clase CSS del badge (.estado-…) que corresponde al estado visible. */
    public String getClaseEstado() {
        switch (getEstadoLegible()) {
            case "Sin enviar":          return "sin-enviar";
            case "En revisión":         return "en-revision";
            case "Aprobada":            return "aprobada";
            case "Rechazada":           return "rechazada";
            case "Finalizada":          return "finalizada";
            case "Reporte rechazado":   return "reporte-rechazado";
            case "Reporte en revisión": return "en-revision"; // mismo amarillo que "En revisión"
            default:                    return "reporte-pendiente"; // "Reporte por completar"
        }
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

    /** Quien aprobó la solicitud; null mientras Estadías no la haya revisado. */
    public String getNombreAutoriza() {
        return nombreAutoriza;
    }

    public void setNombreAutoriza(String nombreAutoriza) {
        this.nombreAutoriza = nombreAutoriza;
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

    /**
     * Rehace el desglose por división sumando los grupos capturados. El docente
     * no captura estas cifras: elige el programa educativo de cada grupo y la
     * división sale del catálogo, así que los dos totales nunca se desfasan.
     * No se persiste: se recalcula al leer la solicitud, porque guardar una
     * suma que ya se puede derivar de PROGRAMA_EDUCATIVO solo la deja
     * desfasarse (así pasó con las solicitudes viejas de texto libre).
     */
    public void recalcularEstudiantesPorDivision() {
        Map<String, Integer> mapa = divisionesEnCero();
        for (ProgramaEducativo p : programas) {
            String division = p.getDivision();
            if (division == null) {
                continue; // Programa fuera del catálogo (solicitudes viejas)
            }
            mapa.merge(division, Math.max(0, p.getNoEstudiantes()), Integer::sum);
        }
        this.estudiantesPorDivision = mapa;
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

    // ----- Textos que arman los documentos oficiales -----

    /**
     * Fecha de la visita con letra ("12 de agosto de 2026"), como se redacta en
     * la carta responsiva y en el oficio; en yyyy-MM-dd no se puede leer ahí.
     */
    public String getFechaInicioEnLetra() {
        return FechaTexto.largo(fechaInicio);
    }

    /**
     * Programas educativos que participan, sin repetir, para el hueco
     * "estudiantes del programa educativo de ______" de la carta responsiva.
     * Una visita puede llevar varios grupos del mismo programa.
     */
    public String getProgramasEnLetra() {
        Set<String> nombres = new LinkedHashSet<>();
        for (ProgramaEducativo p : programas) {
            if (p.getPrograma() != null && !p.getPrograma().isBlank()) {
                nombres.add(p.getPrograma());
            }
        }
        return String.join(", ", nombres);
    }

    /**
     * Docente responsable y acompañantes en una sola línea; es como el reporte
     * oficial pide el campo "Docente (s) responsable (s) de la visita".
     */
    public String getDocentesEnLetra() {
        List<String> nombres = new ArrayList<>();
        String responsable = (docenteResponsable != null && !docenteResponsable.isBlank())
                ? docenteResponsable : nombreSolicitante;
        if (responsable != null && !responsable.isBlank()) {
            nombres.add(responsable);
        }
        for (Usuario docente : docentesAcompanantes) {
            if (docente.getNombre() != null && !nombres.contains(docente.getNombre())) {
                nombres.add(docente.getNombre());
            }
        }
        return String.join(", ", nombres);
    }
}
