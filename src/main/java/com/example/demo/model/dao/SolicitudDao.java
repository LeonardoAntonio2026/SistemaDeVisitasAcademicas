package com.example.demo.model.dao;

import com.example.demo.model.ProgramaEducativo;
import com.example.demo.model.Solicitud;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDao implements Dao<Solicitud, Integer> {

    private static final String SELECT_BASE =
            "SELECT s.id_solicitud, s.id_usuario_solicitante, s.id_usuario_autoriza, "
            + "s.nombre_empresa_actividad, s.lugar_direccion, s.telefono_contacto, s.correo_contacto, "
            + "TO_CHAR(s.fecha_inicio, 'YYYY-MM-DD') AS fecha_inicio, s.objetivo, s.area_solicitante, "
            + "s.id_estado, s.detalles_decision, TO_CHAR(s.fecha_creacion, 'YYYY-MM-DD') AS fecha_creacion, "
            + "e.nombre_estado, us.nombre AS nombre_solicitante, us.correo AS correo_solicitante, "
            + "NVL((SELECT SUM(p.no_estudiantes) FROM programa_educativo p WHERE p.id_solicitud = s.id_solicitud), 0) AS total_estudiantes, "
            // Estado del reporte de la visita; null si todavía no se generó.
            // Va como subselect y no como JOIN para no duplicar filas.
            + "(SELECT er.nombre_estado FROM reporte r "
            + "JOIN estado_reporte er ON er.id_estado = r.id_estado "
            + "WHERE r.id_solicitud = s.id_solicitud AND ROWNUM = 1) AS estado_reporte, "
            // Id del reporte, para enlazar directo a /reporte?id=X desde el histórico
            + "(SELECT r.id_reporte FROM reporte r "
            + "WHERE r.id_solicitud = s.id_solicitud AND ROWNUM = 1) AS id_reporte "
            + "FROM solicitud s "
            + "JOIN estado_solicitud e ON e.id_estado = s.id_estado "
            + "JOIN usuario us ON us.id_usuario = s.id_usuario_solicitante";

    @Override
    public boolean create(Solicitud entidad) {
        String sqlSolicitud = "INSERT INTO solicitud (id_usuario_solicitante, nombre_empresa_actividad, "
                + "lugar_direccion, telefono_contacto, correo_contacto, fecha_inicio, objetivo, "
                + "area_solicitante, id_estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, "
                + "(SELECT id_estado FROM estado_solicitud WHERE nombre_estado = 'Pendiente'))";
        String sqlPrograma = "INSERT INTO programa_educativo (id_solicitud, division_academica, cuatrimestre, grupo, no_estudiantes) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlAsignatura = "INSERT INTO asignatura_reforzar_solicitud (id_solicitud, nombre) VALUES (?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idSolicitud;
            try (PreparedStatement ps = con.prepareStatement(sqlSolicitud, new String[]{"ID_SOLICITUD"})) {
                ps.setInt(1, entidad.getIdUsuarioSolicitante());
                ps.setString(2, entidad.getNombreEmpresaActividad());
                ps.setString(3, entidad.getLugarDireccion());
                ps.setString(4, entidad.getTelefonoContacto());
                ps.setString(5, entidad.getCorreoContacto());
                if (entidad.getFechaInicio() != null && !entidad.getFechaInicio().isBlank()) {
                    ps.setDate(6, Date.valueOf(entidad.getFechaInicio()));
                } else {
                    ps.setNull(6, java.sql.Types.DATE);
                }
                ps.setString(7, entidad.getObjetivo());
                ps.setString(8, entidad.getAreaSolicitante());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    idSolicitud = rs.getInt(1);
                }
            }

            if (!entidad.getProgramas().isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(sqlPrograma)) {
                    for (ProgramaEducativo p : entidad.getProgramas()) {
                        ps.setInt(1, idSolicitud);
                        ps.setString(2, p.getDivisionAcademica());
                        ps.setInt(3, p.getCuatrimestre());
                        ps.setString(4, p.getGrupo());
                        ps.setInt(5, p.getNoEstudiantes());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            if (!entidad.getAsignaturas().isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(sqlAsignatura)) {
                    for (String asignatura : entidad.getAsignaturas()) {
                        ps.setInt(1, idSolicitud);
                        ps.setString(2, asignatura);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            con.commit();
            entidad.setIdSolicitud(idSolicitud);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<Solicitud> getAll() {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    @Override
    public Solicitud getById(Integer id) {
        String sql = SELECT_BASE + " WHERE s.id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Solicitud solicitud = mapRow(rs);
                    solicitud.setProgramas(getProgramas(id));
                    solicitud.setAsignaturas(getAsignaturas(id));
                    return solicitud;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Solicitudes creadas por un docente, la más reciente primero (para las tarjetas del inicio).
     */
    public List<Solicitud> getBySolicitante(int idUsuario) {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.id_usuario_solicitante = ? ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    /**
     * Solicitudes activas de un docente (las Completadas y Rechazadas se van
     * al histórico y ya no aparecen en el inicio — RN-05).
     */
    public List<Solicitud> getActivasBySolicitante(int idUsuario) {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.id_usuario_solicitante = ? "
                + "AND e.nombre_estado NOT IN ('Completada', 'Rechazada') "
                + "ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    /**
     * Solicitudes activas para el coordinador de Estadías: las que ya fueron
     * enviadas y siguen en proceso. Las Pendientes no aparecen porque el
     * docente aún no las envía (RN-02).
     */
    public List<Solicitud> getActivasParaRevision() {
        return getPorEstados("e.nombre_estado IN ('En revisión', 'Aprobada')");
    }

    /** Todas las solicitudes ya enviadas (para la página Solicitudes del coordinador). */
    public List<Solicitud> getEnviadas() {
        return getPorEstados("e.nombre_estado <> 'Pendiente'");
    }

    /**
     * Histórico: solicitudes terminadas (Completadas y Rechazadas).
     * Si idUsuario es null se traen las de todos los docentes.
     */
    public List<Solicitud> getHistorico(Integer idUsuario) {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE e.nombre_estado IN ('Completada', 'Rechazada') "
                + (idUsuario != null ? "AND s.id_usuario_solicitante = ? " : "")
                + "ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (idUsuario != null) {
                ps.setInt(1, idUsuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    private List<Solicitud> getPorEstados(String condicionEstado) {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE " + condicionEstado + " ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    /** Cambio simple de estado (ej. Pendiente → En revisión al enviar). */
    public boolean cambiarEstado(int idSolicitud, String nombreEstado) {
        String sql = "UPDATE solicitud SET id_estado = "
                + "(SELECT id_estado FROM estado_solicitud WHERE nombre_estado = ?) "
                + "WHERE id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreEstado);
            ps.setInt(2, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decisión de Estadías: aprueba o rechaza guardando quién autorizó y el
     * motivo (RF-05).
     */
    public boolean decidir(int idSolicitud, String nombreEstado, String motivo, int idUsuarioAutoriza) {
        String sql = "UPDATE solicitud SET id_estado = "
                + "(SELECT id_estado FROM estado_solicitud WHERE nombre_estado = ?), "
                + "detalles_decision = ?, id_usuario_autoriza = ? WHERE id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreEstado);
            ps.setString(2, motivo);
            ps.setInt(3, idUsuarioAutoriza);
            ps.setInt(4, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza la solicitud completa (datos + desglose por programa +
     * asignaturas) mientras siga Pendiente. Los hijos se reemplazan en la
     * misma transacción para que queden igual que como se capturaron.
     */
    @Override
    public boolean update(Solicitud entidad) {
        String sqlSolicitud = "UPDATE solicitud SET nombre_empresa_actividad = ?, lugar_direccion = ?, "
                + "telefono_contacto = ?, correo_contacto = ?, fecha_inicio = ?, objetivo = ?, "
                + "area_solicitante = ? WHERE id_solicitud = ?";
        String sqlPrograma = "INSERT INTO programa_educativo (id_solicitud, division_academica, cuatrimestre, grupo, no_estudiantes) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlAsignatura = "INSERT INTO asignatura_reforzar_solicitud (id_solicitud, nombre) VALUES (?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int id = entidad.getIdSolicitud();
            int filas;
            try (PreparedStatement ps = con.prepareStatement(sqlSolicitud)) {
                ps.setString(1, entidad.getNombreEmpresaActividad());
                ps.setString(2, entidad.getLugarDireccion());
                ps.setString(3, entidad.getTelefonoContacto());
                ps.setString(4, entidad.getCorreoContacto());
                if (entidad.getFechaInicio() != null && !entidad.getFechaInicio().isBlank()) {
                    ps.setDate(5, Date.valueOf(entidad.getFechaInicio()));
                } else {
                    ps.setNull(5, java.sql.Types.DATE);
                }
                ps.setString(6, entidad.getObjetivo());
                ps.setString(7, entidad.getAreaSolicitante());
                ps.setInt(8, id);
                filas = ps.executeUpdate();
            }
            if (filas == 0) {
                con.rollback();
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM programa_educativo WHERE id_solicitud = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM asignatura_reforzar_solicitud WHERE id_solicitud = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            if (!entidad.getProgramas().isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(sqlPrograma)) {
                    for (ProgramaEducativo p : entidad.getProgramas()) {
                        ps.setInt(1, id);
                        ps.setString(2, p.getDivisionAcademica());
                        ps.setInt(3, p.getCuatrimestre());
                        ps.setString(4, p.getGrupo());
                        ps.setInt(5, p.getNoEstudiantes());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            if (!entidad.getAsignaturas().isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(sqlAsignatura)) {
                    for (String asignatura : entidad.getAsignaturas()) {
                        ps.setInt(1, id);
                        ps.setString(2, asignatura);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean delete(Integer id) {
        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            // Primero los hijos por las FKs, después la solicitud
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM documento WHERE id_solicitud = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM asignatura_reforzar_solicitud WHERE id_solicitud = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM programa_educativo WHERE id_solicitud = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            int filas;
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM solicitud WHERE id_solicitud = ?")) {
                ps.setInt(1, id);
                filas = ps.executeUpdate();
            }

            con.commit();
            return filas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<ProgramaEducativo> getProgramas(int idSolicitud) throws SQLException {
        List<ProgramaEducativo> programas = new ArrayList<>();
        String sql = "SELECT id_programa, id_solicitud, division_academica, cuatrimestre, grupo, no_estudiantes "
                + "FROM programa_educativo WHERE id_solicitud = ? ORDER BY id_programa";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProgramaEducativo p = new ProgramaEducativo();
                    p.setIdPrograma(rs.getInt("id_programa"));
                    p.setIdSolicitud(rs.getInt("id_solicitud"));
                    p.setDivisionAcademica(rs.getString("division_academica"));
                    p.setCuatrimestre(rs.getInt("cuatrimestre"));
                    p.setGrupo(rs.getString("grupo"));
                    p.setNoEstudiantes(rs.getInt("no_estudiantes"));
                    programas.add(p);
                }
            }
        }
        return programas;
    }

    private List<String> getAsignaturas(int idSolicitud) throws SQLException {
        List<String> asignaturas = new ArrayList<>();
        String sql = "SELECT nombre FROM asignatura_reforzar_solicitud WHERE id_solicitud = ? ORDER BY id_asignatura";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    asignaturas.add(rs.getString("nombre"));
                }
            }
        }
        return asignaturas;
    }

    private Solicitud mapRow(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdUsuarioSolicitante(rs.getInt("id_usuario_solicitante"));
        int autoriza = rs.getInt("id_usuario_autoriza");
        s.setIdUsuarioAutoriza(rs.wasNull() ? null : autoriza);
        s.setNombreEmpresaActividad(rs.getString("nombre_empresa_actividad"));
        s.setLugarDireccion(rs.getString("lugar_direccion"));
        s.setTelefonoContacto(rs.getString("telefono_contacto"));
        s.setCorreoContacto(rs.getString("correo_contacto"));
        s.setFechaInicio(rs.getString("fecha_inicio"));
        s.setObjetivo(rs.getString("objetivo"));
        s.setAreaSolicitante(rs.getString("area_solicitante"));
        s.setIdEstado(rs.getInt("id_estado"));
        s.setDetallesDecision(rs.getString("detalles_decision"));
        s.setFechaCreacion(rs.getString("fecha_creacion"));
        s.setNombreEstado(rs.getString("nombre_estado"));
        s.setEstadoReporte(rs.getString("estado_reporte"));
        int idReporte = rs.getInt("id_reporte");
        s.setIdReporte(rs.wasNull() ? null : idReporte);
        s.setNombreSolicitante(rs.getString("nombre_solicitante"));
        s.setCorreoSolicitante(rs.getString("correo_solicitante"));
        s.setTotalEstudiantes(rs.getInt("total_estudiantes"));
        return s;
    }
}
