package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.model.dao.UsuarioDao;
import com.example.demo.utils.SesionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servicio que provee sugerencias de autocompletado para los docentes acompañantes mediante su nombre o
 * correo institucional y retornando los resultados en formato JSON
 * @author Eder Gabriel García Vázquez
 * @since 18/08/2026
 */
@WebServlet(name = "DocenteBuscarServlet", value = "/docentes")
public class DocenteBuscarServlet extends HttpServlet {

    private static final int MAX_SUGERENCIAS = 8;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    /**
     * Procesa las peticiones HTTP {@code GET} para la búsqueda dinámica de docentes.
     * Válida la existencia de una sesión activa para proteger los datos y su acceso.
     * Retorna una cadena JSON con la lista de coincidencias encontradas si el parámetro
     * de consulta {@code q} contiene al menos dos caracteres
     * @param request objeto {@link HttpServletRequest} que contiene el parámetro de búsqueda {@code q}
     * @param response objeto {@link HttpServletResponse} donde se escribe el arreglo JSON
     * @throws ServletException si ocurre un fallo interno en el procesamiento del Servlet
     * @throws IOException si ocurre un error al escribir en la respuesta HTTP
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Este endpoint devuelve nombres y correos institucionales, así que exige sesión
        // por su cuenta y no confía solo en FiltroAutenticacion, igual que el resto de
        // servlets que entregan datos.
        Integer idUsuario = SesionUtils.idUsuario(request);
        if (idUsuario == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");

        String texto = request.getParameter("q");
        if (texto == null || texto.trim().length() < 2) {
            response.getWriter().write("[]");
            return;
        }

        List<Usuario> docentes = usuarioDao.buscarDocentes(texto, idUsuario, MAX_SUGERENCIAS);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < docentes.size(); i++) {
            Usuario d = docentes.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"id\":").append(d.getId())
                .append(",\"nombre\":\"").append(escapar(d.getNombre())).append('"')
                .append(",\"correo\":\"").append(escapar(d.getCorreo())).append("\"}");
        }
        json.append(']');

        try (PrintWriter out = response.getWriter()) {
            out.write(json.toString());
        }
    }

    /**
     * Sanitiza y escapa caracteres especiales dentro de una cadena de texto para asegurar
     * su validez sintáctica de la estructura JSON generada manualmente.
     * @param valor la cadena de texto a sanitizar
     * @return la cadena formateada y segura para su inclusión dentro de las propiedades de un objeto JSON,
     * o una cdena vacía el valor proporcionado es {@code null}
     * @author Eder Gabriel García Vázquez
     * @since 18/08/2026
     */
    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }
}
