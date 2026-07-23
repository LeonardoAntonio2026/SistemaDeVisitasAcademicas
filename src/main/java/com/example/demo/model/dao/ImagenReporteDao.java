package com.example.demo.model.dao;

import com.example.demo.model.ImagenReporte;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImagenReporteDao {

/**
 * Metadatos de las imágenes de un reporte (sin el contenido Base64; la
 * vista carga cada imagen por separado con /reporte?imagen=ID, igual que
 * DocumentoDao.getBySolicitud hace con los PDF).
 */
public List<ImagenReporte> getByReporte(int idReporte) {
List<ImagenReporte> datos = new ArrayList<>();
String sql = "SELECT id_imagen, id_reporte, tipo_mime, "
+ "TO_CHAR(fecha_carga, 'YYYY-MM-DD HH24:MI') AS fecha_carga, "
+ "LENGTH(contenido_base64) AS tam_base64 "
+ "FROM imagen_reporte WHERE id_reporte = ? ORDER BY id_imagen";
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

/** Trae la imagen completa (con contenido) para mostrarla en <img>. */
public ImagenReporte getById(int idImagen) {
String sql = "SELECT id_imagen, id_reporte, tipo_mime, "
+ "TO_CHAR(fecha_carga, 'YYYY-MM-DD HH24:MI') AS fecha_carga, "
+ "LENGTH(contenido_base64) AS tam_base64, contenido_base64 "
+ "FROM imagen_reporte WHERE id_imagen = ?";
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

/** Cuántas imágenes tiene ya el reporte (para validar el máximo de 3, RN-07). */
public int contarPorReporte(int idReporte) {
String sql = "SELECT COUNT(*) FROM imagen_reporte WHERE id_reporte = ?";
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

public boolean guardar(int idReporte, String contenidoBase64, String tipoMime) {
String sql = "INSERT INTO imagen_reporte (id_reporte, contenido_base64, tipo_mime) VALUES (?, ?, ?)";
try (Connection con = SQLConnector.getConnection();
     PreparedStatement ps = con.prepareStatement(sql)) {

ps.setInt(1, idReporte);
ps.setString(2, contenidoBase64);
ps.setString(3, tipoMime);
return ps.executeUpdate() > 0;
} catch (SQLException e) {
e.printStackTrace();
return false;
}
}

private ImagenReporte mapRow(ResultSet rs, boolean conContenido) throws SQLException {
ImagenReporte img = new ImagenReporte();
img.setIdImagen(rs.getInt("id_imagen"));
img.setIdReporte(rs.getInt("id_reporte"));
img.setTipoMime(rs.getString("tipo_mime"));
img.setFechaCarga(rs.getString("fecha_carga"));
// El contenido es Base64: cada 4 caracteres son ~3 bytes reales
img.setTamanoBytes(rs.getLong("tam_base64") * 3 / 4);
if (conContenido) {
img.setContenidoBase64(rs.getString("contenido_base64"));
}
return img;
}
}