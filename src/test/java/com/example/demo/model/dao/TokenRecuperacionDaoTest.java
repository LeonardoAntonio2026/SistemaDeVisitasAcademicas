package com.example.demo.model.dao;

import com.example.demo.model.TokenRecuperacion;
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
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TokenRecuperacionDaoTest {

    private TokenRecuperacionDao dao;
    private Connection conMock;
    private PreparedStatement psMock;
    private ResultSet rsMock;
    private MockedStatic<SQLConnector> sqlConnectorMock;

    @BeforeEach
    void setUp() throws SQLException {
        dao = new TokenRecuperacionDao();
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

    // ==================== crear() ====================

    @Test
    @DisplayName("crear() invalida el token anterior, inserta el nuevo y hace commit")
    void testCrear_Exitoso() throws SQLException {
        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = dao.crear(1, "abc123token");

        assertTrue(resultado);
        verify(conMock).commit();
        verify(conMock, never()).rollback();
    }

    @Test
    @DisplayName("crear() hace rollback y devuelve false si Oracle lanza una excepcion")
    void testCrear_SQLException() throws SQLException {
        when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

        boolean resultado = dao.crear(1, "abc123token");

        assertFalse(resultado);
        verify(conMock).rollback();
    }

    // ==================== buscarPorToken() ====================

    @Test
    @DisplayName("buscarPorToken() mapea correctamente el token encontrado")
    void testBuscarPorToken_Encontrado() throws SQLException {
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt("id_token")).thenReturn(1);
        when(rsMock.getInt("id_usuario")).thenReturn(1);
        when(rsMock.getString("token")).thenReturn("abc123token");
        when(rsMock.getTimestamp("fecha_expiracion")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(rsMock.getString("usado")).thenReturn("N");

        TokenRecuperacion resultado = dao.buscarPorToken("abc123token");

        assertNotNull(resultado);
        assertEquals("abc123token", resultado.getToken());
        assertFalse(resultado.isUsado());
    }

    @Test
    @DisplayName("buscarPorToken() devuelve null si el token no existe")
    void testBuscarPorToken_NoExiste() throws SQLException {
        when(rsMock.next()).thenReturn(false);

        TokenRecuperacion resultado = dao.buscarPorToken("noexiste");

        assertNull(resultado);
    }

    // ==================== marcarUsado() ====================

    @Test
    @DisplayName("marcarUsado() devuelve true cuando actualiza una fila")
    void testMarcarUsado_Exitoso() throws SQLException {
        when(psMock.executeUpdate()).thenReturn(1);

        boolean resultado = dao.marcarUsado("abc123token");

        assertTrue(resultado);
    }

    @Test
    @DisplayName("marcarUsado() devuelve false si el token no existe (0 filas afectadas)")
    void testMarcarUsado_NoExiste() throws SQLException {
        when(psMock.executeUpdate()).thenReturn(0);

        boolean resultado = dao.marcarUsado("noexiste");

        assertFalse(resultado);
    }

    @Test
    @DisplayName("marcarUsado() devuelve false si Oracle lanza una excepcion")
    void testMarcarUsado_SQLException() throws SQLException {
        when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

        boolean resultado = dao.marcarUsado("abc123token");

        assertFalse(resultado);
    }
}