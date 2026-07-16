<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% request.setAttribute("pageTitle", "Reportes"); %>
<% request.setAttribute("activeNav", "reportes"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">

    <c:set var="esDocente" value="${sessionScope.rol == null || sessionScope.rol == 'Docente'}"/>

    <div class="superior">
        <h2>Reportes</h2>
        <p>
            <c:choose>
                <c:when test="${esDocente}">Reportes pendientes por subir de tus visitas realizadas</c:when>
                <c:otherwise>Reportes de los docentes pendientes de subir y recientemente subidos</c:otherwise>
            </c:choose>
        </p>
    </div>

    <h2 class="titulo-solicitudes">Reportes recientes</h2>

    <c:choose>
        <c:when test="${empty listaReportes}">
            <div class="solicitud-vacia">
                <h5>No hay reportes por ahora</h5>
                <p>
                    <c:choose>
                        <c:when test="${esDocente}">Cuando completes una solicitud se creará aquí su reporte de visita</c:when>
                        <c:otherwise>Cuando un docente complete una solicitud aparecerá aquí su reporte</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach var="r" items="${listaReportes}">
                <div class="solicitud-card">
                    <div class="solicitud-card-top">
                        <div>
                            <h5 class="solicitud-empresa">${r.nombreEmpresaActividad}</h5>
                            <div class="solicitud-ubicacion">
                                <i class="bi bi-pin-map"></i>
                                <span>${empty r.lugarDireccion ? 'Sin dirección' : r.lugarDireccion}</span>
                            </div>
                            <c:if test="${!esDocente}">
                                <div class="solicitud-ubicacion">
                                    <i class="bi bi-person"></i>
                                    <span>${r.nombreSolicitante}</span>
                                </div>
                            </c:if>
                        </div>
                        <span class="badge-estado estado-${fn:toLowerCase(r.nombreEstado)}">${r.nombreEstado}</span>
                    </div>

                    <div class="solicitud-card-bottom">
                        <div class="solicitud-meta">
                            <div class="meta-item">
                                <span class="meta-label">Alumnos</span>
                                <span class="meta-valor">${r.totalEstudiantes}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Solicitada</span>
                                <span class="meta-valor">${r.fechaSolicitud}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Fecha de visita</span>
                                <span class="meta-valor">${r.fecha}</span>
                            </div>
                        </div>
                        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                            <a class="btn-ver-detalles" style="text-decoration: none; background-color: #5A5A5A;"
                               href="${pageContext.request.contextPath}/detalle?id=${r.idSolicitud}">Ver solicitud</a>
                            <c:choose>
                                <c:when test="${esDocente && r.nombreEstado == 'Pendiente'}">
                                    <button type="button" class="btn-ver-detalles" disabled
                                            title="Próximamente: formulario del reporte">Completar formulario</button>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" class="btn-ver-detalles" disabled
                                            title="Próximamente">Ver reporte</button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="layout/footer.jsp" %>
