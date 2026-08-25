package com.example.demo.model.dao;

import com.example.demo.model.AsignaturaReforzar;
import com.example.demo.model.ProgramaEducativo;
import com.example.demo.model.Usuario;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD fila por fila de las tres entidades que cuelgan de una solicitud:
 * grupos (PROGRAMA_EDUCATIVO), asignaturas a reforzar
 * (ASIGNATURA_REFORZAR_SOLICITUD) y docentes acompañantes (SOLICITUD_DOCENTE,
 * la tabla puente con USUARIO).
 * <p>
 * SolicitudDao ya toca esas mismas tablas, pero en bloque: al guardar una
 * solicitud borra todos los hijos y los vuelve a insertar, porque el formulario
 * manda el desglose completo de una sola vez. Aquí se opera sobre un renglón a
 * la vez, que es lo que necesita el panel de desglose para agregar, editar o
 * quitar un grupo sin volver a mandar los demás.
 * </p>
 * <p>
 * Todas las operaciones de escritura llevan el id de la solicitud en el WHERE,
 * además del id del renglón. El id del renglón viaja en el mensaje JSON y el
 * navegador lo puede alterar; con las dos condiciones, un id ajeno no afecta
 * ninguna fila en vez de tocar el desglose de otra solicitud.
 * </p>
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 25/08/2026
 */
public class DesgloseDao {

    // ==================== Grupos (programa_educativo) ====================

    private static final String SELECT_GRUPOS =
            "SELECT id_programa, id_solicitud, division_academica, cuatrimestre, grupo, no_estudiantes "
            + "FROM programa_educativo WHERE id_solicitud = ?";

    /**
     * Grupos capturados en la solicitud, en el orden en que se agregaron.
     *
     * @param idSolicitud solicitud dueña de los grupos
     * @return la lista de grupos; vacía si no hay o si falló la consulta
     */
    public List<ProgramaEducativo> getGrupos(int idSolicitud) {
        List<ProgramaEducativo> grupos = new ArrayList<>();
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_GRUPOS + " ORDER BY id_programa")) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grupos.add(mapGrupo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grupos;
    }

    /**
     * Un grupo en concreto, para precargar el modal de edición.
     *
     * @param idSolicitud solicitud dueña del grupo
     * @param idPrograma  llave del grupo
     * @return el grupo, o {@code null} si ese id no es de esta solicitud
     */
    public ProgramaEducativo getGrupo(int idSolicitud, int idPrograma) {
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_GRUPOS + " AND id_programa = ?")) {

            ps.setInt(1, idSolicitud);
            ps.setInt(2, idPrograma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapGrupo(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Da de alta un grupo y deja su id generado en la entidad.
     *
     * @param grupo grupo a insertar, ya con su idSolicitud
     * @return {@code true} si se insertó
     */
    public boolean crearGrupo(ProgramaEducativo grupo) {
        String sql = "INSERT INTO programa_educativo "
                + "(id_solicitud, division_academica, cuatrimestre, grupo, no_estudiantes) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_PROGRAMA"})) {

            ps.setInt(1, grupo.getIdSolicitud());
            ps.setString(2, grupo.getPrograma());
            ps.setInt(3, grupo.getCuatrimestre());
            ps.setString(4, grupo.getGrupo());
            ps.setInt(5, grupo.getNoEstudiantes());

            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    grupo.setIdPrograma(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Guarda los cambios de un grupo.
     *
     * @param grupo grupo con su id, su idSolicitud y los valores nuevos
     * @return {@code true} si se actualizó alguna fila
     */
    public boolean actualizarGrupo(ProgramaEducativo grupo) {
        String sql = "UPDATE programa_educativo SET division_academica = ?, cuatrimestre = ?, "
                + "grupo = ?, no_estudiantes = ? WHERE id_programa = ? AND id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, grupo.getPrograma());
            ps.setInt(2, grupo.getCuatrimestre());
            ps.setString(3, grupo.getGrupo());
            ps.setInt(4, grupo.getNoEstudiantes());
            ps.setInt(5, grupo.getIdPrograma());
            ps.setInt(6, grupo.getIdSolicitud());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Borrado normal (físico) de un grupo: la tabla no guarda historial y un
     * grupo que ya no va a la visita no tiene por qué seguir contando en el
     * total de estudiantes del formato oficial.
     *
     * @param idSolicitud solicitud dueña del grupo
     * @param idPrograma  llave del grupo
     * @return {@code true} si se borró alguna fila
     */
    public boolean eliminarGrupo(int idSolicitud, int idPrograma) {
        String sql = "DELETE FROM programa_educativo WHERE id_programa = ? AND id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPrograma);
            ps.setInt(2, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Indica si la solicitud ya tiene ese mismo programa, cuatrimestre y grupo.
     * <p>
     * La combinación identifica al grupo real que sale de visita, así que
     * capturarla dos veces duplicaría a los mismos estudiantes en el total del
     * FO-UTEZ-EST-08. Es la misma regla que valida SolicitudServlet sobre el
     * formulario completo, aplicada aquí renglón por renglón.
     * </p>
     *
     * @param idSolicitud  solicitud donde se busca
     * @param programa     nombre del programa educativo
     * @param cuatrimestre cuatrimestre del grupo
     * @param grupo        letra del grupo
     * @param idExcluir    id del grupo que se está editando, para que no choque
     *                     consigo mismo; {@code null} al dar de alta
     * @return {@code true} si ya existe otro renglón igual
     */
    public boolean existeGrupo(int idSolicitud, String programa, int cuatrimestre,
                               String grupo, Integer idExcluir) {
        String sql = "SELECT COUNT(*) FROM programa_educativo WHERE id_solicitud = ? "
                + "AND UPPER(TRIM(division_academica)) = UPPER(TRIM(?)) AND cuatrimestre = ? "
                + "AND UPPER(TRIM(grupo)) = UPPER(TRIM(?))"
                + (idExcluir != null ? " AND id_programa <> ?" : "");
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setString(2, programa);
            ps.setInt(3, cuatrimestre);
            ps.setString(4, grupo);
            if (idExcluir != null) {
                ps.setInt(5, idExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ProgramaEducativo mapGrupo(ResultSet rs) throws SQLException {
        ProgramaEducativo p = new ProgramaEducativo();
        p.setIdPrograma(rs.getInt("id_programa"));
        p.setIdSolicitud(rs.getInt("id_solicitud"));
        p.setDivisionAcademica(rs.getString("division_academica"));
        p.setCuatrimestre(rs.getInt("cuatrimestre"));
        p.setGrupo(rs.getString("grupo"));
        p.setNoEstudiantes(rs.getInt("no_estudiantes"));
        return p;
    }

    // ==================== Asignaturas a reforzar ====================

    /**
     * Asignaturas de la solicitud, en el orden en que se agregaron.
     *
     * @param idSolicitud solicitud dueña de las asignaturas
     * @return la lista de asignaturas; vacía si no hay o si falló la consulta
     */
    public List<AsignaturaReforzar> getAsignaturas(int idSolicitud) {
        List<AsignaturaReforzar> asignaturas = new ArrayList<>();
        String sql = "SELECT id_asignatura, id_solicitud, nombre FROM asignatura_reforzar_solicitud "
                + "WHERE id_solicitud = ? ORDER BY id_asignatura";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AsignaturaReforzar a = new AsignaturaReforzar();
                    a.setIdAsignatura(rs.getInt("id_asignatura"));
                    a.setIdSolicitud(rs.getInt("id_solicitud"));
                    a.setNombre(rs.getString("nombre"));
                    asignaturas.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return asignaturas;
    }

    /**
     * Da de alta una asignatura y deja su id generado en la entidad.
     *
     * @param asignatura asignatura a insertar, ya con su idSolicitud
     * @return {@code true} si se insertó
     */
    public boolean crearAsignatura(AsignaturaReforzar asignatura) {
        String sql = "INSERT INTO asignatura_reforzar_solicitud (id_solicitud, nombre) VALUES (?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_ASIGNATURA"})) {

            ps.setInt(1, asignatura.getIdSolicitud());
            ps.setString(2, asignatura.getNombre());

            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    asignatura.setIdAsignatura(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cambia el nombre de una asignatura.
     *
     * @param asignatura asignatura con su id, su idSolicitud y el nombre nuevo
     * @return {@code true} si se actualizó alguna fila
     */
    public boolean actualizarAsignatura(AsignaturaReforzar asignatura) {
        String sql = "UPDATE asignatura_reforzar_solicitud SET nombre = ? "
                + "WHERE id_asignatura = ? AND id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, asignatura.getNombre());
            ps.setInt(2, asignatura.getIdAsignatura());
            ps.setInt(3, asignatura.getIdSolicitud());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Borrado normal (físico) de una asignatura.
     *
     * @param idSolicitud  solicitud dueña de la asignatura
     * @param idAsignatura llave de la asignatura
     * @return {@code true} si se borró alguna fila
     */
    public boolean eliminarAsignatura(int idSolicitud, int idAsignatura) {
        String sql = "DELETE FROM asignatura_reforzar_solicitud "
                + "WHERE id_asignatura = ? AND id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAsignatura);
            ps.setInt(2, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Indica si la solicitud ya tiene esa asignatura, sin distinguir
     * mayúsculas ni espacios de sobra.
     *
     * @param idSolicitud solicitud donde se busca
     * @param nombre      nombre de la asignatura
     * @param idExcluir   id de la asignatura que se está editando, para que no
     *                    choque consigo misma; {@code null} al dar de alta
     * @return {@code true} si ya existe otra asignatura con ese nombre
     */
    public boolean existeAsignatura(int idSolicitud, String nombre, Integer idExcluir) {
        String sql = "SELECT COUNT(*) FROM asignatura_reforzar_solicitud WHERE id_solicitud = ? "
                + "AND UPPER(TRIM(nombre)) = UPPER(TRIM(?))"
                + (idExcluir != null ? " AND id_asignatura <> ?" : "");
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setString(2, nombre);
            if (idExcluir != null) {
                ps.setInt(3, idExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== Docentes acompañantes ====================

    /**
     * Docentes acompañantes de la solicitud, con su nombre y correo.
     *
     * @param idSolicitud solicitud de la que se leen los acompañantes
     * @return la lista de docentes ordenada por nombre; vacía si no hay
     */
    public List<Usuario> getDocentes(int idSolicitud) {
        List<Usuario> docentes = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre, u.correo FROM solicitud_docente sd "
                + "JOIN usuario u ON u.id_usuario = sd.id_usuario "
                + "WHERE sd.id_solicitud = ? ORDER BY u.nombre";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCorreo(rs.getString("correo"));
                    docentes.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docentes;
    }

    /**
     * Liga un docente a la solicitud.
     *
     * @param idSolicitud solicitud a la que se liga
     * @param idUsuario   docente acompañante
     * @return {@code true} si se insertó la fila
     */
    public boolean agregarDocente(int idSolicitud, int idUsuario) {
        String sql = "INSERT INTO solicitud_docente (id_solicitud, id_usuario) VALUES (?, ?)";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cambia un acompañante por otro.
     * <p>
     * SOLICITUD_DOCENTE es una tabla puente: sus dos columnas forman la llave
     * primaria, así que "actualizar" es mover el renglón de un docente a otro.
     * Va como UPDATE y no como borrar más insertar para que la fila no
     * desaparezca ni un instante si algo falla a la mitad.
     * </p>
     *
     * @param idSolicitud solicitud donde está el acompañante
     * @param idAnterior  docente que estaba ligado
     * @param idNuevo     docente que lo reemplaza
     * @return {@code true} si se actualizó la fila
     */
    public boolean cambiarDocente(int idSolicitud, int idAnterior, int idNuevo) {
        String sql = "UPDATE solicitud_docente SET id_usuario = ? "
                + "WHERE id_solicitud = ? AND id_usuario = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNuevo);
            ps.setInt(2, idSolicitud);
            ps.setInt(3, idAnterior);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Desliga a un docente de la solicitud. Borrado normal, pero solo de la
     * tabla puente: la cuenta del docente no se toca.
     *
     * @param idSolicitud solicitud de la que se quita
     * @param idUsuario   docente a quitar
     * @return {@code true} si se borró la fila
     */
    public boolean quitarDocente(int idSolicitud, int idUsuario) {
        String sql = "DELETE FROM solicitud_docente WHERE id_solicitud = ? AND id_usuario = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Indica si el docente ya está ligado a la solicitud. Se consulta antes de
     * insertar: la llave primaria compuesta rechazaría el duplicado, pero con
     * un error de Oracle en vez de un mensaje que el usuario entienda.
     *
     * @param idSolicitud solicitud donde se busca
     * @param idUsuario   docente a buscar
     * @return {@code true} si ya está ligado
     */
    public boolean existeDocente(int idSolicitud, int idUsuario) {
        String sql = "SELECT COUNT(*) FROM solicitud_docente WHERE id_solicitud = ? AND id_usuario = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
