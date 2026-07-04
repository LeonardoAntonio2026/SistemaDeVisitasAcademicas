<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle != null ? pageTitle : 'Sistema de Gestión de Visitas'}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <script defer src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
</head>
<body class="bg-light">
<div style="width: 100%; padding: 0 20px;">

    <div class="superior">
        <h2>!Bienvenido Leonardo Antonio!</h2>
        <p>Aqui puedes gestionar tus visitas academicas</p>
    </div>

    <button type="button" class="botonnueva">Nueva Solicitud</button>

    <div class="cardcontendo">
        <h2>Solicitudes</h2>
        <div class="ubicacion">
            <i class="bi bi-pin-map" id="icono"></i>
            <span>Ciudad de México</span>
            <div class="progreso"></div>
        </div>
    </div>
</div>
</div>
</body>
<div id="wrapper"></div>