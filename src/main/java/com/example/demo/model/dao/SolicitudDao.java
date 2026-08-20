package com.example.demo.model.dao;

import com.example.demo.model.ProgramaEducativo;
import com.example.demo.model.Solicitud;
import com.example.demo.model.Usuario;
import com.example.demo.utils.SQLConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Objeto de Acceso a Datos (DAO) para la gestión persistence de solicitudes de visita académica.
 * <p>
 * Centraliza las operaciones CRUD y las consultas especializadas sobre la tabla {@code solicitud},
 * coordinando transaccionalmente la inserción, actualización y eliminación en cascada de sus tablas
 * hijas relacionales (programas educativos, asignaturas a reforzar, distribución por división
 * y docentes acompañantes).
 * </p>
 *
 * @author Eder Gabriel García Vázquez
 * @since 18/08/2026
 */
public class SolicitudDao implements Dao<Solicitud, Integer> {

    /**
     * Consulta base reutilizable para la proyección de campos de la solicitud.
     * Incluye subconsultas estratégicas para totales agregados e información
     * del reporte asociado sin requerir múltiples JOINs que dupliquen filas.
     */
    private static final String SELECT_BASE =
            "SELECT s.id_solicitud, s.id_usuario_solicitante, s.id_usuario_autoriza, "
                    + "s.nombre_empresa_actividad, s.lugar_direccion, s.telefono_contacto, s.correo_contacto, "
                    + "TO_CHAR(s.fecha_inicio, 'YYYY-MM-DD') AS fecha_inicio, s.objetivo, s.area_solicitante, "
                    + "s.docente_responsable, s.celular_responsable, "
                    + "s.id_estado, s.detalles_decision, TO_CHAR(s.fecha_creacion, 'YYYY-MM-DD') AS fecha_creacion, "
                    + "e.nombre_estado, us.nombre AS nombre_solicitante, us.correo AS correo_solicitante, "
                    // Quien autorizó, para la firma "Autoriza" del formato impreso
                    + "ua.nombre AS nombre_autoriza, "
                    + "NVL((SELECT SUM(p.no_estudiantes) FROM programa_educativo p WHERE p.id_solicitud = s.id_solicitud), 0) AS total_estudiantes, "
                    // Estado del reporte de la visita; null si todavía no se generó.
                    // Va como subselect y no como JOIN para no duplicar filas.
                    + "(SELECT er.nombre_estado FROM reporte r "
                    + "JOIN estado_reporte er ON er.id_estado = r.id_estado "
                    + "WHERE r.id_solicitud = s.id_solicitud AND ROWNUM = 1) AS estado_reporte, "
                    // Id del reporte, para enlazar directo a /reporte?id=X desde el histórico
                    + "(SELECT r.id_reporte FROM reporte r "
                    + "WHERE r.id_solicitud = s.id_solicitud AND ROWNUM = 1) AS id_reporte "
                    + "FROM solicitud s "
                    + "JOIN estado_solicitud e ON e.id_estado = s.id_estado "
                    + "JOIN usuario us ON us.id_usuario = s.id_usuario_solicitante "
                    // LEFT: mientras Estadías no decide, id_usuario_autoriza es null y
                    // un JOIN normal dejaría fuera todas las solicitudes en trámite
                    + "LEFT JOIN usuario ua ON ua.id_usuario = s.id_usuario_autoriza";

    /**
     * Registra una nueva solicitud en la base de datos junto con sus registros dependientes.
     * <p>
     * Maneja una transacción ACID manual para garantizar que tanto la cabecera de la solicitud
     * como las listas de programas, asignaturas, docentes y desgloses por división se persistan
     * correctamente o se reviertan totalmente en caso de fallo.
     * </p>
     *
     * @param entidad el objeto {@link Solicitud} con la información capturada.
     * @return {@code true} si el registro fue exitoso; {@code false} si ocurrió un error SQL.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    @Override
    public boolean create(Solicitud entidad) {
        String sqlSolicitud = "INSERT INTO solicitud (id_usuario_solicitante, nombre_empresa_actividad, "
                + "lugar_direccion, telefono_contacto, correo_contacto, fecha_inicio, objetivo, "
                + "area_solicitante, docente_responsable, celular_responsable, id_estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + "(SELECT id_estado FROM estado_solicitud WHERE nombre_estado = 'Pendiente'))";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int idSolicitud;
            try (PreparedStatement ps = con.prepareStatement(sqlSolicitud, new String[]{"ID_SOLICITUD"})) {
                ps.setInt(1, entidad.getIdUsuarioSolicitante());
                ps.setString(2, entidad.getNombreEmpresaActividad());
                ps.setString(3, entidad.getLugarDireccion());
                ps.setString(4, entidad.getTelefonoContacto());
                ps.setString(5, entidad.getCorreoContacto());
                setFecha(ps, 6, entidad.getFechaInicio());
                ps.setString(7, entidad.getObjetivo());
                ps.setString(8, entidad.getAreaSolicitante());
                ps.setString(9, entidad.getDocenteResponsable());
                ps.setString(10, entidad.getCelularResponsable());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    idSolicitud = rs.getInt(1);
                }
            }

            guardarHijos(con, idSolicitud, entidad);

            con.commit();
            entidad.setIdSolicitud(idSolicitud);
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
     * Recupera la lista general de solicitudes ordenadas descendentemente por fecha de creación.
     *
     * @return una lista de objetos {@link Solicitud} con sus datos generales mapeados.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    @Override
    public List<Solicitud> getAll() {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
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
     * Obtiene los detalles completos de una solicitud por su identificador único.
     * <p>
     * Reutiliza una única conexión de base de datos para cargar las tablas hijas
     * (programas, asignaturas, divisiones y acompañantes), optimizando el uso del pool.
     * </p>
     *
     * @param id el identificador de la solicitud.
     * @return el objeto {@link Solicitud} totalmente poblado, o {@code null} si no se encuentra.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    @Override
    public Solicitud getById(Integer id) {
        String sql = SELECT_BASE + " WHERE s.id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            Solicitud solicitud;
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                solicitud = mapRow(rs);
            }

            solicitud.setProgramas(getProgramas(con, id));
            solicitud.setAsignaturas(getAsignaturas(con, id));
            solicitud.setEstudiantesPorDivision(getEstudiantesPorDivision(con, id));
            solicitud.setDocentesAcompanantes(getDocentesAcompanantes(con, id));
            return solicitud;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Consulta todas las solicitudes creadas por un docente específico,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param idUsuario el identificador del docente solicitante.
     * @return una lista de solicitudes pertenecientes al usuario.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<Solicitud> getBySolicitante(int idUsuario) {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.id_usuario_solicitante = ? ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
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
     * Obtiene las solicitudes activas de un docente excluyendo las que han sido "Completadas".
     * <p>
     * Mantiene las solicitudes "Rechazadas" en el listado para permitir su corrección y reenvío (RN-05).
     * </p>
     *
     * @param idUsuario el identificador del docente.
     * @return la lista de solicitudes en curso o pendientes de atender por el docente.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<Solicitud> getActivasBySolicitante(int idUsuario) {
        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE s.id_usuario_solicitante = ? "
                + "AND e.nombre_estado <> 'Completada' "
                + "ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
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
     * Obtiene las solicitudes enviadas que requieren la atención del coordinador de Estadías.
     * Excluye los borradores ("Pendiente") que no han sido remitidos formalmente (RN-02).
     *
     * @return la lista de solicitudes en estado 'En revisión' o 'Aprobada'.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<Solicitud> getActivasParaRevision() {
        return getPorEstados(true, "En revisión", "Aprobada");
    }

    /**
     * Obtiene todas las solicitudes que han sido enviadas formalmente excluyendo borradores en estado 'Pendiente'.
     *
     * @return la lista de solicitudes enviadas al sistema.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<Solicitud> getEnviadas() {
        return getPorEstados(false, "Pendiente");
    }

    /**
     * Recupera el historial de solicitudes concluídas o archivadas.
     *
     * @param idUsuario el ID del docente para filtrar su historial personal,
     *                  o {@code null} para obtener el historial global administrativo.
     * @return la lista de solicitudes terminadas.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public List<Solicitud> getHistorico(Integer idUsuario) {
        List<Solicitud> datos = new ArrayList<>();
        String estados = idUsuario != null ? "('Completada')" : "('Completada', 'Rechazada')";
        String sql = SELECT_BASE + " WHERE e.nombre_estado IN " + estados + " "
                + (idUsuario != null ? "AND s.id_usuario_solicitante = ? " : "")
                + "ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (idUsuario != null) {
                ps.setInt(1, idUsuario);
            }
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
     * Método auxiliar privado para filtrar solicitudes por un conjunto dinámico de nombres de estado.
     *
     * @param incluir {@code true} para usar cláusula {@code IN}, {@code false} para {@code NOT IN}.
     * @param estados varargs con los nombres de estado a comparar.
     * @return la lista de solicitudes filtradas.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private List<Solicitud> getPorEstados(boolean incluir, String... estados) {
        List<Solicitud> datos = new ArrayList<>();

        String marcadores = "";
        for (int i = 0; i < estados.length; i++) {
            marcadores += (i == 0) ? "?" : ", ?";
        }

        String sql = SELECT_BASE
                + " WHERE e.nombre_estado " + (incluir ? "" : "NOT ") + "IN (" + marcadores + ")"
                + " ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < estados.length; i++) {
                ps.setString(i + 1, estados[i]);
            }
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
     * Actualiza únicamente el estado de una solicitud.
     *
     * @param idSolicitud el identificador de la solicitud.
     * @param nombreEstado el nombre del nuevo estado al que se transicionará.
     * @return {@code true} si la actualización fue exitosa; {@code false} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public boolean cambiarEstado(int idSolicitud, String nombreEstado) {
        String sql = "UPDATE solicitud SET id_estado = "
                + "(SELECT id_estado FROM estado_solicitud WHERE nombre_estado = ?) "
                + "WHERE id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreEstado);
            ps.setInt(2, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registra el dictamen administrativo de Estadías (aprobación o rechazo),
     * guardando el usuario autorizador y el motivo justificante (RF-05).
     *
     * @param idSolicitud el ID de la solicitud.
     * @param nombreEstado el nuevo estado dictaminado ('Aprobada' o 'Rechazada').
     * @param motivo la justificación o detalle de la decisión.
     * @param idUsuarioAutoriza el ID del usuario administrativo que dictamina.
     * @return {@code true} si el registro fue exitoso; {@code false} en caso de error.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    public boolean decidir(int idSolicitud, String nombreEstado, String motivo, int idUsuarioAutoriza) {
        String sql = "UPDATE solicitud SET id_estado = "
                + "(SELECT id_estado FROM estado_solicitud WHERE nombre_estado = ?), "
                + "detalles_decision = ?, id_usuario_autoriza = ? WHERE id_solicitud = ?";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreEstado);
            ps.setString(2, motivo);
            ps.setInt(3, idUsuarioAutoriza);
            ps.setInt(4, idSolicitud);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza de manera transaccional una solicitud existente y recrea sus relaciones hijas.
     * Reinicia el estado a 'Pendiente' y limpia las autorizaciones previas al ser modificada.
     *
     * @param entidad el objeto {@link Solicitud} con los nuevos datos.
     * @return {@code true} si la actualización fue correcta; {@code false} en caso contrario.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    @Override
    public boolean update(Solicitud entidad) {
        String sqlSolicitud = "UPDATE solicitud SET nombre_empresa_actividad = ?, lugar_direccion = ?, "
                + "telefono_contacto = ?, correo_contacto = ?, fecha_inicio = ?, objetivo = ?, "
                + "area_solicitante = ?, docente_responsable = ?, celular_responsable = ?, "
                + "id_estado = (SELECT id_estado FROM estado_solicitud WHERE nombre_estado = 'Pendiente'), "
                + "detalles_decision = NULL, id_usuario_autoriza = NULL "
                + "WHERE id_solicitud = ?";

        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            int id = entidad.getIdSolicitud();
            int filas;
            try (PreparedStatement ps = con.prepareStatement(sqlSolicitud)) {
                ps.setString(1, entidad.getNombreEmpresaActividad());
                ps.setString(2, entidad.getLugarDireccion());
                ps.setString(3, entidad.getTelefonoContacto());
                ps.setString(4, entidad.getCorreoContacto());
                setFecha(ps, 5, entidad.getFechaInicio());
                ps.setString(6, entidad.getObjetivo());
                ps.setString(7, entidad.getAreaSolicitante());
                ps.setString(8, entidad.getDocenteResponsable());
                ps.setString(9, entidad.getCelularResponsable());
                ps.setInt(10, id);
                filas = ps.executeUpdate();
            }
            if (filas == 0) {
                con.rollback();
                return false;
            }

            borrarHijos(con, id);
            guardarHijos(con, id, entidad);

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
     * Elimina una solicitud y todos sus registros asociados de forma transaccional.
     *
     * @param id el identificador de la solicitud a eliminar.
     * @return {@code true} si la eliminación se realizó con éxito; {@code false} en caso de error.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    @Override
    public boolean delete(Integer id) {
        Connection con = null;
        try {
            con = SQLConnector.getConnection();
            con.setAutoCommit(false);

            // Primero los hijos por las FKs, después la solicitud
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM documento WHERE id_solicitud = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            borrarHijos(con, id);
            int filas;
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM solicitud WHERE id_solicitud = ?")) {
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
     * Inserta por Lotes (Batch) las tablas hijas relacionales dentro de una transacción activa.
     *
     * @param con la conexión JDBC transaccional.
     * @param idSolicitud el ID de la solicitud padre.
     * @param entidad la solicitud con las colecciones pobladas.
     * @throws SQLException si ocurre un error al ejecutar los batches de inserción.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private void guardarHijos(Connection con, int idSolicitud, Solicitud entidad) throws SQLException {
        if (!entidad.getProgramas().isEmpty()) {
            String sql = "INSERT INTO programa_educativo (id_solicitud, division_academica, cuatrimestre, grupo, no_estudiantes) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (ProgramaEducativo p : entidad.getProgramas()) {
                    ps.setInt(1, idSolicitud);
                    ps.setString(2, p.getDivisionAcademica());
                    ps.setInt(3, p.getCuatrimestre());
                    ps.setString(4, p.getGrupo());
                    ps.setInt(5, p.getNoEstudiantes());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        if (!entidad.getAsignaturas().isEmpty()) {
            String sql = "INSERT INTO asignatura_reforzar_solicitud (id_solicitud, nombre) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (String asignatura : entidad.getAsignaturas()) {
                    ps.setInt(1, idSolicitud);
                    ps.setString(2, asignatura);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        // Solo se guardan las divisiones con estudiantes; las que van en 0 no ocupan fila
        String sqlDivision = "INSERT INTO estudiantes_division (id_solicitud, division, no_estudiantes) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlDivision)) {
            boolean hayFilas = false;
            for (Map.Entry<String, Integer> e : entidad.getEstudiantesPorDivision().entrySet()) {
                int cantidad = e.getValue() != null ? e.getValue() : 0;
                if (cantidad <= 0) {
                    continue;
                }
                ps.setInt(1, idSolicitud);
                ps.setString(2, e.getKey());
                ps.setInt(3, cantidad);
                ps.addBatch();
                hayFilas = true;
            }
            if (hayFilas) {
                ps.executeBatch();
            }
        }

        if (!entidad.getDocentesAcompanantes().isEmpty()) {
            String sql = "INSERT INTO solicitud_docente (id_solicitud, id_usuario) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Usuario docente : entidad.getDocentesAcompanantes()) {
                    ps.setInt(1, idSolicitud);
                    ps.setInt(2, docente.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    /**
     * Asigna un parámetro de tipo fecha en un {@link PreparedStatement},
     * manejando la conversión de cadenas vacías a valores {@code NULL} de SQL.
     *
     * @param ps el statement preparado.
     * @param indice el índice del parámetro en la consulta SQL.
     * @param iso la representación de la fecha en texto con formato ISO (yyyy-MM-dd).
     * @throws SQLException si falla la asignación de parámetros.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private void setFecha(PreparedStatement ps, int indice, String iso) throws SQLException {
        if (iso != null && !iso.isBlank()) {
            ps.setDate(indice, Date.valueOf(iso.trim()));
        } else {
            ps.setNull(indice, java.sql.Types.DATE);
        }
    }

    /**
     * Elimina los registros hijos dependientes de la solicitud para preparar su actualización o borrado total.
     *
     * @param con la conexión JDBC con la transacción en curso.
     * @param idSolicitud el identificador de la solicitud.
     * @throws SQLException si ocurre un fallo al ejecutar la eliminación.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private void borrarHijos(Connection con, int idSolicitud) throws SQLException {
        String[] tablas = {"asignatura_reforzar_solicitud", "programa_educativo",
                "estudiantes_division", "solicitud_docente"};
        for (String tabla : tablas) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM " + tabla + " WHERE id_solicitud = ?")) {
                ps.setInt(1, idSolicitud);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Carga el desglose de estudiantes distribuidos por divisiones académicas.
     *
     * @param con la conexión JDBC activa.
     * @param idSolicitud el identificador de la solicitud.
     * @return un mapa estructurado con las divisiones y su conteo correspondiente.
     * @throws SQLException si sucede un error de lectura.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private Map<String, Integer> getEstudiantesPorDivision(Connection con, int idSolicitud) throws SQLException {
        Map<String, Integer> divisiones = Solicitud.divisionesEnCero();
        String sql = "SELECT division, no_estudiantes FROM estudiantes_division WHERE id_solicitud = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    divisiones.put(rs.getString("division"), rs.getInt("no_estudiantes"));
                }
            }
        }
        return divisiones;
    }

    /**
     * Obtiene la lista de docentes acompañantes vinculados a la solicitud.
     *
     * @param con la conexión JDBC activa.
     * @param idSolicitud el identificador de la solicitud.
     * @return una lista de objetos {@link Usuario} asignados como acompañantes.
     * @throws SQLException si falla la lectura de datos.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private List<Usuario> getDocentesAcompanantes(Connection con, int idSolicitud) throws SQLException {
        List<Usuario> docentes = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre, u.correo FROM solicitud_docente sd "
                + "JOIN usuario u ON u.id_usuario = sd.id_usuario "
                + "WHERE sd.id_solicitud = ? ORDER BY u.nombre";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
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
        }
        return docentes;
    }

    /**
     * Obtiene la lista de programas educativos vinculados a la solicitud.
     *
     * @param con la conexión JDBC activa.
     * @param idSolicitud el ID de la solicitud.
     * @return la lista de programas educativos agregados.
     * @throws SQLException si ocurre un error durante la consulta.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private List<ProgramaEducativo> getProgramas(Connection con, int idSolicitud) throws SQLException {
        List<ProgramaEducativo> programas = new ArrayList<>();
        String sql = "SELECT id_programa, id_solicitud, division_academica, cuatrimestre, grupo, no_estudiantes "
                + "FROM programa_educativo WHERE id_solicitud = ? ORDER BY id_programa";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProgramaEducativo p = new ProgramaEducativo();
                    p.setIdPrograma(rs.getInt("id_programa"));
                    p.setIdSolicitud(rs.getInt("id_solicitud"));
                    p.setDivisionAcademica(rs.getString("division_academica"));
                    p.setCuatrimestre(rs.getInt("cuatrimestre"));
                    p.setGrupo(rs.getString("grupo"));
                    p.setNoEstudiantes(rs.getInt("no_estudiantes"));
                    programas.add(p);
                }
            }
        }
        return programas;
    }

    /**
     * Obtiene la lista de nombres de asignaturas a reforzar ligadas a la solicitud.
     *
     * @param con la conexión JDBC activa.
     * @param idSolicitud el ID de la solicitud.
     * @return una lista de cadenas con los nombres de las asignaturas.
     * @throws SQLException si sucede un fallo durante la consulta.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private List<String> getAsignaturas(Connection con, int idSolicitud) throws SQLException {
        List<String> asignaturas = new ArrayList<>();
        String sql = "SELECT nombre FROM asignatura_reforzar_solicitud WHERE id_solicitud = ? ORDER BY id_asignatura";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    asignaturas.add(rs.getString("nombre"));
                }
            }
        }
        return asignaturas;
    }

    /**
     * Mapea el registro actual de un {@link ResultSet} a un nuevo objeto entidad {@link Solicitud}.
     *
     * @param rs el cursor activo de la consulta SQL.
     * @return un objeto {@link Solicitud} cargado con los datos de la fila.
     * @throws SQLException si ocurre un fallo al extraer los datos del ResultSet.
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private Solicitud mapRow(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdUsuarioSolicitante(rs.getInt("id_usuario_solicitante"));
        int autoriza = rs.getInt("id_usuario_autoriza");
        s.setIdUsuarioAutoriza(rs.wasNull() ? null : autoriza);
        s.setNombreEmpresaActividad(rs.getString("nombre_empresa_actividad"));
        s.setLugarDireccion(rs.getString("lugar_direccion"));
        s.setTelefonoContacto(rs.getString("telefono_contacto"));
        s.setCorreoContacto(rs.getString("correo_contacto"));
        s.setFechaInicio(rs.getString("fecha_inicio"));
        s.setObjetivo(rs.getString("objetivo"));
        s.setAreaSolicitante(rs.getString("area_solicitante"));
        s.setDocenteResponsable(rs.getString("docente_responsable"));
        s.setCelularResponsable(rs.getString("celular_responsable"));
        s.setIdEstado(rs.getInt("id_estado"));
        s.setDetallesDecision(rs.getString("detalles_decision"));
        s.setFechaCreacion(rs.getString("fecha_creacion"));
        s.setNombreEstado(rs.getString("nombre_estado"));
        s.setEstadoReporte(rs.getString("estado_reporte"));
        int idReporte = rs.getInt("id_reporte");
        s.setIdReporte(rs.wasNull() ? null : idReporte);
        s.setNombreSolicitante(rs.getString("nombre_solicitante"));
        s.setCorreoSolicitante(rs.getString("correo_solicitante"));
        s.setNombreAutoriza(rs.getString("nombre_autoriza"));
        s.setTotalEstudiantes(rs.getInt("total_estudiantes"));
        return s;
    }
}