<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Inicio"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<link rel="stylesheet" href="css/form.css">
<link rel="stylesheet" href="css/card.css">
<link rel="stylesheet" href="css/layout.css">

<main class="form-content my-4">

    <h4 class="page-title text-title-request mb-1">
        ¡Bienvenido ${sessionScope.nombreUsuario != null ? sessionScope.nombreUsuario : 'Invitado'}!
    </h4>
    <p class="text-muted mb-4">Aquí puedes gestionar tus visitas académicas</p>

    <div class="card-custom">
        <h6 class="card-title">Solicitudes</h6>

        <div class="d-flex align-items-center gap-2 mb-3 text-muted">
            <i class="bi bi-pin-map" style="font-size: 1.1rem;"></i>
            <span>Ciudad de México</span>
        </div>

        <div class="contenedor-boton">
            <a href="${pageContext.request.contextPath}/SolicitudDocente.jsp" class="btncrear text-decoration-none">
                <i class="bi bi-plus-lg"></i> Nueva Solicitud
            </a>
        </div>
    </div>

</main>
</div><%-- #wrapper --%>
</body>
</html>
