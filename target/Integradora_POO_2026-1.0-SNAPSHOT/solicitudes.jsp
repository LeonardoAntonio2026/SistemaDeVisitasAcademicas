<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("pageTitle", "Solicitudes"); %>
<% request.setAttribute("activeNav", "solicitudes"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stepper.css">

    <div class="superior">
        <h2>Solicitudes</h2>
        <p>
            <c:choose>
                <c:when test="${sessionScope.rol == null || sessionScope.rol == 'Docente'}">Consulta el estado de tus solicitudes de visita</c:when>
                <c:otherwise>Solicitudes activas y pendientes por revisar enviadas por los docentes</c:otherwise>
            </c:choose>
        </p>
    </div>

    <c:if test="${sessionScope.rol == null || sessionScope.rol == 'Docente'}">
        <a href="${pageContext.request.contextPath}/solicitud?action=nueva" class="botonnueva">Nueva Solicitud</a>
    </c:if>

    <h2 class="titulo-solicitudes">Solicitudes recientes</h2>

    <%@ include file="layout/lista-solicitudes.jsp" %>
</main>

<%@ include file="layout/footer.jsp" %>
