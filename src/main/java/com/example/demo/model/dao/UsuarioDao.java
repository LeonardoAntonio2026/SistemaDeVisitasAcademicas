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
     * Crea el usuario y guarda el hash de su contraseña en la tabla CONTRASENA,
     * todo en una misma transacción. Si la entidad no trae id_rol se usa Docente
     * (registro público); la gestión de usuarios sí lo manda explícito (RF-12).
     */
    @Override
    public boolean create(Usuario entidad) {
        boolean conRolExplicito = entidad.getIdRol() > 0;
        String sqlUsuario = conRolExplicito
                ? "INSERT INTO usuario (id_rol, nombre, correo) VALUES (?, ?, ?)"
                : "INSERT INTO usuario (id_rol, nombre, correo) "
                        + "VALUES ((SELECT id_rol FROM rol WHERE nombre_rol = 'Docente'), ?, ?)";
        String sqlContrasena = "INSERT INTO contrasena (id_usuario, hash_password) VALUES (?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuario;
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, new String[]{"ID_USUARIO"})) {
                int i = 1;
                if (conRolExplicito) {
                    ps.setInt(i++, entidad.getIdRol());
                }
                ps.setString(i++, entidad.getNombre());
                ps.setString(i, entidad.getCorreo());
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

    /**
     * Da de baja la cuenta y borra TODO lo que el usuario generó: sus solicitudes
     * con su desglose, sus reportes, y los documentos e imágenes de ambos. Es
     * irreversible; todo va en una transacción, así que o se borra completo o no
     * se borra nada.
     *
     * Lo que NO destruye es el trabajo de OTROS: si evaluó solicitudes ajenas
     * solo se quita su firma (id_usuario_autoriza queda en NULL) y si acompañó
     * visitas de otro docente solo se quita de la lista de acompañantes. Borrar
     * un coordinador de Estadías no puede llevarse las solicitudes de los demás.
     *
     * Usa {@link #contarHistorial(int)} antes para advertir qué se va a perder.
     */
    @Override
    public boolean delete(Integer id) {
        // Subconsulta reutilizada: las solicitudes de las que el usuario es dueño
        final String SUS_SOLICITUDES = "SELECT id_solicitud FROM solicitud WHERE id_usuario_solicitante = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            // 1. Desligarlo de lo ajeno, sin borrarlo
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE solicitud SET id_usuario_autoriza = NULL WHERE id_usuario_autoriza = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM solicitud_docente WHERE id_usuario = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // 2. Borrar su árbol de solicitudes, de la hoja a la raíz por las FKs:
            //    imagen/documento -> reporte -> documento/hijos -> solicitud
            String[] enCascada = {
                "DELETE FROM imagen WHERE id_reporte IN "
                    + "(SELECT id_reporte FROM reporte WHERE id_solicitud IN (" + SUS_SOLICITUDES + "))",
                "DELETE FROM documento WHERE id_reporte IN "
                    + "(SELECT id_reporte FROM reporte WHERE id_solicitud IN (" + SUS_SOLICITUDES + "))",
                "DELETE FROM reporte WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                "DELETE FROM documento WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                "DELETE FROM asignatura_reforzar_solicitud WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                "DELETE FROM programa_educativo WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                "DELETE FROM estudiantes_division WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                "DELETE FROM solicitud_docente WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                "DELETE FROM solicitud WHERE id_usuario_solicitante = ?"
            };
            for (String sql : enCascada) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            // 3. Sus datos de acceso y, por último, la cuenta
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM token_recuperacion WHERE id_usuario = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
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
     * Lo que hay detrás de un usuario. {@code solicitudes} y {@code reportes} se
     * DESTRUYEN al darlo de baja; {@code autorizaciones} y {@code acompanamientos}
     * son trabajo de otros y solo se desligan.
     */
    public record Historial(int solicitudes, int reportes, int autorizaciones, int acompanamientos) {
        /** Hay algo que se perdería para siempre al borrar la cuenta. */
        public boolean destruyeDatos() {
            return solicitudes + reportes > 0;
        }
    }

    /** Cuenta lo que se perdería al dar de baja al usuario, para poder advertirlo. */
    public Historial contarHistorial(int idUsuario) {
        String sql = "SELECT "
                + " (SELECT COUNT(*) FROM solicitud WHERE id_usuario_solicitante = ?),"
                + " (SELECT COUNT(*) FROM reporte WHERE id_solicitud IN"
                + "     (SELECT id_solicitud FROM solicitud WHERE id_usuario_solicitante = ?)),"
                + " (SELECT COUNT(*) FROM solicitud WHERE id_usuario_autoriza = ?),"
                + " (SELECT COUNT(*) FROM solicitud_docente WHERE id_usuario = ?)"
                + " FROM dual";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setInt(i, idUsuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Historial(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Historial(0, 0, 0, 0);
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
    /**
     * Trae el usuario por correo, con todos sus datos (para RF-02: generar el
     * token de recuperación necesitamos su id, sin exponerlo en la vista).
     */
    public Usuario getByCorreo(String correo) {
        String sql = SELECT_BASE + " WHERE u.correo = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo.trim());
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

    /**
     * Actualiza el hash guardado en CONTRASENA tras un restablecimiento (RF-02).
     * TODO(coordinar con Dev C): si CONTRASENA agrega columna de salt, aquí es
     * donde hay que generarlo y guardarlo junto con el hash (hoy es solo SHA-256
     * plano, igual que en create() y login()).
     */
    public boolean actualizarContrasena(int idUsuario, String nuevaContrasenaPlano) {
        String sql = "UPDATE contrasena SET hash_password = ? WHERE id_usuario = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, PasswordUtils.sha256(nuevaContrasenaPlano));
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Docentes cuyo nombre o correo empatan con el texto escrito, para el
     * autocompletado de docentes acompañantes. Se excluye al usuario indicado
     * (el responsable no se acompaña a sí mismo).
     */
    public List<Usuario> buscarDocentes(String texto, Integer excluirIdUsuario, int limite) {
        List<Usuario> datos = new ArrayList<>();
        String sql = SELECT_BASE
                + " WHERE r.nombre_rol = 'Docente'"
                + " AND (UPPER(u.nombre) LIKE UPPER(?) OR UPPER(u.correo) LIKE UPPER(?))"
                + (excluirIdUsuario != null ? " AND u.id_usuario <> ?" : "")
                + " ORDER BY u.nombre FETCH FIRST ? ROWS ONLY";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String patron = "%" + (texto != null ? texto.trim() : "") + "%";
            int i = 1;
            ps.setString(i++, patron);
            ps.setString(i++, patron);
            if (excluirIdUsuario != null) {
                ps.setInt(i++, excluirIdUsuario);
            }
            ps.setInt(i, limite);

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
