package com.example.demo.model.dao;

import com.example.demo.model.Documento;
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

class DocumentoDaoTest {

    private DocumentoDao dao;
    private Connection conMock;
    private PreparedStatement psMock;
    private ResultSet rsMock;
    private MockedStatic<SQLConnector> sqlConnectorMock;

    @BeforeEach
    void setUp() throws SQLException {

        dao = new DocumentoDao();

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
    @DisplayName("getBySolicitud() devuelve documentos")
    void getBySolicitud() throws SQLException {

        when(rsMock.next())
                .thenReturn(true)
                .thenReturn(false);

        when(rsMock.getInt("id_documento"))
                .thenReturn(1);

        when(rsMock.getInt("id_solicitud"))
                .thenReturn(10);

        when(rsMock.wasNull())
                .thenReturn(false);

        when(rsMock.getInt("id_reporte"))
                .thenReturn(20);

        when(rsMock.getInt("id_tipo_documento"))
                .thenReturn(2);

        when(rsMock.getString("nombre_tipo"))
                .thenReturn("PDF");

        when(rsMock.getString("fecha_carga"))
                .thenReturn("2026-08-14");

        when(rsMock.getLong("tam_base64"))
                .thenReturn(1000L);

        var resultado = dao.getBySolicitud(10);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdDocumento());
        assertEquals("PDF", resultado.get(0).getNombreTipo());
    }


    @Test
    @DisplayName("getById() devuelve el documento encontrado")
    void getById() throws SQLException {

        when(rsMock.next()).thenReturn(true);

        when(rsMock.getInt("id_documento"))
                .thenReturn(1);

        when(rsMock.getInt("id_solicitud"))
                .thenReturn(10);

        when(rsMock.wasNull())
                .thenReturn(false);

        when(rsMock.getInt("id_reporte"))
                .thenReturn(20);

        when(rsMock.getInt("id_tipo_documento"))
                .thenReturn(2);

        when(rsMock.getString("nombre_tipo"))
                .thenReturn("PDF");

        when(rsMock.getString("fecha_carga"))
                .thenReturn("2026-08-14");

        when(rsMock.getLong("tam_base64"))
                .thenReturn(1000L);

        when(rsMock.getString("contenido_base64"))
                .thenReturn("ABC123");

        Documento resultado = dao.getById(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdDocumento());
        assertEquals(10, resultado.getIdSolicitud());
        assertEquals("PDF", resultado.getNombreTipo());
        assertEquals("ABC123", resultado.getContenidoBase64());
    }


    @Test
    @DisplayName("getByReporte() devuelve documentos")
    void getByReporte() throws SQLException {

        when(rsMock.next())
                .thenReturn(true)
                .thenReturn(false);

        when(rsMock.getInt("id_documento"))
                .thenReturn(5);

        when(rsMock.getInt("id_solicitud"))
                .thenReturn(10);

        when(rsMock.wasNull())
                .thenReturn(false);

        when(rsMock.getInt("id_reporte"))
                .thenReturn(20);

        when(rsMock.getInt("id_tipo_documento"))
                .thenReturn(3);

        when(rsMock.getString("nombre_tipo"))
                .thenReturn("Reporte firmado");

        when(rsMock.getString("fecha_carga"))
                .thenReturn("2026-08-14");

        when(rsMock.getLong("tam_base64"))
                .thenReturn(2000L);

        var resultado = dao.getByReporte(20);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(5, resultado.get(0).getIdDocumento());
        assertEquals("Reporte firmado",
                resultado.get(0).getNombreTipo());
    }


    @Test
    @DisplayName("guardarParaSolicitud() guarda correctamente")
    void guardarParaSolicitud() throws SQLException {

        when(psMock.executeUpdate())
                .thenReturn(1);

        boolean resultado = dao.guardarParaSolicitud(
                10,
                "PDF",
                "ABC123"
        );

        assertTrue(resultado);

        verify(conMock).setAutoCommit(false);
        verify(conMock).commit();
        verify(conMock, never()).rollback();
    }

    @Test
    @DisplayName("guardarParaReporte() guarda correctamente")
    void guardarParaReporte() throws SQLException {

        when(psMock.executeUpdate())
                .thenReturn(1);

        boolean resultado = dao.guardarParaReporte(
                20,
                "PDF firmado",
                "ABC123"
        );

        assertTrue(resultado);

        verify(conMock).setAutoCommit(false);
        verify(conMock).commit();
        verify(conMock, never()).rollback();
    }



    @Test
    @DisplayName("eliminarTipoDeReporte() elimina correctamente")
    void eliminarTipoDeReporte() throws SQLException {

        when(psMock.executeUpdate())
                .thenReturn(1);

        boolean resultado = dao.eliminarTipoDeReporte(
                20,
                "PDF firmado"
        );

        assertTrue(resultado);

        verify(psMock).executeUpdate();
    }


    @Test
    @DisplayName("existeTipoEnReporte() devuelve true cuando existe")
    void existeTipoEnReporte() throws SQLException {

        when(rsMock.next())
                .thenReturn(true);

        when(rsMock.getInt(1))
                .thenReturn(1);

        boolean resultado = dao.existeTipoEnReporte(
                20,
                "PDF firmado"
        );

        assertTrue(resultado);

        verify(psMock).executeQuery();
    }


    @Test
    @DisplayName("eliminarTipoDeSolicitud() elimina correctamente")
    void eliminarTipoDeSolicitud() throws SQLException {

        when(psMock.executeUpdate())
                .thenReturn(1);

        boolean resultado = dao.eliminarTipoDeSolicitud(
                10,
                "PDF"
        );

        assertTrue(resultado);

        verify(psMock).executeUpdate();
    }


    @Test
    @DisplayName("existeTipoEnSolicitud() devuelve true cuando existe")
    void existeTipoEnSolicitud() throws SQLException {

        when(rsMock.next())
                .thenReturn(true);

        when(rsMock.getInt(1))
                .thenReturn(1);

        boolean resultado = dao.existeTipoEnSolicitud(
                10,
                "PDF"
        );

        assertTrue(resultado);

        verify(psMock).executeQuery();
    }
}