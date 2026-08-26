package com.example.demo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.demo.model.Solicitud;
import com.example.demo.model.dao.SolicitudDao;
import com.example.demo.utils.SesionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de inicio del sistema: la lista de solicitudes ACTIVAS.
 * <p>
 * Lo que se ve depende del rol. El docente ve las suyas; Estadías ve las que
 * los docentes ya enviaron a revisión; el Administrador, que revisa y además
 * solicita, ve las dos cosas. Las solicitudes terminadas no salen aquí: viven
 * en el histórico (RN-05).
 * </p>
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 25/08/2026
 */
@WebServlet(name = "indexSv", value = "/indexSv")
public class IndexSv extends HttpServlet {

    private final SolicitudDao solicitudDao = new SolicitudDao();

    /**
     * Arma la lista de solicitudes activas que le toca ver al usuario en sesión
     * y la manda a {@code index.jsp} en el atributo {@code listaSolicitudes}.
     *
     * @param req  petición de la que se lee la sesión para saber quién entra
     * @param resp respuesta usada para el forward a la vista
     * @throws ServletException si falla el forward a {@code index.jsp}
     * @throws IOException      si ocurre un error de entrada/salida
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer idUsuario = SesionUtils.idUsuario(req);

        // En el inicio solo van las ACTIVAS (las terminadas viven en el histórico, RN-05).
        // El docente ve las suyas; Estadías ve las enviadas por los docentes
        // (las Pendientes aún no se envían, por eso no le aparecen). El
        // Administrador revisa y además solicita: ve las dos cosas.
        List<Solicitud> solicitudes;
        if (idUsuario == null) {
            solicitudes = new ArrayList<>();
        } else if (SesionUtils.esRevisor(req)) {
            solicitudes = solicitudDao.getActivasParaRevision(
                    SesionUtils.puedeSolicitar(req) ? idUsuario : null);
        } else {
            solicitudes = solicitudDao.getActivasBySolicitante(idUsuario);
        }
        req.setAttribute("listaSolicitudes", solicitudes);

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }

    /**
     * El inicio no recibe formularios: cualquier POST se responde con un
     * redirect al GET, siguiendo el patrón PRG del resto del sistema.
     *
     * @param req  petición recibida, no se lee
     * @param resp respuesta donde se escribe el redirect
     * @throws ServletException si falla el procesamiento de la petición
     * @throws IOException      si ocurre un error de entrada/salida
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("indexSv");
    }
}
