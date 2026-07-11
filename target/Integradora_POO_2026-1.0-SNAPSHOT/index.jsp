<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Inicio"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">

    <div class="superior">
        <h2>¡Bienvenido ${sessionScope.nombreUsuario != null ? sessionScope.nombreUsuario : 'Docente'}!</h2>
        <p>Aquí puedes gestionar tus visitas académicas</p>
    </div>

    <a href="${pageContext.request.contextPath}/SolicitudDocente.jsp" class="botonnueva">Nueva Solicitud</a>

    <div class="cardcontendo">
        <h2>Solicitudes</h2>
        <div class="ubicacion">
            <i class="bi bi-pin-map" id="icono"></i>
            <span>Ciudad de México</span>
        </div>
    </div>
</main>

<%@ include file="layout/footer.jsp" %>
