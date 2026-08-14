package com.example.demo.model.dao;

import com.example.demo.model.Reporte;
import com.example.demo.utils.SQLConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReporteDaoTest {

    private ReporteDao dao;
    private Connection conMock;
    private PreparedStatement psMock;
    private ResultSet rsMock;
    private MockedStatic<SQLConnector> sqlConnectorMock;

    @BeforeEach
    void setUp() throws SQLException {

        dao = new ReporteDao();

        conMock = mock(Connection.class);
        psMock = mock(PreparedStatement.class);
        rsMock = mock(ResultSet.class);

        sqlConnectorMock = mockStatic(SQLConnector.class);

        sqlConnectorMock.when(SQLConnector::getConnection)
                .thenReturn(conMock);

        when(conMock.prepareStatement(anyString()))
                .thenReturn(psMock);

        when(psMock.executeQuery())
                .thenReturn(rsMock);
    }

    @AfterEach
    void tearDown() {
        sqlConnectorMock.close();
    }



    @Test
    @DisplayName("crearPendiente() crea correctamente el reporte")
    void crearPendiente() throws SQLException {

        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = dao.crearPendiente(10);

        assertTrue(resultado);

        verify(psMock).setInt(1, 10);
        verify(psMock).setInt(2, 10);
        verify(psMock).executeUpdate();
    }



    @Test
    @DisplayName("existePorSolicitud() devuelve true si existe")
    void existePorSolicitud() throws SQLException {

        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(1);

        boolean resultado = dao.existePorSolicitud(10);

        assertTrue(resultado);

        verify(psMock).setInt(1, 10);
        verify(psMock).executeQuery();
    }



    @Test
    @DisplayName("getAll() devuelve los reportes")
    void getAll() throws SQLException {

        when(rsMock.next())
                .thenReturn(true)
                .thenReturn(false);

        when(rsMock.getInt("id_reporte")).thenReturn(1);
        when(rsMock.getInt("id_solicitud")).thenReturn(10);
        when(rsMock.getString("fecha")).thenReturn("2026-08-14");
        when(rsMock.getString("resultados")).thenReturn("Visita realizada");
        when(rsMock.getString("observaciones")).thenReturn("Todo correcto");
        when(rsMock.getString("fecha_creacion")).thenReturn("2026-08-14");
        when(rsMock.getInt("id_estado")).thenReturn(1);
        when(rsMock.getString("motivo")).thenReturn(null);
        when(rsMock.getString("nombre_estado")).thenReturn("Pendiente");
        when(rsMock.getString("nombre_empresa_actividad"))
                .thenReturn("Empresa ABC");
        when(rsMock.getString("lugar_direccion"))
                .thenReturn("Cuernavaca");
        when(rsMock.getInt("id_usuario_solicitante")).thenReturn(5);
        when(rsMock.getString("fecha_solicitud"))
                .thenReturn("2026-08-10");
        when(rsMock.getString("nombre_solicitante"))
                .thenReturn("Juan");
        when(rsMock.getString("correo_solicitante"))
                .thenReturn("juan@email.com");
        when(rsMock.getInt("total_estudiantes")).thenReturn(20);

        var resultado = dao.getAll();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdReporte());
        assertEquals("Pendiente",
                resultado.get(0).getNombreEstado());
    }



    @Test
    @DisplayName("getBySolicitante() devuelve los reportes del usuario")
    void getBySolicitante() throws SQLException {

        when(rsMock.next())
                .thenReturn(true)
                .thenReturn(false);

        when(rsMock.getInt("id_reporte")).thenReturn(2);
        when(rsMock.getInt("id_solicitud")).thenReturn(15);
        when(rsMock.getString("nombre_estado"))
                .thenReturn("Completado");
        when(rsMock.getString("nombre_empresa_actividad"))
                .thenReturn("Empresa XYZ");
        when(rsMock.getString("nombre_solicitante"))
                .thenReturn("Juan");
        when(rsMock.getString("correo_solicitante"))
                .thenReturn("juan@email.com");
        when(rsMock.getInt("total_estudiantes")).thenReturn(10);

        var resultado = dao.getBySolicitante(5);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(2, resultado.get(0).getIdReporte());
        assertEquals("Completado",
                resultado.get(0).getNombreEstado());

        verify(psMock).setInt(1, 5);
    }



    @Test
    @DisplayName("getById() devuelve el reporte encontrado")
    void getById() throws SQLException {

        when(rsMock.next()).thenReturn(true);

        when(rsMock.getInt("id_reporte")).thenReturn(3);
        when(rsMock.getInt("id_solicitud")).thenReturn(20);
        when(rsMock.getString("fecha")).thenReturn("2026-08-14");
        when(rsMock.getString("resultados"))
                .thenReturn("Resultados");
        when(rsMock.getString("observaciones"))
                .thenReturn("Observaciones");
        when(rsMock.getString("fecha_creacion"))
                .thenReturn("2026-08-14");
        when(rsMock.getInt("id_estado")).thenReturn(1);
        when(rsMock.getString("motivo")).thenReturn(null);
        when(rsMock.getString("nombre_estado"))
                .thenReturn("Pendiente");
        when(rsMock.getString("nombre_empresa_actividad"))
                .thenReturn("Empresa ABC");
        when(rsMock.getString("lugar_direccion"))
                .thenReturn("Cuernavaca");
        when(rsMock.getInt("id_usuario_solicitante"))
                .thenReturn(5);
        when(rsMock.getString("fecha_solicitud"))
                .thenReturn("2026-08-10");
        when(rsMock.getString("nombre_solicitante"))
                .thenReturn("Juan");
        when(rsMock.getString("correo_solicitante"))
                .thenReturn("juan@email.com");
        when(rsMock.getInt("total_estudiantes"))
                .thenReturn(20);

        Reporte resultado = dao.getById(3);

        assertNotNull(resultado);
        assertEquals(3, resultado.getIdReporte());
        assertEquals(20, resultado.getIdSolicitud());
        assertEquals("Pendiente",
                resultado.getNombreEstado());
        assertEquals("Juan",
                resultado.getNombreSolicitante());
    }



    @Test
    @DisplayName("getBySolicitud() devuelve el reporte")
    void getBySolicitud() throws SQLException {

        when(rsMock.next()).thenReturn(true);

        when(rsMock.getInt("id_reporte")).thenReturn(4);
        when(rsMock.getInt("id_solicitud")).thenReturn(30);
        when(rsMock.getString("nombre_estado"))
                .thenReturn("Completado");
        when(rsMock.getString("nombre_solicitante"))
                .thenReturn("Juan");
        when(rsMock.getInt("total_estudiantes"))
                .thenReturn(15);

        Reporte resultado = dao.getBySolicitud(30);

        assertNotNull(resultado);
        assertEquals(4, resultado.getIdReporte());
        assertEquals(30, resultado.getIdSolicitud());
        assertEquals("Completado",
                resultado.getNombreEstado());

        verify(psMock).setInt(1, 30);
    }



    @Test
    @DisplayName("guardarFormulario() guarda correctamente")
    void guardarFormulario() throws SQLException {

        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = dao.guardarFormulario(
                10,
                "Buenos resultados",
                "Sin observaciones"
        );

        assertTrue(resultado);

        verify(psMock).setString(1, "Buenos resultados");
        verify(psMock).setString(2, "Sin observaciones");
        verify(psMock).setInt(3, 10);
        verify(psMock).executeUpdate();
    }



    @Test
    @DisplayName("enviar() cambia el reporte a enviado")
    void enviar() throws SQLException {

        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = dao.enviar(10);

        assertTrue(resultado);

        verify(psMock).setInt(1, 10);
        verify(psMock).executeUpdate();
    }



    @Test
    @DisplayName("decidir() actualiza el estado del reporte")
    void decidir() throws SQLException {

        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = dao.decidir(
                10,
                "Aprobado",
                "Reporte correcto"
        );

        assertTrue(resultado);

        verify(psMock).setString(1, "Aprobado");
        verify(psMock).setString(2, "Reporte correcto");
        verify(psMock).setInt(3, 10);
        verify(psMock).executeUpdate();
    }
}