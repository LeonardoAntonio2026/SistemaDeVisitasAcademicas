<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% request.setAttribute("pageTitle", "Histórico"); %>
<% request.setAttribute("activeNav", "historico"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/detalle.css">

    <c:set var="esDocente" value="${sessionScope.rol == null || sessionScope.rol == 'Docente'}"/>

    <div class="superior">
        <h2>Histórico</h2>
        <p>Solicitudes terminadas: completadas y rechazadas</p>
    </div>

    <c:choose>
        <c:when test="${empty listaHistorico}">
            <div class="solicitud-vacia">
                <h5>Aún no hay solicitudes terminadas</h5>
                <p>Cuando una solicitud se complete o se rechace aparecerá aquí</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="detalle-card" style="margin-top: 1rem; overflow-x: auto;">
                <table class="tabla-programas" style="margin-top: 0;">
                    <thead>
                    <tr>
                        <th>Empresa o actividad</th>
                        <c:if test="${!esDocente}"><th>Docente</th></c:if>
                        <th>Alumnos</th>
                        <th>Fecha de visita</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="s" items="${listaHistorico}">
                        <tr>
                            <td>${s.nombreEmpresaActividad}</td>
                            <c:if test="${!esDocente}"><td>${s.nombreSolicitante}</td></c:if>
                            <td>${s.totalEstudiantes}</td>
                            <td>${empty s.fechaInicio ? '—' : s.fechaInicio}</td>
                            <td>
                                <%-- "Completada" se lee como "ya no hay nada que hacer", así que
                                     mientras falte el reporte se muestra que sigue pendiente. --%>
                                <c:choose>
                                    <c:when test="${s.nombreEstado == 'Completada'
                                                    && (empty s.estadoReporte || s.estadoReporte == 'Pendiente')}">
                                        <span class="badge-estado estado-reporte-pendiente">Reporte pendiente</span>
                                    </c:when>
                                    <c:when test="${s.nombreEstado == 'Completada' && s.estadoReporte == 'Rechazado'}">
                                        <span class="badge-estado estado-reporte-rechazado">Reporte rechazado</span>
                                    </c:when>
                                    <c:when test="${s.nombreEstado == 'Completada' && s.estadoReporte == 'Completado'}">
                                        <span class="badge-estado estado-completado">Reporte en revisión</span>
                                    </c:when>
                                    <c:when test="${s.nombreEstado == 'Completada' && s.estadoReporte == 'Aprobado'}">
                                        <span class="badge-estado estado-aprobado">Reporte aprobado</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-estado estado-${fn:replace(fn:toLowerCase(s.nombreEstado), ' ', '-')}">${s.nombreEstado}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="white-space: nowrap;">
                                <a class="btn-descargar" style="margin-right: 6px;"
                                   href="${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}">Ver solicitud</a>
                                <c:if test="${s.nombreEstado == 'Completada' && s.idReporte != null}">
                                    <a class="btn-descargar btn-verde"
                                       href="${pageContext.request.contextPath}/reporte?id=${s.idReporte}">Ver reporte</a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="layout/footer.jsp" %>
