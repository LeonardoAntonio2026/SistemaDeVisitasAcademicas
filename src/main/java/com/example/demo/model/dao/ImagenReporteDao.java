package com.example.demo.model.dao;

import com.example.demo.model.ImagenReporte;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Imágenes del reporte de visita, guardadas en la tabla IMAGEN
 * (id_imagen, id_reporte, contenido_base64 CLOB, fecha_carga).
 * La tabla no tiene tipo_mime: el servlet detecta JPG/PNG del contenido.
 *
 * @author Alan Esteban Zarinana Arizmendi
 * @since 2026-08-25
 */
public class ImagenReporteDao {

    /**
     * Metadatos de las imágenes de un reporte (sin el contenido Base64; la
     * vista carga cada imagen por separado con /reporte?imagen=ID, igual que
     * DocumentoDao.getBySolicitud hace con los PDF).
     *
     * @param idReporte id del reporte dueño de las imágenes
     * @return lista de imágenes (sin contenido); vacía si no hay o si falla la consulta
     */
    public List<ImagenReporte> getByReporte(int idReporte) {
        List<ImagenReporte> datos = new ArrayList<>();
        String sql = "SELECT id_imagen, id_reporte, "
                + "TO_CHAR(fecha_carga, 'YYYY-MM-DD HH24:MI') AS fecha_carga, "
                + "LENGTH(contenido_base64) AS tam_base64 "
                + "FROM imagen WHERE id_reporte = ? ORDER BY id_imagen";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReporte);
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

    /**
     * Trae la imagen completa (con contenido) para mostrarla en {@code <img>}.
     *
     * @param idImagen id de la imagen a buscar
     * @return la imagen con su contenido Base64, o {@code null} si no existe o falla la consulta
     */
    public ImagenReporte getById(int idImagen) {
        String sql = "SELECT id_imagen, id_reporte, "
                + "TO_CHAR(fecha_carga, 'YYYY-MM-DD HH24:MI') AS fecha_carga, "
                + "LENGTH(contenido_base64) AS tam_base64, contenido_base64 "
                + "FROM imagen WHERE id_imagen = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idImagen);
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
     * Cuántas imágenes tiene ya el reporte (para validar las 3 exactas, RN-07).
     *
     * @param idReporte id del reporte a consultar
     * @return el número de imágenes que tiene el reporte
     */
    public int contarPorReporte(int idReporte) {
        String sql = "SELECT COUNT(*) FROM imagen WHERE id_reporte = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReporte);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Guarda una imagen nueva para el reporte.
     *
     * @param idReporte       id del reporte dueño de la imagen
     * @param contenidoBase64 contenido de la imagen codificado en Base64
     * @return {@code true} si se guardó correctamente
     */
    public boolean guardar(int idReporte, String contenidoBase64) {
        String sql = "INSERT INTO imagen (id_reporte, contenido_base64) VALUES (?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReporte);
            ps.setString(2, contenidoBase64);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Borra una imagen que el docente quitó al editar. El id_reporte en el
     * WHERE evita borrar imágenes de otro reporte manipulando el formulario.
     *
     * @param idImagen  id de la imagen a eliminar
     * @param idReporte id del reporte dueño de la imagen (verificación de pertenencia)
     * @return {@code true} si se eliminó al menos un registro
     */
    public boolean eliminar(int idImagen, int idReporte) {
        String sql = "DELETE FROM imagen WHERE id_imagen = ? AND id_reporte = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idImagen);
            ps.setInt(2, idReporte);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convierte una fila del ResultSet en una {@link ImagenReporte}.
     *
     * @param rs           fila actual del resultado de la consulta
     * @param conContenido si {@code true}, también carga el contenido Base64
     * @return la imagen mapeada a partir de la fila
     * @throws SQLException si falla la lectura de alguna columna
     */
    private ImagenReporte mapRow(ResultSet rs, boolean conContenido) throws SQLException {
        ImagenReporte img = new ImagenReporte();
        img.setIdImagen(rs.getInt("id_imagen"));
        img.setIdReporte(rs.getInt("id_reporte"));
        img.setFechaCarga(rs.getString("fecha_carga"));
        // El contenido es Base64: cada 4 caracteres son ~3 bytes reales
        img.setTamanoBytes(rs.getLong("tam_base64") * 3 / 4);
        if (conContenido) {
            img.setContenidoBase64(rs.getString("contenido_base64"));
        }
        return img;
    }
}