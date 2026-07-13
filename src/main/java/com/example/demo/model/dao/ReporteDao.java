package com.example.demo.model.dao;

import com.example.demo.model.Reporte;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDao {

    private static final String SELECT_BASE =
            "SELECT r.id_reporte, r.id_solicitud, TO_CHAR(r.fecha, 'YYYY-MM-DD') AS fecha, "
            + "r.resultados, r.observaciones, TO_CHAR(r.fecha_creacion, 'YYYY-MM-DD') AS fecha_creacion, "
            + "r.id_estado, r.motivo, e.nombre_estado, "
            + "s.nombre_empresa_actividad, s.lugar_direccion, s.id_usuario_solicitante, "
            + "TO_CHAR(s.fecha_creacion, 'YYYY-MM-DD') AS fecha_solicitud, "
            + "u.nombre AS nombre_solicitante, "
            + "NVL((SELECT SUM(p.no_estudiantes) FROM programa_educativo p WHERE p.id_solicitud = s.id_solicitud), 0) AS total_estudiantes "
            + "FROM reporte r "
            + "JOIN estado_reporte e ON e.id_estado = r.id_estado "
            + "JOIN solicitud s ON s.id_solicitud = r.id_solicitud "
            + "JOIN usuario u ON u.id_usuario = s.id_usuario_solicitante";

    /**
     * Crea el reporte pendiente de una solicitud completada. La fecha del
     * reporte es la fecha de la visita (o la de hoy si no se capturó).
     */
    public boolean crearPendiente(int idSolicitud) {
        String sql = "INSERT INTO reporte (id_solicitud, fecha, id_estado) VALUES (?, "
                + "NVL((SELECT fecha_inicio FROM solicitud WHERE id_solicitud = ?), SYSDATE), "
                + "(SELECT id_estado FROM estado_reporte WHERE nombre_estado = 'Pendiente'))";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setInt(2, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existePorSolicitud(int idSolicitud) {
        String sql = "SELECT COUNT(*) FROM reporte WHERE id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Reporte> getAll() {
        List<Reporte> datos = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY r.fecha_creacion DESC";
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

    /** Reportes de las solicitudes de un docente, el más reciente primero. */
    public List<Reporte> getBySolicitante(int idUsuario) {
        List<Reporte> datos = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.id_usuario_solicitante = ? ORDER BY r.fecha_creacion DESC";
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

    public Reporte getById(int idReporte) {
        String sql = SELECT_BASE + " WHERE r.id_reporte = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReporte);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Reporte getBySolicitud(int idSolicitud) {
        String sql = SELECT_BASE + " WHERE r.id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Reporte mapRow(ResultSet rs) throws SQLException {
        Reporte r = new Reporte();
        r.setIdReporte(rs.getInt("id_reporte"));
        r.setIdSolicitud(rs.getInt("id_solicitud"));
        r.setFecha(rs.getString("fecha"));
        r.setResultados(rs.getString("resultados"));
        r.setObservaciones(rs.getString("observaciones"));
        r.setFechaCreacion(rs.getString("fecha_creacion"));
        r.setIdEstado(rs.getInt("id_estado"));
        r.setMotivo(rs.getString("motivo"));
        r.setNombreEstado(rs.getString("nombre_estado"));
        r.setNombreEmpresaActividad(rs.getString("nombre_empresa_actividad"));
        r.setLugarDireccion(rs.getString("lugar_direccion"));
        r.setIdUsuarioSolicitante(rs.getInt("id_usuario_solicitante"));
        r.setFechaSolicitud(rs.getString("fecha_solicitud"));
        r.setNombreSolicitante(rs.getString("nombre_solicitante"));
        r.setTotalEstudiantes(rs.getInt("total_estudiantes"));
        return r;
    }
}
