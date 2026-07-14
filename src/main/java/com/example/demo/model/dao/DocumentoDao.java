package com.example.demo.model.dao;

import com.example.demo.model.Documento;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentoDao {

    /**
     * Metadatos de los documentos de una solicitud (sin el contenido Base64,
     * que puede pesar varios MB; ese solo se trae al descargar).
     */
    public List<Documento> getBySolicitud(int idSolicitud) {
        List<Documento> datos = new ArrayList<>();
        String sql = "SELECT d.id_documento, d.id_solicitud, d.id_reporte, d.id_tipo_documento, "
                + "t.nombre_tipo, TO_CHAR(d.fecha_carga, 'YYYY-MM-DD') AS fecha_carga, "
                + "LENGTH(d.contenido_base64) AS tam_base64 "
                + "FROM documento d JOIN tipo_documento t ON t.id_tipo_documento = d.id_tipo_documento "
                + "WHERE d.id_solicitud = ? ORDER BY d.fecha_carga, d.id_documento";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.add(mapRow(rs, false));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    /** Trae el documento completo (con contenido) para descargarlo. */
    public Documento getById(int idDocumento) {
        String sql = "SELECT d.id_documento, d.id_solicitud, d.id_reporte, d.id_tipo_documento, "
                + "t.nombre_tipo, TO_CHAR(d.fecha_carga, 'YYYY-MM-DD') AS fecha_carga, "
                + "LENGTH(d.contenido_base64) AS tam_base64, d.contenido_base64 "
                + "FROM documento d JOIN tipo_documento t ON t.id_tipo_documento = d.id_tipo_documento "
                + "WHERE d.id_documento = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Guarda un documento de la solicitud. Si ya existía uno del mismo tipo se
     * reemplaza (el docente puede volver a subir el archivo si se equivocó).
     */
    public boolean guardarParaSolicitud(int idSolicitud, String nombreTipo, String contenidoBase64) {
        String sqlDelete = "DELETE FROM documento WHERE id_solicitud = ? AND id_tipo_documento = "
                + "(SELECT id_tipo_documento FROM tipo_documento WHERE nombre_tipo = ?)";
        String sqlInsert = "INSERT INTO documento (id_solicitud, id_tipo_documento, contenido_base64) "
                + "VALUES (?, (SELECT id_tipo_documento FROM tipo_documento WHERE nombre_tipo = ?), ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                ps.setInt(1, idSolicitud);
                ps.setString(2, nombreTipo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setInt(1, idSolicitud);
                ps.setString(2, nombreTipo);
                ps.setString(3, contenidoBase64);
                ps.executeUpdate();
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

    /**
     * Elimina el documento de un tipo de la solicitud. Se usa al editar los
     * datos: el FO firmado que ya estaba subido queda obsoleto porque el
     * formato se regenera con la información nueva.
     */
    public boolean eliminarTipoDeSolicitud(int idSolicitud, String nombreTipo) {
        String sql = "DELETE FROM documento WHERE id_solicitud = ? AND id_tipo_documento = "
                + "(SELECT id_tipo_documento FROM tipo_documento WHERE nombre_tipo = ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setString(2, nombreTipo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Indica si la solicitud ya tiene un documento de ese tipo (ej. el FO firmado). */
    public boolean existeTipoEnSolicitud(int idSolicitud, String nombreTipo) {
        String sql = "SELECT COUNT(*) FROM documento d "
                + "JOIN tipo_documento t ON t.id_tipo_documento = d.id_tipo_documento "
                + "WHERE d.id_solicitud = ? AND t.nombre_tipo = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setString(2, nombreTipo);
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

    private Documento mapRow(ResultSet rs, boolean conContenido) throws SQLException {
        Documento d = new Documento();
        d.setIdDocumento(rs.getInt("id_documento"));
        int idSolicitud = rs.getInt("id_solicitud");
        d.setIdSolicitud(rs.wasNull() ? null : idSolicitud);
        int idReporte = rs.getInt("id_reporte");
        d.setIdReporte(rs.wasNull() ? null : idReporte);
        d.setIdTipoDocumento(rs.getInt("id_tipo_documento"));
        d.setNombreTipo(rs.getString("nombre_tipo"));
        d.setFechaCarga(rs.getString("fecha_carga"));
        // El contenido es Base64: cada 4 caracteres son ~3 bytes reales
        d.setTamanoBytes(rs.getLong("tam_base64") * 3 / 4);
        if (conContenido) {
            d.setContenidoBase64(rs.getString("contenido_base64"));
        }
        return d;
    }
}
