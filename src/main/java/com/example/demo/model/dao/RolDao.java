package com.example.demo.model.dao;

import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de roles (tabla ROL). No implementa Dao a propósito: el catálogo se
 * siembra con seed.sql y desde la aplicación solo se consulta, así el combo de
 * roles de la gestión de usuarios (RF-12) nunca queda hardcodeado en la vista.
 */
public class RolDao {

    /** Nombres de rol en el orden del catálogo, para llenar el select del formulario. */
    public List<String> getNombres() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre_rol FROM rol ORDER BY id_rol";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                nombres.add(rs.getString("nombre_rol"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombres;
    }

    /**
     * Id del rol con ese nombre, o 0 si no existe. Sirve además para validar en
     * el servidor el rol que llega del formulario: si devuelve 0, se rechaza.
     */
    public int getIdPorNombre(String nombreRol) {
        if (nombreRol == null || nombreRol.isBlank()) {
            return 0;
        }
        String sql = "SELECT id_rol FROM rol WHERE nombre_rol = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreRol.trim());
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
}
