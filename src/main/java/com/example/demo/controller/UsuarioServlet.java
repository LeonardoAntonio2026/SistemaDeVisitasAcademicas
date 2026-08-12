package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.model.dao.RolDao;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de usuarios (RF-12): alta de cuentas, cambio de rol y baja.
 *
 * El permiso se decide SIEMPRE con el atributo "rol" que LoginServlet guardó en
 * la sesión: nunca con un parámetro de la URL, que el usuario podría escribir a
 * mano. Quien no sea Administrador se va al inicio.
 */
@WebServlet(name = "UsuarioServlet", value = "/usuarios")
public class UsuarioServlet extends HttpServlet {

    private static final String ROL_ADMIN = "Administrador";

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final RolDao rolDao = new RolDao();

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
        String destino;

        if ("crear".equals(action)) {
            destino = crear(request);
        } else if ("actualizar".equals(action)) {
            destino = actualizar(request);
        } else if ("eliminar".equals(action)) {
            destino = eliminar(request);
        } else {
            destino = "usuarios";
        }

        // Patrón PRG: el resultado viaja en la URL para que recargar no repita la operación
        response.sendRedirect(destino);
    }

    /** Alta de cuenta: valida, resuelve el rol y delega en el DAO (que hashea la contraseña). */
    private String crear(HttpServletRequest request) {
        String nombre = limpiar(request.getParameter("nombre"));
        String correo = limpiar(request.getParameter("correo"));
        String contrasena = request.getParameter("contrasena");
        String rol = limpiar(request.getParameter("rol"));

        if (nombre.isEmpty() || correo.isEmpty() || rol.isEmpty()
                || contrasena == null || contrasena.isBlank()) {
            return "usuarios?error=campos";
        }
        if (nombre.length() > 100) {
            return "usuarios?error=nombrelargo";
        }
        if (!Validador.correoValido(correo)) {
            return "usuarios?error=correoformato";
        }
        if (!Validador.contrasenaValida(contrasena)) {
            return "usuarios?error=contrasena";
        }
        int idRol = rolDao.getIdPorNombre(rol);
        if (idRol == 0) {
            return "usuarios?error=rol";
        }
        if (usuarioDao.existeCorreo(correo)) {
            return "usuarios?error=correo";
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setCorreo(correo);
        nuevo.setIdRol(idRol);
        nuevo.setContrasena(contrasena);

        return usuarioDao.create(nuevo) ? "usuarios?ok=creado" : "usuarios?error=crear";
    }

    /** Edición de nombre, correo y rol. La contraseña no se toca desde aquí. */
    private String actualizar(HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            return "usuarios?error=noexiste";
        }
        Usuario usuario = usuarioDao.getById(id);
        if (usuario == null) {
            return "usuarios?error=noexiste";
        }

        // Si algo falla, el redirect conserva action=editar&id: sin esto el
        // error caía en el formulario de "Nuevo usuario" vacío y el
        // administrador perdía la edición que traía a medias.
        String volverAEditar = "usuarios?action=editar&id=" + id;

        String nombre = limpiar(request.getParameter("nombre"));
        String correo = limpiar(request.getParameter("correo"));
        String rol = limpiar(request.getParameter("rol"));

        if (nombre.isEmpty() || correo.isEmpty() || rol.isEmpty()) {
            return volverAEditar + "&error=campos";
        }
        if (nombre.length() > 100) {
            return volverAEditar + "&error=nombrelargo";
        }
        if (!Validador.correoValido(correo)) {
            return volverAEditar + "&error=correoformato";
        }
        int idRol = rolDao.getIdPorNombre(rol);
        if (idRol == 0) {
            return volverAEditar + "&error=rol";
        }
        // Si se quitara a sí mismo el rol de admin se quedaría fuera del panel
        if (id.equals(idEnSesion(request)) && idRol != usuario.getIdRol()) {
            return volverAEditar + "&error=autorol";
        }
        // El correo es la credencial de acceso: no puede chocar con otra cuenta
        if (!correo.equalsIgnoreCase(usuario.getCorreo()) && usuarioDao.existeCorreo(correo)) {
            return volverAEditar + "&error=correo";
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setIdRol(idRol);

        return usuarioDao.update(usuario) ? "usuarios?ok=actualizado" : volverAEditar + "&error=actualizar";
    }

    private String eliminar(HttpServletRequest request) {
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            return "usuarios?error=noexiste";
        }
        if (id.equals(idEnSesion(request))) {
            return "usuarios?error=autoeliminar";
        }

        // Se cuenta ANTES: después del borrado ya no hay de dónde sacarlo
        UsuarioDao.Historial h = usuarioDao.contarHistorial(id);
        UsuarioDao.Baja baja = usuarioDao.eliminarConDetalle(id);
        if (!baja.ok()) {
            return "usuarios?error=" + baja.error();
        }
        return "usuarios?ok=eliminado&s=" + h.solicitudes() + "&r=" + h.reportes();
    }

    /** Resume qué se llevó por delante la baja, para confirmárselo al administrador. */
    private String detalleBaja(HttpServletRequest request) {
        int solicitudes = parseConteo(request.getParameter("s"));
        int reportes = parseConteo(request.getParameter("r"));

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

    private int parseConteo(String valor) {
        try {
            return Math.max(0, Integer.parseInt(valor));
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    /** Convierte los códigos que viajan en la URL (PRG) al texto que ve el usuario. */
    private void traducirAvisos(HttpServletRequest request) {
        String ok = request.getParameter("ok");
        if ("creado".equals(ok)) {
            request.setAttribute("mensaje", "Usuario creado correctamente.");
        } else if ("actualizado".equals(ok)) {
            request.setAttribute("mensaje", "Usuario actualizado correctamente.");
        } else if ("eliminado".equals(ok)) {
            request.setAttribute("mensaje", detalleBaja(request));
        }

        String error = request.getParameter("error");
        if (error == null) {
            return;
        }
        switch (error) {
            case "campos" -> request.setAttribute("error", "Completa todos los campos obligatorios.");
            case "correo" -> request.setAttribute("error", "Ya existe una cuenta registrada con ese correo.");
            case "correoformato" -> request.setAttribute("error", "El correo electrónico no tiene un formato válido.");
            case "contrasena" -> request.setAttribute("error", Validador.REGLA_CONTRASENA);
            case "nombrelargo" -> request.setAttribute("error", "El nombre no debe pasar de 100 caracteres.");
            case "rol" -> request.setAttribute("error", "El rol seleccionado no es válido.");
            case "noexiste" -> request.setAttribute("error", "Ese usuario ya no existe.");
            case "autorol" -> request.setAttribute("error", "No puedes cambiar tu propio rol: perderías el acceso a este panel.");
            case "autoeliminar" -> request.setAttribute("error", "No puedes eliminar tu propia cuenta.");
            case "eliminar" -> request.setAttribute("error",
                    "No se pudo eliminar el usuario. No se borró nada; inténtalo de nuevo.");
            case "ligado" -> request.setAttribute("error",
                    "No se puede eliminar este usuario porque está ligado a información de otros "
                            + "registros del sistema. No se borró nada.");
            case "actualizar" -> request.setAttribute("error",
                    "No se pudieron guardar los cambios del usuario. Inténtalo de nuevo.");
            case "crear" -> request.setAttribute("error",
                    "No se pudo crear el usuario. Inténtalo de nuevo.");
            default -> request.setAttribute("error", "No se pudo completar la operación. Inténtalo de nuevo.");
        }
    }

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

    private String limpiar(String valor) {
        return (valor == null) ? "" : valor.trim();
    }
}
