package com.example.demo.model.dao;

package com.example.demo.model.dao;

import com.example.demo.model.Usuario;
import com.example.demo.utils.SQLConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;package com.example.demo.model.dao;

import com.example.demo.model.Usuario;
import com.example.demo.utils.SQLConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioDaoTest {
    private UsuarioDao usuarioDao;

    // Objetos simulados (Mocks) de JDBC
    private Connection conMock;
    private PreparedStatement psMock;
    private ResultSet rsMock;

    // Objeto para interceptar el método estático SQLConnector.getConnection()
    private MockedStatic<SQLConnector> sqlConnectorMockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        usuarioDao = new UsuarioDao();

        // 1. Inicializar los mocks
        conMock = mock(Connection.class);
        psMock = mock(PreparedStatement.class);
        rsMock = mock(ResultSet.class);

        // 2. Interceptar el llamado estático a la base de datos
        sqlConnectorMockedStatic = mockStatic(SQLConnector.class);
        sqlConnectorMockedStatic.when(SQLConnector::getConnection).thenReturn(conMock);
    }

    @AfterEach
    void tearDown() {
        // MUY IMPORTANTE: Cerrar el mock estático después de cada prueba para evitar conflictos
        sqlConnectorMockedStatic.close();
    }

    @Test
    void create() throws Exception {
        // Arrange
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setIdRol(1);
        nuevoUsuario.setNombre("Alan Mock");
        nuevoUsuario.setCorreo("alan.mock@example.com");
        nuevoUsuario.setContrasena("Password123!");

        // Simulamos que cualquier prepareStatement devuelva nuestro mock
        when(conMock.prepareStatement(anyString(), any(String[].class))).thenReturn(psMock);
        when(conMock.prepareStatement(anyString())).thenReturn(psMock);

        // Simulamos que el insert funciona
        when(psMock.executeUpdate()).thenReturn(1);

        // Simulamos la generación del ID (id_usuario = 100)
        when(psMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(100);

        // Act
        boolean creado = usuarioDao.create(nuevoUsuario);

        // Assert
        assertTrue(creado, "El usuario debería crearse simulando la BD");
        assertEquals(100, nuevoUsuario.getId(), "Se debió asignar el ID generado por el ResultSet simulado");
        verify(conMock).commit(); // Verificamos que se ejecutó el COMMIT de la transacción
    }

    // ==========================================
    // READ (getAll / getById)
    // ==========================================

    @Test
    void getAll() throws Exception {
        // Arrange
        when(conMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeQuery()).thenReturn(rsMock);

        // Simulamos que el ResultSet tiene 1 fila y luego termina
        when(rsMock.next()).thenReturn(true, false);
        when(rsMock.getInt("id_usuario")).thenReturn(1);
        when(rsMock.getInt("id_rol")).thenReturn(2);
        when(rsMock.getString("nombre")).thenReturn("Docente Simulado");
        when(rsMock.getString("correo")).thenReturn("docente@mock.com");
        when(rsMock.getString("nombre_rol")).thenReturn("Docente");

        // Act
        List<Usuario> lista = usuarioDao.getAll();

        // Assert
        assertNotNull(lista);
        assertEquals(1, lista.size(), "Debería retornar un usuario en la lista");
        assertEquals("Docente Simulado", lista.get(0).getNombre());
    }

    @Test
    void getById() throws Exception {
        // Arrange
        when(conMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true); // Simulamos que encuentra el registro

        when(rsMock.getInt("id_usuario")).thenReturn(5);
        when(rsMock.getInt("id_rol")).thenReturn(1);
        when(rsMock.getString("nombre")).thenReturn("Admin Simulado");
        when(rsMock.getString("correo")).thenReturn("admin@mock.com");
        when(rsMock.getString("nombre_rol")).thenReturn("Administrador");

        // Act
        Usuario usuario = usuarioDao.getById(5);

        // Assert
        assertNotNull(usuario);
        assertEquals(5, usuario.getId());
        assertEquals("Admin Simulado", usuario.getNombre());
        verify(psMock).setInt(1, 5); // Verificamos que le pasó el ID 5 al PreparedStatement
    }


    // UPDATE

    @Test
    void update() throws Exception {
        // Arrange
        Usuario usuario = new Usuario(1, 2, "Juan Modificado", "juan@mock.com");
        when(conMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeUpdate()).thenReturn(1); // Simulamos 1 fila actualizada

        // Act
        boolean actualizado = usuarioDao.update(usuario);

        // Assert
        assertTrue(actualizado);
        verify(psMock).setInt(1, 2); // Verificamos que setea id_rol = 2
        verify(psMock).setString(2, "Juan Modificado"); // Verificamos que setea nombre
    }

    // DELETE Y METODOS EXTRA


    @Test
    void delete() throws Exception {
        // Arrange
        when(conMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeUpdate()).thenReturn(1); // Simulamos que todos los deletes de cascada afectan al menos 1 fila

        // Act
        boolean eliminado = usuarioDao.delete(1);

        // Assert
        assertTrue(eliminado);
        verify(conMock).commit(); // Verificamos que la eliminación completó la transacción
    }

    @Test
    void eliminarConDetalle() throws Exception {
        // Arrange
        when(conMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeUpdate()).thenReturn(1); // Todo se ejecuta bien

        // Act
        UsuarioDao.Baja resultado = usuarioDao.eliminarConDetalle(1);

        // Assert
        assertTrue(resultado.ok());
        assertNull(resultado.error());
        verify(conMock).commit();
    }

    @Test
    void contarHistorial() throws Exception {
        // Arrange
        when(conMock.prepareStatement(anyString())).thenReturn(psMock);
        when(psMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);

        // Simulamos lo que devuelve la consulta dual en la BD
        when(rsMock.getInt(1)).thenReturn(3); // 3 solicitudes
        when(rsMock.getInt(2)).thenReturn(1); // 1 reporte
        when(rsMock.getInt(3)).thenReturn(0); // 0 autorizaciones
        when(rsMock.getInt(4)).thenReturn(2); // 2 acompañamientos

        // Act
        UsuarioDao.Historial historial = usuarioDao.contarHistorial(1);

        // Assert
        assertNotNull(historial);
        assertEquals(3, historial.solicitudes());
        assertEquals(1, historial.reportes());
        assertTrue(historial.destruyeDatos(), "Debería retornar true porque 3 solicitudes + 1 reporte > 0");
    }