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

/**
 * Clase de Acceso a Datos (DAO) para gestionar las operaciones relativas a los usuarios.
 * Administra la persistencia de datos en Oracle DB, transacciones complejas de alta y baja,
 * autenticación de credenciales y consulta de relaciones.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
public class UsuarioDao implements Dao<Usuario, Integer> {

    /** Consulta SQL base que realiza un JOIN entre las tablas USUARIO y ROL. */
    private static final String SELECT_BASE =
            "SELECT u.id_usuario, u.id_rol, u.nombre, u.correo, r.nombre_rol "
                    + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol";

    /** Código de error de Oracle cuando una restricción de llave foránea impide eliminar un registro padre. */
    private static final int ORA_HIJO_ENCONTRADO = 2292;

    /**
     * Registro auxiliar que representa el resultado detallado del proceso de eliminación de un usuario.
     *
     * @param ok    Indica si la eliminación se realizó con éxito.
     * @param error Clave del motivo del fallo si la operación no fue exitosa.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public record Baja(boolean ok, String error) {
        /**
         * Retorna un objeto Baja representando éxito.
         *
         * @return Instancia de {@link Baja} exitosa.
         * @author Hugo Alberto Ramirez Martinez
         * @since 25/08/2026
         */
        static Baja exitosa() {
            return new Baja(true, null);
        }

        /**
         * Retorna un objeto Baja representando fallo.
         *
         * @param error Clave del error.
         * @return Instancia de {@link Baja} fallida.
         * @author Hugo Alberto Ramirez Martinez
         * @since 25/08/2026
         */
        static Baja fallida(String error) {
            return new Baja(false, error);
        }
    }

    /**
     * Registro que contiene el conteo de entidades asociadas al historial de un usuario.
     *
     * @param solicitudes     Total de solicitudes creadas por el usuario.
     * @param reportes        Total de reportes asociados a las solicitudes del usuario.
     * @param autorizaciones  Total de solicitudes autorizadas por el usuario.
     * @param acompanamientos Total de visitas en las que participó como acompañante.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public record Historial(int solicitudes, int reportes, int autorizaciones, int acompanamientos) {
        /**
         * Evalúa si la eliminación provocará la pérdida permanente de datos de solicitudes o reportes.
         *
         * @return {@code true} si se destruyen solicitudes o reportes; {@code false} en caso contrario.
         * @author Hugo Alberto Ramirez Martinez
         * @since 25/08/2026
         */
        public boolean destruyeDatos() {
            return solicitudes + reportes > 0;
        }
    }

    /**
     * Crea un usuario y su correspondiente hash de contraseña dentro de una misma transacción base de datos.
     *
     * @param entidad Objeto {@link Usuario} que contiene la información a registrar.
     * @return {@code true} si la transacción fue exitosa; {@code false} si ocurrió un error y se ejecutó un rollback.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    @Override
    public boolean create(Usuario entidad) {
        String sqlUsuario = "INSERT INTO usuario (id_rol, nombre, correo) VALUES (?, ?, ?)";
        String sqlContrasena = "INSERT INTO contrasena (id_usuario, hash_password) VALUES (?, ?)";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idUsuario;
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, new String[]{"ID_USUARIO"})) {
                ps.setInt(1, entidad.getIdRol());
                ps.setString(2, entidad.getNombre());
                ps.setString(3, entidad.getCorreo());
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

    /**
     * Obtiene la lista completa de usuarios registrados.
     *
     * @return Lista de objetos {@link Usuario}.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
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

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id Identificador único del usuario.
     * @return Objeto {@link Usuario} encontrado o {@code null} si no existe.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
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

    /**
     * Actualiza la información personal y el rol de un usuario existente.
     *
     * @param entidad Objeto {@link Usuario} con los datos actualizados.
     * @return {@code true} si se actualizó el registro; {@code false} en caso contrario.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
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
     * Elimina a un usuario y sus entidades dependientes de la base de datos.
     *
     * @param id Identificador único del usuario.
     * @return {@code true} si la eliminación se realizó con éxito.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    @Override
    public boolean delete(Integer id) {
        return eliminarConDetalle(id).ok();
    }

    /**
     * Realiza el borrado en cascada de la cuenta del usuario y sus registros relacionados,
     * desligando autorizaciones o acompañamientos de terceros dentro de una sola transacción.
     *
     * @param id Identificador del usuario a eliminar.
     * @return Objeto {@link Baja} con la confirmación o el detalle del fallo.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public Baja eliminarConDetalle(Integer id) {
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

            // 2. Borrar su árbol de solicitudes, de la hoja a la raíz por las FKs
            String[] enCascada = {
                    "DELETE FROM imagen WHERE id_reporte IN "
                            + "(SELECT id_reporte FROM reporte WHERE id_solicitud IN (" + SUS_SOLICITUDES + "))",
                    "DELETE FROM documento WHERE id_reporte IN "
                            + "(SELECT id_reporte FROM reporte WHERE id_solicitud IN (" + SUS_SOLICITUDES + "))",
                    "DELETE FROM reporte WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                    "DELETE FROM documento WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                    "DELETE FROM asignatura_reforzar_solicitud WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
                    "DELETE FROM programa_educativo WHERE id_solicitud IN (" + SUS_SOLICITUDES + ")",
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
            return filas > 0 ? Baja.exitosa() : Baja.fallida("noexiste");

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return Baja.fallida(e.getErrorCode() == ORA_HIJO_ENCONTRADO ? "ligado" : "eliminar");
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
     * Realiza un conteo del historial del usuario (solicitudes, reportes, autorizaciones y acompañamientos)
     * para advertir sobre los datos que serán removidos o modificados.
     *
     * @param idUsuario Identificador único del usuario.
     * @return Objeto {@link Historial} con la cantidad de registros por categoría.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
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
     * Valida el acceso de un usuario comparando su correo y el hash SHA-256 de su contraseña.
     *
     * @param correo     Correo electrónico ingresado.
     * @param contrasena Contraseña en texto plano a verificar.
     * @return Objeto {@link Usuario} correspondiente a las credenciales o {@code null} si son inválidas.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
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
     * Comprueba la existencia previa de un correo electrónico en la base de datos.
     *
     * @param correo Correo electrónico a consultar.
     * @return {@code true} si ya se encuentra registrado; {@code false} en caso contrario.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
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
     * Consulta un usuario mediante su correo electrónico ignorando mayúsculas y minúsculas.
     *
     * @param correo Correo electrónico a buscar.
     * @return Objeto {@link Usuario} si existe coincidencia o {@code null} si no.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public Usuario getByCorreo(String correo) {
        String sql = SELECT_BASE + " WHERE UPPER(u.correo) = UPPER(?)";
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
     * Actualiza o inserta mediante un {@code MERGE} la contraseña hash de un usuario determinado.
     *
     * @param idUsuario            Identificador único del usuario.
     * @param nuevaContrasenaPlano Nueva contraseña en texto plano.
     * @return {@code true} si la operación fue ejecutada con éxito.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public boolean actualizarContrasena(int idUsuario, String nuevaContrasenaPlano) {
        String sql = "MERGE INTO contrasena c "
                + "USING (SELECT ? AS id_usuario, ? AS hash_password FROM dual) nuevo "
                + "ON (c.id_usuario = nuevo.id_usuario) "
                + "WHEN MATCHED THEN UPDATE SET c.hash_password = nuevo.hash_password, "
                + "                            c.fecha_actualizacion = SYSDATE "
                + "WHEN NOT MATCHED THEN INSERT (id_usuario, hash_password, fecha_actualizacion) "
                + "                      VALUES (nuevo.id_usuario, nuevo.hash_password, SYSDATE)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, PasswordUtils.sha256(nuevaContrasenaPlano));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Realiza búsquedas de usuarios con el rol 'Docente' cuyo nombre o correo coincidan con un criterio dado.
     *
     * @param texto             Texto de búsqueda.
     * @param excluirIdUsuario  ID del usuario que se desea ignorar en los resultados.
     * @param limite            Límite de registros a retornar.
     * @return Lista de objetos {@link Usuario} tipo Docente.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
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

    /**
     * Mapea el registro actual de un {@link ResultSet} a una instancia de {@link Usuario}.
     *
     * @param rs Cursor {@link ResultSet} posicionado en la fila actual.
     * @return Instancia poblada de {@link Usuario}.
     * @throws SQLException Si ocurre un error al extraer las columnas.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
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