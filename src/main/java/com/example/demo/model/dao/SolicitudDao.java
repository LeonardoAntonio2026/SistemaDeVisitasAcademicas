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

public class SolicitudDao implements Dao<Solicitud, Integer> {

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
     * La solicitud con todo su desglose. Las tablas hijas se consultan con la
     * MISMA conexión que la solicitud: antes cada una pedía la suya al pool, y
     * armar un solo detalle tenía 5 conexiones ocupadas de las 10 que hay.
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
     * Solicitudes creadas por un docente, la más reciente primero (para las tarjetas del inicio).
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
     * Solicitudes activas de un docente (las Completadas se van al histórico y
     * ya no aparecen en el inicio — RN-05).
     *
     * Las Rechazadas SÍ siguen aquí: el docente las puede corregir y reenviar,
     * igual que un reporte rechazado, así que para él no están terminadas. Si
     * se fueran al histórico tendría que ir a buscarlas allá para corregirlas.
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
     * Solicitudes activas para el coordinador de Estadías: las que ya fueron
     * enviadas y siguen en proceso. Las Pendientes no aparecen porque el
     * docente aún no las envía (RN-02).
     *
     * idPropias solo llega con valor cuando quien revisa también levanta
     * solicitudes (el Administrador): a las enviadas por los demás se le suman
     * las suyas, que siguen Pendientes y si no no le saldrían en ningún lado.
     * Con null se comporta igual que antes.
     */
    public List<Solicitud> getActivasParaRevision(Integer idPropias) {
        if (idPropias == null) {
            return getPorEstados(true, "En revisión", "Aprobada");
        }

        List<Solicitud> datos = new ArrayList<>();
        String sql = SELECT_BASE
                + " WHERE e.nombre_estado IN (?, ?)"
                // Las propias son las mismas que vería como docente: todas
                // menos las Completadas, que ya viven en el histórico
                + " OR (s.id_usuario_solicitante = ? AND e.nombre_estado <> ?)"
                + " ORDER BY s.fecha_creacion DESC";
        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "En revisión");
            ps.setString(2, "Aprobada");
            ps.setInt(3, idPropias);
            ps.setString(4, "Completada");
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

    /** Todas las solicitudes ya enviadas (para la página Solicitudes del coordinador). */
    public List<Solicitud> getEnviadas() {
        return getPorEstados(false, "Pendiente");
    }

    /**
     * Histórico: solicitudes terminadas. Si idUsuario es null se traen las de
     * todos los docentes (así lo pide Estadías desde HistorialServlet).
     *
     * Qué cuenta como "terminada" depende de quién pregunta: para Estadías una
     * Rechazada ya está cerrada, pero para el docente dueño no, porque la puede
     * corregir; a él le sigue apareciendo en su bandeja y por eso no se repite
     * aquí.
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
     * Solicitudes cuyo estado está dentro de la lista, o fuera de ella si
     * incluir es false. Los nombres de estado viajan como parámetros (?) en
     * vez de pegarse al texto de la consulta, que es como se cuela una
     * inyección de SQL. Lo único que se concatena son los signos de
     * interrogación, uno por cada estado recibido.
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

    /** Cambio simple de estado (ej. Pendiente → En revisión al enviar). */
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
     * Decisión de Estadías: aprueba o rechaza guardando quién autorizó y el
     * motivo (RF-05).
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
     * Actualiza la solicitud completa (datos + desglose por programa +
     * asignaturas). Los hijos se reemplazan en la misma transacción para que
     * queden igual que como se capturaron.
     *
     * Siempre deja el estado en Pendiente, igual que ReporteDao.guardarFormulario:
     * si venía Rechazada, corregirla la "reabre" y el docente tiene que volver a
     * firmar el FO y enviarla. Con ella se borra la decisión anterior, porque
     * quien la rechazó ya no autoriza nada y su nombre saldría en la firma
     * "Autoriza" del formato impreso.
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
     * Inserta las tablas hijas de la solicitud (desglose por programa,
     * asignaturas, estudiantes por división y docentes acompañantes) usando la
     * conexión de la transacción que ya viene abierta.
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
     * Fecha en yyyy-MM-dd, o NULL si el campo viene vacío. Los formularios
     * mandan "" cuando el docente no elige fecha y Date.valueOf("") revienta.
     */
    private void setFecha(PreparedStatement ps, int indice, String iso) throws SQLException {
        if (iso != null && !iso.isBlank()) {
            ps.setDate(indice, Date.valueOf(iso.trim()));
        } else {
            ps.setNull(indice, java.sql.Types.DATE);
        }
    }

    /** Borra las tablas hijas para volver a escribirlas (update) o eliminar la solicitud. */
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

    /** Devuelve siempre las 4 divisiones; las que no tienen fila quedan en 0. */
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
