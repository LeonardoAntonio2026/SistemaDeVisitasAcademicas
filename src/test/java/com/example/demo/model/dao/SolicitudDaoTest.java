package com.example.demo.model.dao;

import com.example.demo.model.Solicitud;
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


class SolicitudDaoTest {

private SolicitudDao solicitudDao;
private Connection conMock;
private PreparedStatement psMock;      // genérico: update/delete/select simples
private ResultSet rsMock;              // genérico
private MockedStatic<SQLConnector> sqlConnectorMock;

@BeforeEach
void setUp() throws SQLException {
solicitudDao = new SolicitudDao();
conMock = mock(Connection.class);
psMock = mock(PreparedStatement.class);
rsMock = mock(ResultSet.class);

sqlConnectorMock = mockStatic(SQLConnector.class);
sqlConnectorMock.when(SQLConnector::getConnection).thenReturn(conMock);

// Cableado genérico por defecto (los tests lo sobreescriben cuando lo necesitan)
when(conMock.prepareStatement(anyString())).thenReturn(psMock);
when(psMock.executeQuery()).thenReturn(rsMock);
}

@AfterEach
void tearDown() {
sqlConnectorMock.close();
}

private Solicitud solicitudDePrueba() {
Solicitud s = new Solicitud();
s.setIdUsuarioSolicitante(1);
s.setNombreEmpresaActividad("Empresa Demo");
s.setLugarDireccion("Cuernavaca, Morelos");
s.setTelefonoContacto("7771234567");
s.setCorreoContacto("demo@utez.edu.mx");
s.setFechaInicio("2026-09-01");
s.setObjetivo("Visita academica");
s.setAreaSolicitante("TI");
s.setDocenteResponsable("Ing. Prueba");
s.setCelularResponsable("7779876543");
// programas, asignaturas, docentesAcompanantes quedan vacíos (default)
// y estudiantesPorDivision queda en 0 en las 4 divisiones (default)
return s;
}

//  CREATE

@Test
@DisplayName("create() guarda la solicitud, hace commit y asigna el id generado")
void testCreate_Exitoso() throws SQLException {
// Arrange
PreparedStatement psInsertMock = mock(PreparedStatement.class);
ResultSet rsGenMock = mock(ResultSet.class);
when(conMock.prepareStatement(anyString(), any(String[].class))).thenReturn(psInsertMock);
when(rsGenMock.next()).thenReturn(true);
when(rsGenMock.getInt(1)).thenReturn(100);
when(psInsertMock.getGeneratedKeys()).thenReturn(rsGenMock);

Solicitud nueva = solicitudDePrueba();

// Act
boolean resultado = solicitudDao.create(nueva);

// Assert
assertTrue(resultado);
assertEquals(100, nueva.getIdSolicitud());
verify(conMock).commit();
verify(conMock, never()).rollback();
}

@Test
@DisplayName("create() hace rollback y devuelve false si Oracle no regresa un id generado")
void testCreate_SinIdGenerado() throws SQLException {
// Arrange
PreparedStatement psInsertMock = mock(PreparedStatement.class);
ResultSet rsGenMock = mock(ResultSet.class);
when(conMock.prepareStatement(anyString(), any(String[].class))).thenReturn(psInsertMock);
when(rsGenMock.next()).thenReturn(false); // no hay id generado
when(psInsertMock.getGeneratedKeys()).thenReturn(rsGenMock);

// Act
boolean resultado = solicitudDao.create(solicitudDePrueba());

// Assert
assertFalse(resultado);
verify(conMock).rollback();
verify(conMock, never()).commit();
}

@Test
@DisplayName("create() hace rollback y devuelve false si Oracle lanza una excepcion")
void testCreate_SQLException() throws SQLException {
// Arrange: forzamos que falle el INSERT principal
when(conMock.prepareStatement(anyString(), any(String[].class)))
.thenThrow(new SQLException("Error simulado de insercion"));

// Act
boolean resultado = solicitudDao.create(solicitudDePrueba());

// Assert
assertFalse(resultado);
verify(conMock).rollback();
}

//  READ (getAll / getById)

@Test
@DisplayName("getAll() mapea correctamente varias filas del ResultSet")
void testGetAll_ConDatos() throws SQLException {
// Arrange: 2 filas simuladas
when(rsMock.next()).thenReturn(true, true, false);
when(rsMock.getInt("id_solicitud")).thenReturn(1, 2);
when(rsMock.getString("nombre_empresa_actividad")).thenReturn("Empresa A", "Empresa B");
when(rsMock.getString("nombre_estado")).thenReturn("Pendiente", "Aprobada");

// Act
List<Solicitud> lista = solicitudDao.getAll();

// Assert
assertEquals(2, lista.size());
assertEquals(1, lista.get(0).getIdSolicitud());
assertEquals("Empresa A", lista.get(0).getNombreEmpresaActividad());
assertEquals("Aprobada", lista.get(1).getNombreEstado());
}

@Test
@DisplayName("getAll() devuelve lista vacia si no hay solicitudes")
void testGetAll_Vacio() throws SQLException {
when(rsMock.next()).thenReturn(false);

List<Solicitud> lista = solicitudDao.getAll();

assertNotNull(lista);
assertTrue(lista.isEmpty());
}

@Test
@DisplayName("getById() arma la solicitud completa junto con sus tablas hijas")
void testGetById_Existente() throws SQLException {
// Arrange: consulta principal
PreparedStatement psMainMock = mock(PreparedStatement.class);
ResultSet rsMainMock = mock(ResultSet.class);
when(conMock.prepareStatement(contains("FROM solicitud s"))).thenReturn(psMainMock);
when(psMainMock.executeQuery()).thenReturn(rsMainMock);
when(rsMainMock.next()).thenReturn(true);
when(rsMainMock.getInt("id_solicitud")).thenReturn(5);
when(rsMainMock.getString("nombre_empresa_actividad")).thenReturn("Empresa X");
when(rsMainMock.getString("nombre_estado")).thenReturn("En revision");

// Subconsultas: programas, asignaturas, division, docentes -> todas vacías
PreparedStatement psProgMock = mock(PreparedStatement.class);
ResultSet rsProgMock = mock(ResultSet.class);
when(conMock.prepareStatement(contains("division_academica, cuatrimestre"))).thenReturn(psProgMock);
when(psProgMock.executeQuery()).thenReturn(rsProgMock);
when(rsProgMock.next()).thenReturn(false);

PreparedStatement psAsigMock = mock(PreparedStatement.class);
ResultSet rsAsigMock = mock(ResultSet.class);
when(conMock.prepareStatement(contains("asignatura_reforzar_solicitud WHERE"))).thenReturn(psAsigMock);
when(psAsigMock.executeQuery()).thenReturn(rsAsigMock);
when(rsAsigMock.next()).thenReturn(false);

PreparedStatement psDivMock = mock(PreparedStatement.class);
ResultSet rsDivMock = mock(ResultSet.class);
when(conMock.prepareStatement(contains("estudiantes_division WHERE"))).thenReturn(psDivMock);
when(psDivMock.executeQuery()).thenReturn(rsDivMock);
when(rsDivMock.next()).thenReturn(false);

PreparedStatement psDocMock = mock(PreparedStatement.class);
ResultSet rsDocMock = mock(ResultSet.class);
when(conMock.prepareStatement(contains("solicitud_docente sd"))).thenReturn(psDocMock);
when(psDocMock.executeQuery()).thenReturn(rsDocMock);
when(rsDocMock.next()).thenReturn(false);

// Act
Solicitud resultado = solicitudDao.getById(5);

// Assert
assertNotNull(resultado);
assertEquals(5, resultado.getIdSolicitud());
assertEquals("Empresa X", resultado.getNombreEmpresaActividad());
assertTrue(resultado.getProgramas().isEmpty());
assertTrue(resultado.getAsignaturas().isEmpty());
}

@Test
@DisplayName("getById() devuelve null si el id no existe")
void testGetById_NoExiste() throws SQLException {
PreparedStatement psMainMock = mock(PreparedStatement.class);
ResultSet rsMainMock = mock(ResultSet.class);
when(conMock.prepareStatement(contains("FROM solicitud s"))).thenReturn(psMainMock);
when(psMainMock.executeQuery()).thenReturn(rsMainMock);
when(rsMainMock.next()).thenReturn(false);

Solicitud resultado = solicitudDao.getById(999);

assertNull(resultado);
}

// UPDATE

@Test
@DisplayName("update() modifica la solicitud y hace commit cuando el id existe")
void testUpdate_Exitoso() throws SQLException {
when(psMock.executeUpdate()).thenReturn(1); // 1 fila afectada

Solicitud s = solicitudDePrueba();
s.setIdSolicitud(10);

boolean resultado = solicitudDao.update(s);

assertTrue(resultado);
verify(conMock).commit();
verify(conMock, never()).rollback();
}

@Test
@DisplayName("update() hace rollback y devuelve false si el id no existe (0 filas afectadas)")
void testUpdate_NoExiste() throws SQLException {
when(psMock.executeUpdate()).thenReturn(0);

Solicitud s = solicitudDePrueba();
s.setIdSolicitud(999);

boolean resultado = solicitudDao.update(s);

assertFalse(resultado);
verify(conMock).rollback();
}

@Test
@DisplayName("update() hace rollback y devuelve false si Oracle lanza una excepcion")
void testUpdate_SQLException() throws SQLException {
when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

Solicitud s = solicitudDePrueba();
s.setIdSolicitud(10);

boolean resultado = solicitudDao.update(s);

assertFalse(resultado);
verify(conMock).rollback();
}

//  DELETE

@Test
@DisplayName("delete() elimina la solicitud y hace commit cuando el id existe")
void testDelete_Exitoso() throws SQLException {
when(psMock.executeUpdate()).thenReturn(1);

boolean resultado = solicitudDao.delete(10);

assertTrue(resultado);
verify(conMock).commit();
verify(conMock, never()).rollback();
}

@Test
@DisplayName("delete() devuelve false si el id no existe (0 filas afectadas)")
void testDelete_NoExiste() throws SQLException {
when(psMock.executeUpdate()).thenReturn(0);

boolean resultado = solicitudDao.delete(999);

assertFalse(resultado);
verify(conMock).commit(); // el DAO igual hace commit aunque filas=0, no lanza excepción
}

@Test
@DisplayName("delete() hace rollback y devuelve false si Oracle lanza una excepcion")
void testDelete_SQLException() throws SQLException {
when(conMock.prepareStatement(anyString())).thenThrow(new SQLException("Error simulado"));

boolean resultado = solicitudDao.delete(10);

assertFalse(resultado);
verify(conMock).rollback();
}
}