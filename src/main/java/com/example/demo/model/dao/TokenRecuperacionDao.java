package com.example.demo.model.dao;

import com.example.demo.model.TokenRecuperacion;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenRecuperacionDao {

    public boolean crear(int idUsuario, String token) {
        String sqlInvalidar = "UPDATE token_recuperacion SET usado = 'S' WHERE id_usuario = ? AND usado = 'N'";
        String sqlInsert = "INSERT INTO token_recuperacion (id_usuario, token, fecha_expiracion, usado) "
                + "VALUES (?, ?, SYSTIMESTAMP + INTERVAL '24' HOUR, 'N')";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlInvalidar)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, token);
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

    public TokenRecuperacion buscarPorToken(String token) {
        String sql = "SELECT id_token, id_usuario, token, fecha_expiracion, usado "
                + "FROM token_recuperacion WHERE token = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TokenRecuperacion t = new TokenRecuperacion();
                    t.setIdToken(rs.getInt("id_token"));
                    t.setIdUsuario(rs.getInt("id_usuario"));
                    t.setToken(rs.getString("token"));
                    t.setFechaExpiracion(rs.getTimestamp("fecha_expiracion"));
                    t.setUsado("S".equalsIgnoreCase(rs.getString("usado")));
                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean marcarUsado(String token) {
        String sql = "UPDATE token_recuperacion SET usado = 'S' WHERE token = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}