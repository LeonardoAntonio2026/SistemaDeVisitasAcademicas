package com.example.demo.model.dao;

import com.example.demo.model.ImagenReporte;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class ImagenReporteDaoTest {

    private ImagenReporteDao imagenReporteDao;
    private Connection conMock;
    private PreparedStatement psMock;
    private ResultSet rsMock;
    private MockedStatic<SQLConnector> sqlConnectorMock;

    @BeforeEach
    void setUp() throws SQLException {
        imagenReporteDao = new ImagenReporteDao();
        conMock = mock(Connection.class);
        psMock = mock(PreparedStatement.class);
        rsMock = mock(ResultSet.class);

        sqlConnectorMock = mockStatic(SQLConnector.class);
        sqlConnectorMock.when(SQLConnector::getConnection).thenReturn(conMock);

        when(conMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeQuery()).thenReturn(rsMock);
    }

    @AfterEach
    void tearDown() {
        sqlConnectorMock.close();
    }

    // GET BY REPORTE

    @Test
    @DisplayName("getByReporte() mapea correctamente varias filas sin traer el contenido base64")
    void testGetByReporte_ConDatos() throws SQLException {
        when(rsMock.next()).thenReturn(true, true, false);
        when(rsMock.getInt("id_imagen")).thenReturn(1, 2);
        when(rsMock.getInt("id_reporte")).thenReturn(10, 10);
        when(rsMock.getString("fecha_carga")).thenReturn("2026-08-16 10:00", "2026-08-16 10:05");
        when(rsMock.getLong("tam_base64")).thenReturn(400L, 800L);

        List<ImagenReporte> resultado = imagenReporteDao.getByReporte(10);

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getIdImagen());
        assertEquals(10, resultado.get(0).getIdReporte());
        assertEquals(300, resultado.get(0).getTamanoBytes()); // 400 * 3 / 4
        assertNull(resultado.get(0).getContenidoBase64());
    }

    @Test
    @DisplayName("getByReporte() devuelve lista vacia si el reporte no tiene imagenes")
    void testGetByReporte_Vacio() throws SQLException {
        when(rsMock.next()).thenReturn(false);

        List<ImagenReporte> resultado = imagenReporteDao.getByReporte(999);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("getByReporte() devuelve lista vacia si Oracle lanza una excepcion")
    void testGetByReporte_SQLException() throws SQLException {
        when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

        List<ImagenReporte> resultado = imagenReporteDao.getByReporte(10);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // GET BY ID

    @Test
    @DisplayName("getById() trae la imagen completa incluyendo el contenido base64")
    void testGetById_Existente() throws SQLException {
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt("id_imagen")).thenReturn(5);
        when(rsMock.getInt("id_reporte")).thenReturn(20);
        when(rsMock.getString("fecha_carga")).thenReturn("2026-08-16 11:00");
        when(rsMock.getLong("tam_base64")).thenReturn(100L);
        when(rsMock.getString("contenido_base64")).thenReturn("BASE64FALSO==");

        ImagenReporte resultado = imagenReporteDao.getById(5);

        assertNotNull(resultado);
        assertEquals(5, resultado.getIdImagen());
        assertEquals(20, resultado.getIdReporte());
        assertEquals("BASE64FALSO==", resultado.getContenidoBase64());
        assertEquals(75, resultado.getTamanoBytes()); // 100 * 3 / 4
    }

    @Test
    @DisplayName("getById() devuelve null si la imagen no existe")
    void testGetById_NoExiste() throws SQLException {
        when(rsMock.next()).thenReturn(false);

        ImagenReporte resultado = imagenReporteDao.getById(999);

        assertNull(resultado);
    }

    @Test
    @DisplayName("getById() devuelve null si Oracle lanza una excepcion")
    void testGetById_SQLException() throws SQLException {
        when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

        ImagenReporte resultado = imagenReporteDao.getById(5);

        assertNull(resultado);
    }

    // CONTAR POR REPORTE

    @Test
    @DisplayName("contarPorReporte() devuelve el conteo correcto para validar el limite de 3 (RN-07)")
    void testContarPorReporte_ConDatos() throws SQLException {
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(3);

        int resultado = imagenReporteDao.contarPorReporte(10);

        assertEquals(3, resultado);
    }

    @Test
    @DisplayName("contarPorReporte() devuelve 0 si el reporte no tiene imagenes")
    void testContarPorReporte_SinImagenes() throws SQLException {
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(0);

        int resultado = imagenReporteDao.contarPorReporte(10);

        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("contarPorReporte() devuelve 0 si Oracle lanza una excepcion")
    void testContarPorReporte_SQLException() throws SQLException {
        when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

        int resultado = imagenReporteDao.contarPorReporte(10);

        assertEquals(0, resultado);
    }

    // GUARDAR

    @Test
    @DisplayName("guardar() devuelve true cuando la insercion afecta una fila")
    void testGuardar_Exitoso() throws SQLException {
        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = imagenReporteDao.guardar(10, "BASE64DEPRUEBA==");

        assertTrue(resultado);
        verify(psMock).setInt(1, 10);
        verify(psMock).setString(2, "BASE64DEPRUEBA==");
    }

    @Test
    @DisplayName("guardar() devuelve false cuando no se afecta ninguna fila")
    void testGuardar_NoAfecta() throws SQLException {
        when(psMock.executeUpdate()).thenReturn(0);

        boolean resultado = imagenReporteDao.guardar(10, "BASE64DEPRUEBA==");

        assertFalse(resultado);
    }

    @Test
    @DisplayName("guardar() devuelve false si Oracle lanza una excepcion")
    void testGuardar_SQLException() throws SQLException {
        when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

        boolean resultado = imagenReporteDao.guardar(10, "BASE64DEPRUEBA==");

        assertFalse(resultado);
    }

    // ELIMINAR

    @Test
    @DisplayName("eliminar() devuelve true cuando la imagen pertenece al reporte indicado")
    void testEliminar_Exitoso() throws SQLException {
        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = imagenReporteDao.eliminar(5, 10);

        assertTrue(resultado);
        verify(psMock).setInt(1, 5);
        verify(psMock).setInt(2, 10);
    }

    @Test
    @DisplayName("eliminar() devuelve false si la imagen no pertenece al reporte indicado")
    void testEliminar_NoPertenece() throws SQLException {
        when(psMock.executeUpdate()).thenReturn(0);

        boolean resultado = imagenReporteDao.eliminar(5, 999);

        assertFalse(resultado);
    }

    @Test
    @DisplayName("eliminar() devuelve false si Oracle lanza una excepcion")
    void testEliminar_SQLException() throws SQLException {
        when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

        boolean resultado = imagenReporteDao.eliminar(5, 10);

        assertFalse(resultado);
    }
}