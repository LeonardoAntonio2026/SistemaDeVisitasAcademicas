package com.example.demo.model.dao;

import com.example.demo.model.Visita;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VisitaDao implements Dao<Visita, Integer> {

    @Override
    public boolean create(Visita entidad) {
        String sql = "INSERT INTO visitas (nombre_empresa, direccion, telefono, correo, fecha_inicio, "
                + "fecha_termino, hora_visita, objetivo, area_solicitante, docente_responsable, "
                + "celular_docente, docentes_acompanantes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombreEmpresa());
            ps.setString(2, entidad.getDireccion());
            ps.setString(3, entidad.getTelefono());
            ps.setString(4, entidad.getCorreo());
            ps.setString(5, entidad.getFechaInicio());
            ps.setString(6, entidad.getFechaTermino());
            ps.setString(7, entidad.getHoraVisita());
            ps.setString(8, entidad.getObjetivo());
            ps.setString(9, entidad.getAreaSolicitante());
            ps.setString(10, entidad.getDocenteResponsable());
            ps.setString(11, entidad.getCelularDocente());
            ps.setString(12, entidad.getDocentesAcompanantes());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Visita> getAll() {
        List<Visita> datos = new ArrayList<>();
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM visitas");
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
    public Visita getById(Integer id) {
        String sql = "SELECT * FROM visitas WHERE id = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
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

    @Override
    public boolean update(Visita entidad) {
        String sql = "UPDATE visitas SET nombre_empresa = ?, direccion = ?, telefono = ?, correo = ?, "
                + "fecha_inicio = ?, fecha_termino = ?, hora_visita = ?, objetivo = ?, "
                + "area_solicitante = ?, docente_responsable = ?, celular_docente = ?, "
                + "docentes_acompanantes = ? WHERE id = ?";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombreEmpresa());
            ps.setString(2, entidad.getDireccion());
            ps.setString(3, entidad.getTelefono());
            ps.setString(4, entidad.getCorreo());
            ps.setString(5, entidad.getFechaInicio());
            ps.setString(6, entidad.getFechaTermino());
            ps.setString(7, entidad.getHoraVisita());
            ps.setString(8, entidad.getObjetivo());
            ps.setString(9, entidad.getAreaSolicitante());
            ps.setString(10, entidad.getDocenteResponsable());
            ps.setString(11, entidad.getCelularDocente());
            ps.setString(12, entidad.getDocentesAcompanantes());
            ps.setInt(13, entidad.getId());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM visitas WHERE id = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Visita mapRow(ResultSet rs) throws SQLException {
        Visita v = new Visita();
        v.setId(rs.getInt("id"));
        v.setNombreEmpresa(rs.getString("nombre_empresa"));
        v.setDireccion(rs.getString("direccion"));
        v.setTelefono(rs.getString("telefono"));
        v.setCorreo(rs.getString("correo"));
        v.setFechaInicio(rs.getString("fecha_inicio"));
        v.setFechaTermino(rs.getString("fecha_termino"));
        v.setHoraVisita(rs.getString("hora_visita"));
        v.setObjetivo(rs.getString("objetivo"));
        v.setAreaSolicitante(rs.getString("area_solicitante"));
        v.setDocenteResponsable(rs.getString("docente_responsable"));
        v.setCelularDocente(rs.getString("celular_docente"));
        v.setDocentesAcompanantes(rs.getString("docentes_acompanantes"));
        return v;
    }

    public List<Visita> getByDocente(int idDocente) {
        List<Visita> datos = new ArrayList<>();
        String sql = "SELECT * FROM visitas WHERE docente_responsable = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) datos.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    public List<Visita> getByArea(String area) {
        List<Visita> datos = new ArrayList<>();
        String sql = "SELECT * FROM visitas WHERE area_solicitante = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, area);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) datos.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }
}


