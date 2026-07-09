<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Inicio"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<link rel="stylesheet" href="css/layout.css">

<main class="container my-4">

    <div class="superior">
        <h2>¡Bienvenido ${sessionScope.nombreUsuario != null ? sessionScope.nombreUsuario : 'Invitado'}!</h2>
        <p>Aqui puedes gestionar tus visitas academicas</p>
    </div>

    <a href="${pageContext.request.contextPath}/SolicitudDocente.jsp" class="botonnueva">Nueva Solicitud</a>

    <div class="cardcustom">
        <h2>Solicitudes</h2>
        <div class="ubicacion">
            <i class="bi bi-pin-map" id="icono"></i>
            <span>Ciudad de México</span>
            <div class="progreso"></div>
        </div>
    </div>

</main>
</div><%-- #wrapper --%>
</body>
</html>
