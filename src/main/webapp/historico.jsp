<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Mismo guardado que en index.jsp: entrar directo al JSP (sin el servlet)
     deja la lista en null y la página mentiría "Aún no hay solicitudes" --%>
<c:if test="${listaHistorico == null}">
    <c:redirect url="/historico"/>
</c:if>
<% request.setAttribute("pageTitle", "Histórico"); %>
<% request.setAttribute("activeNav", "historico"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/detalle.css">

    <%-- esRevisor lo define layout/header.jsp --%>
    <%-- El docente no ve aquí sus rechazadas: las puede corregir, así que le
         siguen apareciendo en Solicitudes y no se repiten en el histórico.
         Al revisor sí le salen todas las rechazadas (para él ya están
         cerradas), incluidas las suyas si es el Administrador. --%>
    <%-- No todo lo de aquí está terminado: una solicitud cerrada puede seguir
         esperando su reporte. El subtítulo lo dice, en vez de prometer que
         "ya se completó todo" y contradecirse con los badges de abajo. --%>
    <div class="superior">
        <h2>Histórico</h2>
        <p>${!esRevisor ? 'Solicitudes cerradas: el estado indica cómo va el reporte de cada visita'
                       : 'Solicitudes cerradas (con su reporte en curso) y rechazadas'}</p>
    </div>

    <c:choose>
        <c:when test="${empty listaHistorico}">
            <div class="solicitud-vacia">
                <h5>Aún no hay solicitudes cerradas</h5>
                <p>${!esRevisor ? 'Cuando una solicitud se complete aparecerá aquí'
                               : 'Cuando una solicitud se complete o se rechace aparecerá aquí'}</p>
            </div>
        </c:when>
        <c:otherwise>
            <%-- Los filtros van fuera de la tarjeta de la tabla: si la búsqueda
                 no encuentra nada la tabla se oculta y la barra debe seguir ahí --%>
            <div class="filtros" role="search">
                <div class="filtro-campo filtro-campo--busqueda">
                    <label class="form-label" for="filtro-texto">Buscar</label>
                    <input id="filtro-texto" class="form-control" type="search" autocomplete="off"
                           placeholder="${!esRevisor ? 'Empresa o actividad' : 'Empresa, actividad o docente'}">
                </div>

                <%-- Las opciones de estos dos selects las llena
                     filtros-historico.js con lo que hay en la tabla --%>
                <div class="filtro-campo">
                    <label class="form-label" for="filtro-estado">Estado</label>
                    <select id="filtro-estado" class="form-control">
                        <option value="">Todos</option>
                    </select>
                </div>

                <c:if test="${esRevisor}">
                    <div class="filtro-campo">
                        <label class="form-label" for="filtro-docente">Docente</label>
                        <select id="filtro-docente" class="form-control">
                            <option value="">Todos</option>
                        </select>
                    </div>
                </c:if>

                <%-- Las dos fechas comparten etiqueta porque son un solo dato:
                     el rango de la visita. El aria-label las distingue. --%>
                <div class="filtro-campo filtro-campo--rango">
                    <label class="form-label" for="filtro-desde">Fecha de visita</label>
                    <div class="rango-fechas">
                        <input id="filtro-desde" class="form-control" type="date" aria-label="Visita desde">
                        <span class="rango-guion" aria-hidden="true">–</span>
                        <input id="filtro-hasta" class="form-control" type="date" aria-label="Visita hasta">
                    </div>
                </div>

                <button type="button" id="filtro-limpiar" class="btn-base btn-secundario">
                    <i class="bi bi-x-lg"></i> Limpiar
                </button>
            </div>

            <p id="filtro-conteo" class="filtros-conteo" aria-live="polite"></p>

            <div id="card-historico" class="detalle-card" style="margin-top: 0.5rem;">
                <div class="tabla-scroll">
                <table class="tabla-programas">
                    <thead>
                    <tr>
                        <th>Empresa o actividad</th>
                        <c:if test="${esRevisor}"><th>Docente</th></c:if>
                        <th>Alumnos</th>
                        <th>Fecha de visita</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody id="filas-historico">
                    <%-- Cada fila lleva pegado lo que se filtra: el texto de la
                         búsqueda, el estado del badge, el docente y la fecha en
                         yyyy-MM-dd, igual que la del <input type="date"> --%>
                    <c:forEach var="s" items="${listaHistorico}">
                        <tr data-busqueda="<c:out value="${s.nombreEmpresaActividad} ${s.nombreSolicitante}"/>"
                            data-estado="<c:out value="${s.estadoLegible}"/>"
                            data-docente="<c:out value="${s.nombreSolicitante}"/>"
                            data-fecha="<c:out value="${s.fechaInicio}"/>">
                            <td><c:out value="${s.nombreEmpresaActividad}"/></td>
                            <c:if test="${esRevisor}"><td><c:out value="${s.nombreSolicitante}"/></td></c:if>
                            <td>${s.totalEstudiantes}</td>
                            <td>${empty s.fechaInicio ? '—' : s.fechaInicio}</td>
                            <td>
                                <%-- El texto sale del modelo, no de la BD: mientras falte
                                     el reporte, "Completada" no es lo que se muestra --%>
                                <span class="badge-estado estado-${s.claseEstado}">${s.estadoLegible}</span>
                            </td>
                            <td style="white-space: nowrap;">
                                <%-- Consultar va en contorno; el botón se vuelve sólido solo
                                     cuando al docente le toca hacer algo con el reporte --%>
                                <a class="btn-descargar btn-contorno" style="margin-right: 6px;"
                                   href="${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}">Ver solicitud</a>
                                <c:if test="${s.nombreEstado == 'Completada' && s.idReporte != null}">
                                    <c:set var="esPropia" value="${sessionScope.idUsuario == s.idUsuarioSolicitante}"/>
                                    <c:choose>
                                        <c:when test="${esPropia && s.estadoReporte == 'Pendiente'}">
                                            <a class="btn-descargar"
                                               href="${pageContext.request.contextPath}/reporte?id=${s.idReporte}">Completar reporte</a>
                                        </c:when>
                                        <c:when test="${esPropia && s.estadoReporte == 'Rechazado'}">
                                            <a class="btn-descargar"
                                               href="${pageContext.request.contextPath}/reporte?id=${s.idReporte}">Corregir reporte</a>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="btn-descargar btn-contorno"
                                               href="${pageContext.request.contextPath}/reporte?id=${s.idReporte}">Ver reporte</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                </div>
            </div>

            <%-- Los botones de página los pone filtros-historico.js: cuántos
                 son depende de cuántas solicitudes queden tras filtrar. --%>
            <nav id="paginacion" class="paginacion" aria-label="Páginas del histórico" hidden></nav>

            <%-- Convive con la tabla igual que en gestión de usuarios: se
                 alterna cuál de los dos está oculto, sin recargar. --%>
            <div id="sin-resultados" class="solicitud-vacia" hidden>
                <h5>Ninguna solicitud coincide con la búsqueda</h5>
                <p>Prueba con otras palabras o quita alguno de los filtros</p>
            </div>

            <script src="${pageContext.request.contextPath}/js/filtros-historico.js" defer></script>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="layout/footer.jsp" %>
