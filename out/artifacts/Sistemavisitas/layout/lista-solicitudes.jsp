<%@ page pageEncoding="UTF-8" %>
<%-- Fragmento reutilizable: tarjetas de solicitudes (usado por index.jsp y solicitudes.jsp).
     Espera el atributo "listaSolicitudes" en el request. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="esDocente" value="${sessionScope.rol == null || sessionScope.rol == 'Docente'}"/>

<c:choose>
    <c:when test="${empty listaSolicitudes}">
        <div class="solicitud-vacia">
            <c:choose>
                <c:when test="${esDocente}">
                    <h5>No tienes ninguna solicitud</h5>
                    <p>Puedes crear una nueva</p>
                    <a href="${pageContext.request.contextPath}/solicitud?action=nueva" class="btn-nueva-vacia">Nueva solicitud</a>
                </c:when>
                <c:otherwise>
                    <h5>No hay solicitudes por ahora</h5>
                    <p>Cuando un docente envíe una solicitud aparecerá aquí</p>
                </c:otherwise>
            </c:choose>
        </div>
    </c:when>
    <c:otherwise>
        <c:forEach var="s" items="${listaSolicitudes}">
            <div class="solicitud-card">
                <div class="solicitud-card-top">
                    <div>
                        <h5 class="solicitud-empresa">${s.nombreEmpresaActividad}</h5>
                        <div class="solicitud-ubicacion">
                            <i class="bi bi-pin-map"></i>
                            <span>${empty s.lugarDireccion ? 'Sin dirección' : s.lugarDireccion}</span>
                        </div>
                    </div>
                    <span class="badge-estado estado-${fn:replace(fn:toLowerCase(s.nombreEstado), ' ', '-')}">${s.nombreEstado}</span>
                </div>

                <%-- Stepper compacto: mismo componente que la página de detalles --%>
                <c:set var="stepperEstado" value="${s.nombreEstado}"/>
                <%@ include file="stepper.jsp" %>

                <div class="solicitud-card-bottom">
                    <div class="solicitud-meta">
                        <div class="meta-item">
                            <span class="meta-label">Alumnos</span>
                            <span class="meta-valor">${s.totalEstudiantes}</span>
                        </div>
                        <div class="meta-item">
                            <span class="meta-label">Solicitada</span>
                            <span class="meta-valor">${s.fechaCreacion}</span>
                        </div>
                        <c:if test="${not empty s.fechaInicio}">
                            <div class="meta-item">
                                <span class="meta-label">Visita</span>
                                <span class="meta-valor">${s.fechaInicio}</span>
                            </div>
                        </c:if>
                    </div>
                    <a class="btn-ver-detalles" style="text-decoration: none;"
                       href="${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}">Ver detalles</a>
                </div>
            </div>
        </c:forEach>
    </c:otherwise>
</c:choose>
