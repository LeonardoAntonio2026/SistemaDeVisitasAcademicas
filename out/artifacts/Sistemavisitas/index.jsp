<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("pageTitle", "Inicio"); %>
<% request.setAttribute("activeNav", "inicio"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stepper.css">

    <div class="superior">
        <h2>¡Bienvenido ${sessionScope.nombreUsuario != null ? sessionScope.nombreUsuario : 'Docente'}!</h2>
        <p>Aquí puedes gestionar tus visitas académicas</p>
    </div>

    <c:if test="${sessionScope.rol == null || sessionScope.rol == 'Docente'}">
        <a href="${pageContext.request.contextPath}/solicitud?action=nueva" class="botonnueva">Nueva Solicitud</a>
    </c:if>

    <h2 class="titulo-solicitudes">Solicitudes recientes</h2>

    <%@ include file="layout/lista-solicitudes.jsp" %>
</main>

<%@ include file="layout/footer.jsp" %>
