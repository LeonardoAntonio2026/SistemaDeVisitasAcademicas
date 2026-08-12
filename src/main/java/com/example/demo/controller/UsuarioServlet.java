package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.model.dao.RolDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.Validador;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de usuarios (RF-12): alta de cuentas, cambio de rol y baja.
 *
 * El permiso se decide SIEMPRE con el atributo "rol" que LoginServlet guardó en
 * la sesión: nunca con un parámetro de la URL, que el usuario podría escribir a
 * mano. Quien no sea Administrador se va al inicio.
 *
 * El servlet responde de dos formas y la lógica es la misma para las dos:
 *  - HTML (por defecto): forward al JSP y patrón PRG en los POST, para que el
 *    panel siga funcionando aunque el navegador no ejecute JavaScript.
 *  - JSON (con ?formato=json): lo que consume gestion-usuarios.js con fetch
 *    para dar de alta, editar y borrar sin recargar la página.
 */
@WebServlet(name = "UsuarioServlet", value = "/usuarios")
public class UsuarioServlet extends HttpServlet {

    private static final String ROL_ADMIN = "Administrador";

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final RolDao rolDao = new RolDao();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Gestión de usuarios es solo del Administrador: a los demás se les dice
        // por qué no pueden entrar, en vez de rebotarlos al inicio sin explicación
        if (!esAdministrador(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String accion = request.getParameter("action");

        // El panel pide por fetch lo que necesitan sus modales: los datos del
        // usuario que se va a editar, o el resumen de lo que se perdería al
        // darlo de baja. Sin JavaScript el flujo sigue por las ramas de abajo.
        if (pideJson(request)) {
            responderJson(response, consultar(accion, request));
            return;
        }

        // Editar: el mismo formulario de alta, precargado con el usuario elegido
        if ("editar".equals(accion)) {
            Integer id = parseId(request.getParameter("id"));
            Usuario enEdicion = (id != null) ? usuarioDao.getById(id) : null;
            if (enEdicion != null) {
                request.setAttribute("usuarioEditado", enEdicion);
            }
        }

        // Confirmar baja: la eliminación es irreversible y se lleva las solicitudes
        // y reportes del usuario, así que primero se le enseña al administrador
        // exactamente qué va a perder.
        if ("confirmar".equals(accion)) {
            Integer id = parseId(request.getParameter("id"));
            Usuario aBorrar = (id != null) ? usuarioDao.getById(id) : null;
            if (aBorrar != null && !id.equals(idEnSesion(request))) {
                UsuarioDao.Historial h = usuarioDao.contarHistorial(id);
                request.setAttribute("usuarioABorrar", aBorrar);
                request.setAttribute("bajaSolicitudes", h.solicitudes());
                request.setAttribute("bajaReportes", h.reportes());
                request.setAttribute("bajaAutorizaciones", h.autorizaciones());
                request.setAttribute("bajaAcompanamientos", h.acompanamientos());
            }
        }

        request.setAttribute("listaUsuarios", usuarioDao.getAll());
        request.setAttribute("rolesDisponibles", rolDao.getNombres());
        traducirAvisos(request);

        request.getRequestDispatcher("UserManagement.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Gestión de usuarios es solo del Administrador: a los demás se les dice
        // por qué no pueden entrar, en vez de rebotarlos al inicio sin explicación
        if (!esAdministrador(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        Resultado resultado = switch (action != null ? action : "") {
            case "crear" -> crear(request);
            case "actualizar" -> actualizar(request);
            case "eliminar" -> eliminar(request);
            default -> Resultado.error("desconocida");
        };

        // Con fetch la respuesta se queda en la página: el panel pinta el aviso
        // y repinta solo la fila que cambió, sin perder lo que hubiera escrito
        if (pideJson(request)) {
            responderJson(response, aRespuesta(resultado));
            return;
        }

        // Patrón PRG: el resultado viaja en la URL para que recargar no repita la operación
        response.sendRedirect(destino(resultado, action, request));
    }

    /** Alta de cuenta: valida, resuelve el rol y delega en el DAO (que hashea la contraseña). */
    private Resultado crear(HttpServletRequest request) {
        String nombre = limpiar(request.getParameter("nombre"));
        String correo = limpiar(request.getParameter("correo"));
        String contrasena = request.getParameter("contrasena");
        String confirmacion = request.getParameter("contrasena2");
        String rol = limpiar(request.getParameter("rol"));

        if (nombre.isEmpty() || correo.isEmpty() || rol.isEmpty()
                || contrasena == null || contrasena.isBlank()) {
            return Resultado.error("campos");
        }
        if (nombre.length() > 100) {
            return Resultado.error("nombrelargo");
        }
        if (!Validador.correoValido(correo)) {
            return Resultado.error("correoformato");
        }
        if (!Validador.contrasenaValida(contrasena)) {
            return Resultado.error("contrasena");
        }
        // La confirmación se vuelve a comparar aquí: el navegador ya avisa al
        // escribir, pero un POST directo se salta esa validación
        if (!contrasena.equals(confirmacion)) {
            return Resultado.error("contrasenas");
        }
        int idRol = rolDao.getIdPorNombre(rol);
        if (idRol == 0) {
            return Resultado.error("rol");
        }
        if (usuarioDao.existeCorreo(correo)) {
            return Resultado.error("correo");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setCorreo(correo);
        nuevo.setIdRol(idRol);
        nuevo.setContrasena(contrasena);

        if (!usuarioDao.create(nuevo)) {
            return Resultado.error("crear");
        }
        // create() deja el id generado en la entidad; el nombre del rol se
        // completa para que el panel pueda pintar la fila nueva sin releer la BD
        nuevo.setNombreRol(rol);
        return Resultado.exito("creado", nuevo);
    }

    /** Edición de nombre, correo y rol. La contraseña no se toca desde aquí. */
    private Resultado actualizar(HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            return Resultado.error("noexiste");
        }
        Usuario usuario = usuarioDao.getById(id);
        if (usuario == null) {
            return Resultado.error("noexiste");
        }

        String nombre = limpiar(request.getParameter("nombre"));
        String correo = limpiar(request.getParameter("correo"));
        String rol = limpiar(request.getParameter("rol"));

        if (nombre.isEmpty() || correo.isEmpty() || rol.isEmpty()) {
            return Resultado.error("campos");
        }
        if (nombre.length() > 100) {
            return Resultado.error("nombrelargo");
        }
        if (!Validador.correoValido(correo)) {
            return Resultado.error("correoformato");
        }
        int idRol = rolDao.getIdPorNombre(rol);
        if (idRol == 0) {
            return Resultado.error("rol");
        }
        // Si se quitara a sí mismo el rol de admin se quedaría fuera del panel
        if (id.equals(idEnSesion(request)) && idRol != usuario.getIdRol()) {
            return Resultado.error("autorol");
        }
        // El correo es la credencial de acceso: no puede chocar con otra cuenta
        if (!correo.equalsIgnoreCase(usuario.getCorreo()) && usuarioDao.existeCorreo(correo)) {
            return Resultado.error("correo");
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setIdRol(idRol);
        usuario.setNombreRol(rol);

        return usuarioDao.update(usuario)
                ? Resultado.exito("actualizado", usuario)
                : Resultado.error("actualizar");
    }

    private Resultado eliminar(HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            return Resultado.error("noexiste");
        }
        if (id.equals(idEnSesion(request))) {
            return Resultado.error("autoeliminar");
        }

        // Se consultan ANTES: después del borrado ya no hay de dónde sacarlos
        Usuario usuario = usuarioDao.getById(id);
        if (usuario == null) {
            return Resultado.error("noexiste");
        }
        UsuarioDao.Historial h = usuarioDao.contarHistorial(id);

        UsuarioDao.Baja baja = usuarioDao.eliminarConDetalle(id);
        if (!baja.ok()) {
            return Resultado.error(baja.error());
        }
        return new Resultado(true, "eliminado", usuario, h);
    }

    // ==================== Salida HTML (flujo sin JavaScript) ====================

    /**
     * A dónde vuelve el navegador después del POST. El resultado viaja como
     * clave en la URL (patrón PRG) y traducirAvisos lo convierte en texto.
     */
    private String destino(Resultado resultado, String action, HttpServletRequest request) {
        if (resultado.ok()) {
            if (resultado.historial() != null) {
                return "usuarios?ok=" + resultado.clave()
                        + "&s=" + resultado.historial().solicitudes()
                        + "&r=" + resultado.historial().reportes();
            }
            return "usuarios?ok=" + resultado.clave();
        }

        // Si algo falla al editar, el redirect conserva action=editar&id: sin
        // esto el error caía en el formulario de "Nuevo usuario" vacío y el
        // administrador perdía la edición que traía a medias.
        Integer id = parseId(request.getParameter("id"));
        if ("actualizar".equals(action) && id != null && !"noexiste".equals(resultado.clave())) {
            return "usuarios?action=editar&id=" + id + "&error=" + resultado.clave();
        }
        return "usuarios?error=" + resultado.clave();
    }

    /** Convierte los códigos que viajan en la URL (PRG) al texto que ve el usuario. */
    private void traducirAvisos(HttpServletRequest request) {
        String ok = request.getParameter("ok");
        if (ok != null) {
            request.setAttribute("mensaje", mensajeExito(ok,
                    parseConteo(request.getParameter("s")),
                    parseConteo(request.getParameter("r"))));
        }
        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", mensajeError(error));
        }
    }

    // ==================== Salida JSON (panel con fetch) ====================

    /** El panel pide JSON con ?formato=json; el resto del sitio sigue recibiendo HTML. */
    private boolean pideJson(HttpServletRequest request) {
        return "json".equals(request.getParameter("formato"));
    }

    /** Datos que alimentan los modales del panel: edición y confirmación de baja. */
    private Respuesta consultar(String accion, HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        Usuario usuario = (id != null) ? usuarioDao.getById(id) : null;
        if (usuario == null) {
            return Respuesta.error(mensajeError("noexiste"));
        }

        if ("confirmar".equals(accion)) {
            if (id.equals(idEnSesion(request))) {
                return Respuesta.error(mensajeError("autoeliminar"));
            }
            return new Respuesta(true, null, UsuarioJson.de(usuario), usuarioDao.contarHistorial(id));
        }
        return new Respuesta(true, null, UsuarioJson.de(usuario), null);
    }

    /** El mismo resultado del POST, pero con el texto ya armado para el panel. */
    private Respuesta aRespuesta(Resultado resultado) {
        if (!resultado.ok()) {
            return Respuesta.error(mensajeError(resultado.clave()));
        }
        UsuarioDao.Historial h = resultado.historial();
        String mensaje = mensajeExito(resultado.clave(),
                h != null ? h.solicitudes() : 0,
                h != null ? h.reportes() : 0);
        return new Respuesta(true, mensaje, UsuarioJson.de(resultado.usuario()), null);
    }

    private void responderJson(HttpServletResponse response, Respuesta respuesta) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            gson.toJson(respuesta, out);
        }
    }

    // ==================== Avisos (compartidos por las dos salidas) ====================

    /** Los conteos solo los usa la baja, que resume qué se llevó por delante. */
    private String mensajeExito(String clave, int solicitudes, int reportes) {
        return switch (clave) {
            case "creado" -> "Usuario creado correctamente.";
            case "actualizado" -> "Usuario actualizado correctamente.";
            case "eliminado" -> detalleBaja(solicitudes, reportes);
            default -> "Operación completada.";
        };
    }

    private String mensajeError(String clave) {
        return switch (clave) {
            case "campos" -> "Completa todos los campos obligatorios.";
            case "correo" -> "Ya existe una cuenta registrada con ese correo.";
            case "correoformato" -> "El correo electrónico no tiene un formato válido.";
            case "contrasena" -> Validador.REGLA_CONTRASENA;
            case "contrasenas" -> "Las contraseñas no coinciden.";
            case "nombrelargo" -> "El nombre no debe pasar de 100 caracteres.";
            case "rol" -> "El rol seleccionado no es válido.";
            case "noexiste" -> "Ese usuario ya no existe.";
            case "autorol" -> "No puedes cambiar tu propio rol: perderías el acceso a este panel.";
            case "autoeliminar" -> "No puedes eliminar tu propia cuenta.";
            case "eliminar" -> "No se pudo eliminar el usuario. No se borró nada; inténtalo de nuevo.";
            case "ligado" -> "No se puede eliminar este usuario porque está ligado a información de otros "
                    + "registros del sistema. No se borró nada.";
            case "actualizar" -> "No se pudieron guardar los cambios del usuario. Inténtalo de nuevo.";
            case "crear" -> "No se pudo crear el usuario. Inténtalo de nuevo.";
            default -> "No se pudo completar la operación. Inténtalo de nuevo.";
        };
    }

    /** Resume qué se llevó por delante la baja, para confirmárselo al administrador. */
    private String detalleBaja(int solicitudes, int reportes) {
        List<String> borrado = new ArrayList<>();
        if (solicitudes > 0) {
            borrado.add(solicitudes == 1 ? "1 solicitud" : solicitudes + " solicitudes");
        }
        if (reportes > 0) {
            borrado.add(reportes == 1 ? "1 reporte" : reportes + " reportes");
        }
        if (borrado.isEmpty()) {
            return "Usuario eliminado correctamente.";
        }
        return "Usuario eliminado junto con " + String.join(" y ", borrado) + ".";
    }

    // ==================== Tipos de apoyo ====================

    /**
     * Lo que dejó una operación de alta, edición o baja: la clave del aviso, el
     * usuario afectado y —solo en la baja— el conteo de lo que se borró con él.
     * De aquí salen las dos respuestas del servlet, así la validación y las
     * reglas viven en un solo lugar.
     */
    private record Resultado(boolean ok, String clave, Usuario usuario, UsuarioDao.Historial historial) {
        static Resultado error(String clave) {
            return new Resultado(false, clave, null, null);
        }

        static Resultado exito(String clave, Usuario usuario) {
            return new Resultado(true, clave, usuario, null);
        }
    }

    /**
     * El usuario tal como sale a la vista: solo lo que la tabla del panel pinta.
     * No se serializa la entidad Usuario para no arriesgarse a mandar de vuelta
     * nada relacionado con su acceso.
     */
    private record UsuarioJson(int id, String nombre, String correo, String rol) {
        static UsuarioJson de(Usuario u) {
            return new UsuarioJson(u.getId(), u.getNombre(), u.getCorreo(), u.getNombreRol());
        }
    }

    /**
     * Sobre común de las respuestas JSON. Gson omite los campos nulos, así que
     * cada petición manda solo lo suyo: el usuario para repintar su fila y, en
     * la confirmación de baja, el conteo de lo que se perdería.
     */
    private record Respuesta(boolean ok, String mensaje, UsuarioJson usuario, UsuarioDao.Historial baja) {
        static Respuesta error(String mensaje) {
            return new Respuesta(false, mensaje, null, null);
        }
    }

    // ==================== Utilidades ====================

    private boolean esAdministrador(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String rol = (session != null) ? (String) session.getAttribute("rol") : null;
        return ROL_ADMIN.equalsIgnoreCase(rol);
    }

    private Integer idEnSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Integer) session.getAttribute("idUsuario") : null;
    }

    private Integer parseId(String valor) {
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private int parseConteo(String valor) {
        try {
            return Math.max(0, Integer.parseInt(valor));
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    private String limpiar(String valor) {
        return (valor == null) ? "" : valor.trim();
    }
}
