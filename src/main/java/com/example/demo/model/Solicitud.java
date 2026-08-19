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

    /**
     * Divisiones académicas del formato FO-UTEZ-EST-08, en el orden en que se imprimen.
     */
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
    // Estudiantes por división académica (ESTUDIANTES_DIVISION), siempre con las 4 llaves
    private Map<String, Integer> estudiantesPorDivision = divisionesEnCero();
    // Docentes acompañantes (SOLICITUD_DOCENTE); solo se usan id y nombre
    private List<Usuario> docentesAcompanantes = new ArrayList<>();

    /**
     * Entidad que representa una Solicitud de Visita Académica
     * <p>
     * Mapea la información general del trámite y consolida datos complementarios requeridos para la generación
     * del formato oficial <b>FO-UTEZ-EST-08</b>, tales como programas educativos, asignaturas, docentes acompañantes
     * y el desglose de estudiantes.
     * </p>
     * <p>
     * Además de sus propiedades persistentes, incluye lógica de negocio para
     * determinar el estado legible de la visita según el ciclo de vida del reporte,
     * mapeo de estilos de CSS para visitas y formeadores de texto para la redacción
     * de oficios y cartas responsivas.
     * </p>
     *
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public Solicitud() {
    }

    /**
     * Crea un mapa ordenado inicializado con todas las divisiones académicas
     * oficiales registradas en cero.
     * <p>
     * Este asegura que las vistas JSP y procesadores siempre encuentren las
     * llaves correspondientes a cada división sin arrojar excepciones por valores nulos.
     * </p>
     *
     * @return un {@link Map} con las divisiones académicas como clave y {@code 0} como valor inicial.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public static Map<String, Integer> divisionesEnCero() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (String division : DIVISIONES) {
            mapa.put(division, 0);
        }
        return mapa;
    }

    /**
     * Obtiene el identificador del reporte vinculado a esta solicitud
     *
     * @return el identificador del reporte, o {@code null} si aún no se ha generado
     * @author Eder Gabriel Gaarcía Vázquez
     * @since 18/08/2026
     */
    public Integer getIdReporte() {
        return idReporte;
    }

    /**
     * Establece el identificador del reporte vinculado
     *
     * @param idReporte el identificador del reporte
     * @author Eder Gabriel Gaarcía Vázquez
     * @since 18/08/2026
     */
    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    /**
     * Obtiene el identificador único de la solicitud
     *
     * @return el id de la solicitud
     * @author Eder Gabriel Gaarcía Vázquez
     * @since 18/08/2026
     */
    public int getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * Establece el identificador único de la solicitud
     *
     * @param idSolicitud el Id de la solicitud
     * @author Eder Gabriel Gaarcía Vázquez
     * @since 18/08/2026
     */
    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * Obtiene el ID del usuario docente que creó la solicitud
     *
     * @return el ID del usuario solicitante
     * @author Eder Gabriel Gaarcía Vázquez
     * @since 18/08/2026
     */
    public int getIdUsuarioSolicitante() {
        return idUsuarioSolicitante;
    }

    /**
     * Establece el ID del usuario docente solicitante
     *
     * @param idUsuarioSolicitante el ID del usuario
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setIdUsuarioSolicitante(int idUsuarioSolicitante) {
        this.idUsuarioSolicitante = idUsuarioSolicitante;
    }

    /**
     * Obtiene el ID del usuario administrativo que autorizó o revisó la solicitud
     *
     * @return el ID del usuario autorizador, o {@code null} si no ha sido procesada
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public Integer getIdUsuarioAutoriza() {
        return idUsuarioAutoriza;
    }

    /**
     * Establece el ID del usuario administrativo que autoriza
     *
     * @param idUsuarioAutoriza el ID del usuario autorizador
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setIdUsuarioAutoriza(Integer idUsuarioAutoriza) {
        this.idUsuarioAutoriza = idUsuarioAutoriza;
    }

    /**
     * Obtiene el nombre de la empresa o actividad objetivo de la visita
     *
     * @return el nombre de la empresa o actividad
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getNombreEmpresaActividad() {
        return nombreEmpresaActividad;
    }

    /**
     * Establece el nombre de la empresa o actividad
     *
     * @param nombreEmpresaActividad el nombre de la empresa o actividad
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setNombreEmpresaActividad(String nombreEmpresaActividad) {
        this.nombreEmpresaActividad = nombreEmpresaActividad;
    }

    /**
     * Obtiene la dirección física o lugar de destino
     *
     * @return la dirección o lugar
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getLugarDireccion() {
        return lugarDireccion;
    }

    /**
     * Establece la dirección física o lugar de destino
     *
     * @param lugarDireccion la dirección o lugar
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setLugarDireccion(String lugarDireccion) {
        this.lugarDireccion = lugarDireccion;
    }


    /**
     * Obtiene el teléfono de contacto del lugar de la visita.
     *
     * @return el teléfono de contacto.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    /**
     * Establece el teléfono de contacto del lugar de la visita.
     *
     * @param telefonoContacto el teléfono de contacto.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    /**
     * Obtiene el correo electrónico del contacto en la empresa.
     *
     * @return el correo electrónico de contacto.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getCorreoContacto() {
        return correoContacto;
    }

    /**
     * Establece el correo electrónico del contacto en la empresa.
     *
     * @param correoContacto el correo electrónico.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    /**
     * Obtiene la fecha programada de inicio de la visita.
     *
     * @return la fecha en formato {@code yyyy-MM-dd}.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getFechaInicio() {
        return fechaInicio;
    }

    /**
     * Establece la fecha programada de inicio de la visita.
     *
     * @param fechaInicio la fecha en formato {@code yyyy-MM-dd}.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /**
     * Obtiene el objetivo académico pedagógico de la visita.
     *
     * @return el objetivo de la visita.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getObjetivo() {
        return objetivo;
    }

    /**
     * Establece el objetivo académico pedagógico de la visita.
     *
     * @param objetivo el objetivo de la visita.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    /**
     * Obtiene el área o división solicitante.
     *
     * @return el nombre del área solicitante.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getAreaSolicitante() {
        return areaSolicitante;
    }

    /**
     * Establece el área o división solicitante.
     *
     * @param areaSolicitante el nombre del área solicitante.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setAreaSolicitante(String areaSolicitante) {
        this.areaSolicitante = areaSolicitante;
    }

    /**
     * Obtiene el nombre del docente responsable capturado en el formulario.
     * <p>
     * Nota: Este nombre se captura manualmente en el formato y puede diferir
     * del nombre de la cuenta del usuario solicitante.
     * </p>
     *
     * @return el nombre del docente responsable.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getDocenteResponsable() {
        return docenteResponsable;
    }

    /**
     * Establece el nombre del docente responsable de la visita.
     *
     * @param docenteResponsable el nombre del docente responsable.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setDocenteResponsable(String docenteResponsable) {
        this.docenteResponsable = docenteResponsable;
    }

    /**
     * Obtiene el teléfono celular del docente responsable.
     *
     * @return el celular del docente responsable.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getCelularResponsable() {
        return celularResponsable;
    }

    /**
     * Establece el teléfono celular del docente responsable.
     *
     * @param celularResponsable el número celular.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setCelularResponsable(String celularResponsable) {
        this.celularResponsable = celularResponsable;
    }

    /**
     * Obtiene el ID del estado actual de la solicitud.
     *
     * @return el ID del estado.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public int getIdEstado() {
        return idEstado;
    }

    /**
     * Establece el ID del estado de la solicitud.
     *
     * @param idEstado el ID del estado.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    /**
     * Obtiene los detalles, observaciones o motivos dictaminados durante la revisión.
     *
     * @return los detalles de la decisión tomada.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getDetallesDecision() {
        return detallesDecision;
    }

    /**
     * Establece los detalles u observaciones de la decisión administrativa.
     *
     * @param detallesDecision el texto explicativo de la decisión.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setDetallesDecision(String detallesDecision) {
        this.detallesDecision = detallesDecision;
    }

    /**
     * Obtiene la fecha y hora de creación del registro.
     *
     * @return la fecha de creación en texto.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Establece la fecha y hora de creación de la solicitud.
     *
     * @param fechaCreacion la fecha de creación.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Obtiene el nombre del estado almacenado en la base de datos.
     *
     * @return el nombre crudo del estado.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getNombreEstado() {
        return nombreEstado;
    }

    /**
     * Establece el nombre del estado registrado en la base de datos.
     *
     * @param nombreEstado el nombre del estado.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    /**
     * Determina y retorna la denominación comprensible y precisa del estado para la interfaz de usuario.
     * <p>
     * Regla de negocio: Traduce estados internos que pueden prestarse a malinterpretación
     * (por ejemplo, "Pendiente" pasa a "Sin enviar") e integra de forma transparente
     * el estado del reporte una vez que la solicitud primaria fue "Completada".
     * Mantiene consistencia léxica usando prefijos estándar para el seguimiento
     * del reporte académico (ej. "Reporte en revisión", "Finalizada").
     * </p>
     *
     * @return el estado formateado para su lectura y despliegue público en la vista.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
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

    /**
     * Determina el nombre del sufijo CSS correspondiente al estado visible devuelto por {@link #getEstadoLegible()}.
     * <p>
     * Utilizado en la interfaz gráfica para renderizar badges e indicadores visuales de estado
     * con sus respectivos colores institucionales (ej. {@code .estado-sin-enviar}, {@code .estado-aprobada}).
     * </p>
     *
     * @return el nombre abreviado de la clase CSS del estado.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getClaseEstado() {
        switch (getEstadoLegible()) {
            case "Sin enviar":
                return "sin-enviar";
            case "En revisión":
                return "en-revision";
            case "Aprobada":
                return "aprobada";
            case "Rechazada":
                return "rechazada";
            case "Finalizada":
                return "finalizada";
            case "Reporte rechazado":
                return "reporte-rechazado";
            case "Reporte en revisión":
                return "en-revision"; // Mismo color amarillo que "En revisión"
            default:
                return "reporte-pendiente"; // Caso "Reporte por completar"
        }
    }

    /**
     * Obtiene el estado del reporte final de la visita.
     *
     * @return el estado del reporte, o {@code null} si el reporte aún no ha sido creado.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getEstadoReporte() {
        return estadoReporte;
    }

    /**
     * Establece el estado del reporte final de la visita.
     *
     * @param estadoReporte el estado del reporte.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setEstadoReporte(String estadoReporte) {
        this.estadoReporte = estadoReporte;
    }

    /**
     * Obtiene el nombre completo del docente solicitante.
     *
     * @return el nombre del solicitante.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    /**
     * Establece el nombre completo del docente solicitante.
     *
     * @param nombreSolicitante el nombre del solicitante.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    /**
     * Obtiene el correo institucional del docente solicitante.
     *
     * @return el correo electrónico del solicitante.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    /**
     * Establece el correo institucional del docente solicitante.
     *
     * @param correoSolicitante el correo del solicitante.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setCorreoSolicitante(String correoSolicitante) {
        this.correoSolicitante = correoSolicitante;
    }

    /**
     * Obtiene el nombre del usuario administrativo que aprobó la solicitud.
     *
     * @return el nombre del usuario que autoriza, o {@code null} si aún no ha sido revisada por Estadías.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public String getNombreAutoriza() {
        return nombreAutoriza;
    }

    /**
     * Establece el nombre del usuario administrativo que aprueba la solicitud.
     *
     * @param nombreAutoriza el nombre del usuario autorizador.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setNombreAutoriza(String nombreAutoriza) {
        this.nombreAutoriza = nombreAutoriza;
    }

    /**
     * Obtiene la suma total de estudiantes participantes.
     *
     * @return el número total de estudiantes.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public int getTotalEstudiantes() {
        return totalEstudiantes;
    }

    /**
     * Establece la suma total de estudiantes participantes.
     *
     * @param totalEstudiantes el total de estudiantes.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setTotalEstudiantes(int totalEstudiantes) {
        this.totalEstudiantes = totalEstudiantes;
    }

    /**
     * Obtiene la lista de programas educativos asociados a la solicitud.
     *
     * @return la lista de objetos {@link ProgramaEducativo}.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<ProgramaEducativo> getProgramas() {
        return programas;
    }

    /**
     * Establece la lista de programas educativos asociados a la solicitud.
     *
     * @param programas la lista de programas educativos.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setProgramas(List<ProgramaEducativo> programas) {
        this.programas = programas;
    }

    /**
     * Obtiene la lista de nombres de asignaturas vinculadas a la visita.
     *
     * @return una lista de cadenas con los nombres de las asignaturas.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<String> getAsignaturas() {
        return asignaturas;
    }

    /**
     * Establece la lista de nombres de asignaturas vinculadas.
     *
     * @param asignaturas la lista de asignaturas.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setAsignaturas(List<String> asignaturas) {
        this.asignaturas = asignaturas;
    }

    /**
     * Obtiene el mapa del desglose de estudiantes asignados por cada división académica.
     *
     * @return un {@link Map} con el nombre de la división y la cantidad de estudiantes.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public Map<String, Integer> getEstudiantesPorDivision() {
        return estudiantesPorDivision;
    }

    /**
     * Establece el mapa con la distribución de estudiantes por división académica.
     *
     * @param estudiantesPorDivision el mapa con el desglose por división.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setEstudiantesPorDivision(Map<String, Integer> estudiantesPorDivision) {
        this.estudiantesPorDivision = estudiantesPorDivision;
    }

    /**
     * Recalcula automáticamente el desglose de estudiantes por división académica
     * sumando la cantidad de alumnos registrada en los programas educativos asignados.
     * <p>
     * Como cada programa educativo pertenece a una división específica del catálogo,
     * este método garantiza que los totales acumulados por división y por programa
     * se mantengan siempre perfectamente sincronizados sin desfases mecánicos.
     * </p>
     *
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void recalcularEstudiantesPorDivision() {
        Map<String, Integer> mapa = divisionesEnCero();
        for (ProgramaEducativo p : programas) {
            String division = p.getDivision();
            if (division == null) {
                continue; // Programa fuera del catálogo (útil para retrocompatibilidad con solicitudes viejas)
            }
            mapa.merge(division, Math.max(0, p.getNoEstudiantes()), Integer::sum);
        }
        this.estudiantesPorDivision = mapa;
    }

    /**
     * Calcula la suma total acumulada de los estudiantes distribuidos entre todas las divisiones académicas.
     *
     * @return la suma total de estudiantes desglosados en el mapa de divisiones.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public int getTotalPorDivision() {
        int total = 0;
        for (Integer valor : estudiantesPorDivision.values()) {
            total += (valor != null ? valor : 0);
        }
        return total;
    }

    /**
     * Obtiene la lista de docentes asignados como acompañantes para la visita.
     *
     * @return una lista de objetos {@link Usuario} que representan a los docentes acompañantes.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<Usuario> getDocentesAcompanantes() {
        return docentesAcompanantes;
    }

    /**
     * Establece la lista de docentes acompañantes.
     *
     * @param docentesAcompanantes la lista de docentes acompañantes.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public void setDocentesAcompanantes(List<Usuario> docentesAcompanantes) {
        this.docentesAcompanantes = docentesAcompanantes;
    }

    // ----- Métodos formateadores para la generación de documentos oficiales -----

    /**
     * Formatea la fecha de inicio en texto redactado con letras (ej. {@code "12 de agosto de 2026"}).
     * <p>
     * Requerido exclusivamente para la inserción limpia en párrafos narrativos
     * del oficio formal y la carta responsiva institucional.
     * </p>
     *
     * @return la fecha en texto extendido en español, o una cadena vacía si la fecha es nula.
     * @author Eder Gabriel García Vázquez
     * @see FechaTexto#largo(String)
     * @since 18/08/2026
     */
    public String getFechaInicioEnLetra() {
        return FechaTexto.largo(fechaInicio);
    }

    /**
     * Concatena los nombres de los programas educativos participantes sin duplicados,
     * separados por comas.
     * <p>
     * Genera la cadena para llenar el espacio predeterminado de los documentos oficiales:
     * <i>"estudiantes del programa educativo de [resultado]"</i>.
     * </p>
     *
     * @return una cadena de texto con los programas educativos únicos participantes.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
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
     * Consolida los nombres del docente responsable principal y de los docentes acompañantes
     * en una sola línea de texto separada por comas.
     * <p>
     * Satisface el requerimiento de formato para el campo oficial del reporte:
     * <i>"Docente(s) responsable(s) de la visita"</i>.
     * </p>
     *
     * @return una cadena con la lista consolidada y sin repeticiones de todos los docentes a cargo.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
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