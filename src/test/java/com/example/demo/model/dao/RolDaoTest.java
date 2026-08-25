package com.example.demo.model.dao;

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
import static org.mockito.Mockito.*;

class RolDaoTest {

private RolDao rolDao;
private Connection conMock;
private PreparedStatement psMock;
private ResultSet rsMock;
private MockedStatic<SQLConnector> sqlConnectorMock;

@BeforeEach
void setUp() throws SQLException {
rolDao = new RolDao();
conMock = mock(Connection.class);
psMock = mock(PreparedStatement.class);
rsMock = mock(ResultSet.class);

// Interceptamos la clase estática SQLConnector
sqlConnectorMock = mockStatic(SQLConnector.class);
sqlConnectorMock.when(SQLConnector::getConnection).thenReturn(conMock);

// Cableado genérico: cualquier prepareStatement/executeQuery usa nuestros mocks
when(conMock.prepareStatement(anyString())).thenReturn(psMock);
when(psMock.executeQuery()).thenReturn(rsMock);
}

@AfterEach
void tearDown() {
// Muy importante: cerrar el mock estático después de cada test,
// si no, se queda "pegado" para los siguientes tests de otras clases.
sqlConnectorMock.close();
}

//  getNombres()

@Test
@DisplayName("getNombres devuelve la lista de roles cuando hay filas en la BD")
void testGetNombres_ConRoles() throws SQLException {
// Arrange: simulamos 3 filas en el ResultSet
when(rsMock.next()).thenReturn(true, true, true, false);
when(rsMock.getString("nombre_rol")).thenReturn("Administrador", "Docente", "Estadias");

// Act
List<String> nombres = rolDao.getNombres();

// Assert
assertEquals(3, nombres.size());
assertEquals("Administrador", nombres.get(0));
assertEquals("Docente", nombres.get(1));
assertEquals("Estadias", nombres.get(2));
}

@Test
@DisplayName("getNombres devuelve lista vacía si la tabla ROL no tiene registros")
void testGetNombres_SinRoles() throws SQLException {
// Arrange: el ResultSet no trae ninguna fila
when(rsMock.next()).thenReturn(false);

// Act
List<String> nombres = rolDao.getNombres();

// Assert
assertTrue(nombres.isEmpty());
}

@Test
@DisplayName("getNombres no revienta y devuelve lista vacía si hay un error de BD")
void testGetNombres_ErrorDeBaseDeDatos() throws SQLException {
// Arrange: forzamos que prepareStatement lance una excepción
when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Fallo de conexion simulado"));

// Act
List<String> nombres = rolDao.getNombres();

// Assert: el DAO atrapa la excepción (e.printStackTrace) y regresa lista vacía, no null
assertNotNull(nombres);
assertTrue(nombres.isEmpty());
}

// getIdPorNombre()

@Test
@DisplayName("getIdPorNombre devuelve el id correcto cuando el rol existe")
void testGetIdPorNombre_RolExistente() throws SQLException {
// Arrange
when(rsMock.next()).thenReturn(true);
when(rsMock.getInt(1)).thenReturn(2);

// Act
int id = rolDao.getIdPorNombre("Docente");

// Assert
assertEquals(2, id);
// Confirmamos que sí se le pasó el nombre "limpio" al PreparedStatement
verify(psMock).setString(1, "Docente");
}

@Test
@DisplayName("getIdPorNombre devuelve 0 cuando el rol no existe en el catálogo")
void testGetIdPorNombre_RolNoExiste() throws SQLException {
// Arrange: no hay filas que coincidan
when(rsMock.next()).thenReturn(false);

// Act
int id = rolDao.getIdPorNombre("RolInventado");

// Assert
assertEquals(0, id);
}

@Test
@DisplayName("getIdPorNombre devuelve 0 sin tocar la BD si el nombre es null")
void testGetIdPorNombre_NombreNull() throws SQLException {
// Act
int id = rolDao.getIdPorNombre(null);

// Assert
assertEquals(0, id);
// Verificamos que ni siquiera se intentó abrir conexión (validación temprana)
sqlConnectorMock.verify(SQLConnector::getConnection, never());
}

@Test
@DisplayName("getIdPorNombre devuelve 0 sin tocar la BD si el nombre viene en blanco")
void testGetIdPorNombre_NombreEnBlanco() throws SQLException {
// Act
int id = rolDao.getIdPorNombre("   ");

// Assert
assertEquals(0, id);
sqlConnectorMock.verify(SQLConnector::getConnection, never());
}

@Test
@DisplayName("getIdPorNombre recorta espacios antes de buscar en la BD")
void testGetIdPorNombre_RecortaEspacios() throws SQLException {
// Arrange
when(rsMock.next()).thenReturn(true);
when(rsMock.getInt(1)).thenReturn(1);

// Act
rolDao.getIdPorNombre("  Administrador  ");

// Assert: el DAO hace .trim() antes de mandarlo a la consulta
verify(psMock).setString(1, "Administrador");
}
}