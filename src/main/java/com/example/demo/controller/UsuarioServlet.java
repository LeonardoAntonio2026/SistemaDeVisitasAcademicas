package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.model.dao.RolDao;
import com.example.demo.model.dao.TokenRecuperacionDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.EmailSender;
import com.example.demo.utils.SesionUtils;
import com.example.demo.utils.Validador;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Gestión de usuarios (RF-12): alta de cuentas, cambio de rol y baja.
 *
 * El permiso se decide SIEMPRE con el atributo "rol" que LoginServlet guardó en
 * la sesión: nunca con un parámetro de la URL, que el usuario podría escribir a
 * mano. Quien no sea Administrador recibe un 403.
 *
 * El JSP se pinta una sola vez, al entrar (GET sin action). De ahí en adelante
 * todo pasa por JSON y lo consume gestion-usuarios.js con fetch: los modales
 * piden datos con GET y las tres operaciones van por POST.
 */
@WebServlet(name = "UsuarioServlet", value = "/usuarios")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final RolDao rolDao = new RolDao();
    private final TokenRecuperacionDao tokenDao = new TokenRecuperacionDao();
    private final Gson gson = new Gson();
    private final SecureRandom random = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Gestión de usuarios es solo del Administrador; a los demás se les
        // dice por qué no pueden entrar
        if (!SesionUtils.esAdministrador(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Con action el panel pide lo que necesitan sus modales; sin action es
        // la carga normal de la página
        String action = request.getParameter("action");
        if ("editar".equals(action) || "confirmar".equals(action)) {
            responderJson(response, consultar(action, request));
            return;
        }

        request.setAttribute("listaUsuarios", usuarioDao.getAll());
        request.setAttribute("rolesDisponibles", rolDao.getNombres());

        request.getRequestDispatcher("UserManagement.jsp").forward(request, response);
    }

    /** Las tres operaciones responden JSON: el panel se actualiza sin recargar. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!SesionUtils.esAdministrador(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        Respuesta respuesta = switch (action != null ? action : "") {
            case "crear" -> crear(request);
            case "actualizar" -> actualizar(request);
            case "eliminar" -> eliminar(request);
            default -> Respuesta.error("No se pudo completar la operación. Inténtalo de nuevo.");
        };

        responderJson(response, respuesta);
    }

    /** Datos que alimentan los modales: edición y confirmación de baja. */
    private Respuesta consultar(String action, HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        Usuario usuario = (id != null) ? usuarioDao.getById(id) : null;
        if (usuario == null) {
            return Respuesta.error("Ese usuario ya no existe.");
        }

        if ("confirmar".equals(action)) {
            if (id.equals(SesionUtils.idUsuario(request))) {
                return Respuesta.error("No puedes eliminar tu propia cuenta.");
            }
            // El borrado es irreversible, así que primero se le enseña al
            // administrador exactamente qué va a perder
            return new Respuesta(true, null, UsuarioJson.de(usuario), usuarioDao.contarHistorial(id));
        }
        return new Respuesta(true, null, UsuarioJson.de(usuario), null);
    }

    // ==================== Operaciones ====================

    /** Alta de cuenta: valida, resuelve el rol y delega en el DAO (que hashea la contraseña). */
    private Respuesta crear(HttpServletRequest request) {
        String nombre = limpiar(request.getParameter("nombre"));
        String correo = limpiar(request.getParameter("correo"));
        String contrasena = request.getParameter("contrasena");
        String confirmacion = request.getParameter("contrasena2");
        String rol = limpiar(request.getParameter("rol"));

        if (nombre.isEmpty() || correo.isEmpty() || rol.isEmpty()
                || contrasena == null || contrasena.isBlank()) {
            return Respuesta.error("Completa todos los campos obligatorios.");
        }
        if (nombre.length() > 100) {
            return Respuesta.error("El nombre no debe pasar de 100 caracteres.");
        }
        if (!Validador.correoValido(correo)) {
            return Respuesta.error("El correo electrónico no tiene un formato válido.");
        }
        if (!Validador.contrasenaValida(contrasena)) {
            return Respuesta.error(Validador.REGLA_CONTRASENA);
        }
        // La confirmación se vuelve a comparar aquí: el navegador ya avisa al
        // escribir, pero un POST directo se salta esa validación
        if (!contrasena.equals(confirmacion)) {
            return Respuesta.error("Las contraseñas no coinciden.");
        }
        int idRol = rolDao.getIdPorNombre(rol);
        if (idRol == 0) {
            return Respuesta.error("El rol seleccionado no es válido.");
        }
        if (usuarioDao.existeCorreo(correo)) {
            return Respuesta.error("Ya existe una cuenta registrada con ese correo.");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setCorreo(correo);
        nuevo.setIdRol(idRol);
        nuevo.setContrasena(contrasena);

        if (!usuarioDao.create(nuevo)) {
            return Respuesta.error("No se pudo crear el usuario. Inténtalo de nuevo.");
        }
        // create() deja el id generado en la entidad; el nombre del rol se
        // completa para que el panel pueda pintar la fila nueva sin releer la BD
        nuevo.setNombreRol(rol);
        darLaBienvenida(request, nuevo);
        return Respuesta.exito("Usuario creado. Se le envió un correo para que defina su contraseña.", nuevo);
    }

    /**
     * Correo de alta: le avisa al usuario que ya tiene cuenta y le da un enlace
     * para poner su propia contraseña, en vez de mandarle la que escribió el
     * administrador (que quedaría en el correo para siempre).
     *
     * Es el mismo token de la recuperación (RF-02), así que si el enlace vence
     * el usuario puede pedir otro desde "Recuperar contraseña" sin ayuda.
     * Si algo falla aquí no se deshace el alta: la cuenta ya está creada y la
     * contraseña inicial del administrador sigue sirviendo para entrar.
     */
    private void darLaBienvenida(HttpServletRequest request, Usuario nuevo) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        if (!tokenDao.crear(nuevo.getId(), token)) {
            System.err.println("No se pudo generar el enlace de bienvenida para " + nuevo.getCorreo());
            return;
        }

        int puerto = request.getServerPort();
        String sufijoPuerto = (puerto == 80 || puerto == 443) ? "" : ":" + puerto;
        String enlace = request.getScheme() + "://" + request.getServerName() + sufijoPuerto
                + request.getContextPath() + "/restablecer-contrasena?token=" + token;

        String plantillaHtml = """
        <html>
            <body style="font-family: Arial, sans-serif; color: #333333;">
                <h2 style="color: #183052;">¡Hola, {0}!</h2>
                <p>Se creó tu cuenta en el Sistema de Gestión de Visitas Académicas con el rol de <strong>{1}</strong>.</p>
                <p>Entras con este correo: <strong>{2}</strong></p>
                <p>Para definir tu contraseña, <a href="{3}" style="color: #183052;">haz click aquí</a>.
                   El enlace es válido durante <strong>{4} horas</strong>; si vence, usa "Recuperar contraseña"
                   en la pantalla de inicio de sesión.</p>
                <p style="font-size: 12px; color: #777777;">Sistema de Gestión de Visitas Académicas - UTEZ</p>
            </body>
        </html>
        """;
        String cuerpo = MessageFormat.format(plantillaHtml, nuevo.getNombre(), nuevo.getNombreRol(),
                nuevo.getCorreo(), enlace, String.valueOf(TokenRecuperacionDao.HORAS_VIGENCIA));

        // En un hilo aparte: el panel espera el JSON para pintar la fila nueva y
        // el SMTP tarda varios segundos
        EmailSender.sendMailAsync(nuevo.getCorreo(),
                "Tu cuenta en el Sistema de Visitas Académicas", cuerpo);
    }

    /** Edición de nombre, correo y rol. La contraseña no se toca desde aquí. */
    private Respuesta actualizar(HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        Usuario usuario = (id != null) ? usuarioDao.getById(id) : null;
        if (usuario == null) {
            return Respuesta.error("Ese usuario ya no existe.");
        }

        String nombre = limpiar(request.getParameter("nombre"));
        String correo = limpiar(request.getParameter("correo"));
        String rol = limpiar(request.getParameter("rol"));

        if (nombre.isEmpty() || correo.isEmpty() || rol.isEmpty()) {
            return Respuesta.error("Completa todos los campos obligatorios.");
        }
        if (nombre.length() > 100) {
            return Respuesta.error("El nombre no debe pasar de 100 caracteres.");
        }
        if (!Validador.correoValido(correo)) {
            return Respuesta.error("El correo electrónico no tiene un formato válido.");
        }
        int idRol = rolDao.getIdPorNombre(rol);
        if (idRol == 0) {
            return Respuesta.error("El rol seleccionado no es válido.");
        }
        // Si se quitara a sí mismo el rol de admin se quedaría fuera del panel
        if (id.equals(SesionUtils.idUsuario(request)) && idRol != usuario.getIdRol()) {
            return Respuesta.error("No puedes cambiar tu propio rol: perderías el acceso a este panel.");
        }
        // El correo es la credencial de acceso: no puede chocar con otra cuenta
        if (!correo.equalsIgnoreCase(usuario.getCorreo()) && usuarioDao.existeCorreo(correo)) {
            return Respuesta.error("Ya existe una cuenta registrada con ese correo.");
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setIdRol(idRol);
        usuario.setNombreRol(rol);

        if (!usuarioDao.update(usuario)) {
            return Respuesta.error("No se pudieron guardar los cambios del usuario. Inténtalo de nuevo.");
        }
        return Respuesta.exito("Usuario actualizado correctamente.", usuario);
    }

    private Respuesta eliminar(HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            return Respuesta.error("Ese usuario ya no existe.");
        }
        if (id.equals(SesionUtils.idUsuario(request))) {
            return Respuesta.error("No puedes eliminar tu propia cuenta.");
        }

        // Se consultan ANTES: después del borrado ya no hay de dónde sacarlos
        Usuario usuario = usuarioDao.getById(id);
        if (usuario == null) {
            return Respuesta.error("Ese usuario ya no existe.");
        }
        UsuarioDao.Historial historial = usuarioDao.contarHistorial(id);

        UsuarioDao.Baja baja = usuarioDao.eliminarConDetalle(id);
        if (!baja.ok()) {
            if ("ligado".equals(baja.error())) {
                return Respuesta.error("No se puede eliminar este usuario porque está ligado a información "
                        + "de otros registros del sistema. No se borró nada.");
            }
            return Respuesta.error("No se pudo eliminar el usuario. No se borró nada; inténtalo de nuevo.");
        }
        return Respuesta.exito(detalleBaja(historial), usuario);
    }

    /** Resume qué se llevó por delante la baja, para confirmárselo al administrador. */
    private String detalleBaja(UsuarioDao.Historial historial) {
        List<String> borrado = new ArrayList<>();
        if (historial.solicitudes() > 0) {
            borrado.add(historial.solicitudes() == 1 ? "1 solicitud" : historial.solicitudes() + " solicitudes");
        }
        if (historial.reportes() > 0) {
            borrado.add(historial.reportes() == 1 ? "1 reporte" : historial.reportes() + " reportes");
        }
        if (borrado.isEmpty()) {
            return "Usuario eliminado correctamente.";
        }
        return "Usuario eliminado junto con " + String.join(" y ", borrado) + ".";
    }

    // ==================== Respuesta JSON ====================

    private void responderJson(HttpServletResponse response, Respuesta respuesta) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            gson.toJson(respuesta, out);
        }
    }

    /**
     * Sobre común de todas las respuestas. Gson omite los campos nulos, así que
     * cada petición manda solo lo suyo: el usuario para pintar o repintar su
     * fila y, en la confirmación de baja, el conteo de lo que se perdería.
     */
    private record Respuesta(boolean ok, String mensaje, UsuarioJson usuario, UsuarioDao.Historial baja) {
        static Respuesta error(String mensaje) {
            return new Respuesta(false, mensaje, null, null);
        }

        static Respuesta exito(String mensaje, Usuario usuario) {
            return new Respuesta(true, mensaje, UsuarioJson.de(usuario), null);
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

    // ==================== Utilidades ====================

    private Integer parseId(String valor) {
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private String limpiar(String valor) {
        return (valor == null) ? "" : valor.trim();
    }
}
