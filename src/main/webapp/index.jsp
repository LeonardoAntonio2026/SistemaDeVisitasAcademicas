<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- La raíz "/" sirve este JSP directo (welcome-file), sin pasar por IndexSv:
     la lista llega en null y la página mentía "No hay solicitudes por ahora".
     Si nadie cargó la lista, se pasa por el servlet, que sí la trae. --%>
<c:if test="${listaSolicitudes == null}">
    <c:redirect url="/indexSv"/>
</c:if>
<% request.setAttribute("pageTitle", "Inicio"); %>
<% request.setAttribute("activeNav", "inicio"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stepper.css">

    <div class="superior">
        <h2>Solicitudes</h2>
        <p>
            <c:choose>
                <c:when test="${!esRevisor}">Consulta el estado de tus solicitudes de visita</c:when>
                <c:when test="${puedeSolicitar}">Solicitudes por revisar de los docentes, junto con las tuyas</c:when>
                <c:otherwise>Solicitudes activas y pendientes por revisar enviadas por los docentes</c:otherwise>
            </c:choose>
        </p>
    </div>

    <c:if test="${puedeSolicitar}">
        <a href="${pageContext.request.contextPath}/solicitud?action=nueva" class="botonnueva">Nueva Solicitud</a>
    </c:if>

    <h2 class="titulo-solicitudes">Solicitudes recientes</h2>

    <%@ include file="layout/lista-solicitudes.jsp" %>
</main>

<%@ include file="layout/footer.jsp" %>
