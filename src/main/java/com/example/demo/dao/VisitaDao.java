package com.example.demo.dao; // 👈 Con el .demo correcto

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.example.demo.model.Visita;
import com.example.demo.util.SQLConnector;

public class VisitaDao {

    public boolean insertar(Visita visita) {
        String sql = "INSERT INTO visitas (nombre_empresa, direccion, telefono, correo, fecha_inicio, "
                + "fecha_termino, hora_visita, objetivo, area_solicitante, docente_responsable, "
                + "celular_docente, docentes_acompanantes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, visita.getNombreEmpresa());
            ps.setString(2, visita.getDireccion());
            ps.setString(3, visita.getTelefono());
            ps.setString(4, visita.getCorreo());
            ps.setString(5, visita.getFechaInicio());
            ps.setString(6, visita.getFechaTermino());
            ps.setString(7, visita.getHoraVisita());
            ps.setString(8, visita.getObjetivo());
            ps.setString(9, visita.getAreaSolicitante());
            ps.setString(10, visita.getDocenteResponsable());
            ps.setString(11, visita.getCelularDocente());
            ps.setString(12, visita.getDocentesAcompanantes());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}