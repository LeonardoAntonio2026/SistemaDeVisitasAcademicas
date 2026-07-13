package com.example.demo.model.dao;

import com.example.demo.model.Usuario;
import com.example.demo.utils.PasswordUtils;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao implements Dao<Usuario, Integer> {

    private static final String SELECT_BASE =
            "SELECT u.id_usuario, u.id_rol, u.nombre, u.correo, r.nombre_rol "
            + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol";

    /**
     * Crea el usuario con rol Docente por defecto y guarda el hash de su
     * contraseña en la tabla CONTRASENA, todo en una misma transacción.
     */
    @Override
    public boolean create(Usuario entidad) {
        String sqlUsuario = "INSERT INTO usuario (id_rol, nombre, correo) "
                + "VALUES ((SELECT id_rol FROM rol WHERE nombre_rol = 'Docente'), ?, ?)";
        String sqlContrasena = "INSERT INTO contrasena (id_usuario, hash_password) VALUES (?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuario;
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, new String[]{"ID_USUARIO"})) {
                ps.setString(1, entidad.getNombre());
                ps.setString(2, entidad.getCorreo());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    idUsuario = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlContrasena)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, PasswordUtils.sha256(entidad.getContrasena()));
                ps.executeUpdate();
            }

            con.commit();
            entidad.setId(idUsuario);
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
    public List<Usuario> getAll() {
        List<Usuario> datos = new ArrayList<>();
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE);
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
    public Usuario getById(Integer id) {
        String sql = SELECT_BASE + " WHERE u.id_usuario = ?";
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
    public boolean update(Usuario entidad) {
        String sql = "UPDATE usuario SET id_rol = ?, nombre = ?, correo = ? WHERE id_usuario = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, entidad.getIdRol());
            ps.setString(2, entidad.getNombre());
            ps.setString(3, entidad.getCorreo());
            ps.setInt(4, entidad.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM contrasena WHERE id_usuario = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            int filas;
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuario WHERE id_usuario = ?")) {
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

    /**
     * Devuelve el usuario si las credenciales coinciden, o null si no existe.
     * La comparación se hace contra el hash SHA-256 guardado en CONTRASENA.
     */
    public Usuario login(String correo, String contrasena) {
        String sql = SELECT_BASE
                + " JOIN contrasena c ON c.id_usuario = u.id_usuario"
                + " WHERE u.correo = ? AND c.hash_password = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo.trim());
            ps.setString(2, PasswordUtils.sha256(contrasena));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al intentar realizar el login.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Indica si ya existe un usuario registrado con ese correo (para no duplicar cuentas).
     */
    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE correo = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo.trim());
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

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setIdRol(rs.getInt("id_rol"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setNombreRol(rs.getString("nombre_rol"));
        return u;
    }
}
