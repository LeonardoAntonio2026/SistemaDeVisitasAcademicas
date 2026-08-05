<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cambiar Contraseña - Visitas Académicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body style="background-color: #F2F2F7;">
<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh;">
    <div class="card border-0 shadow-sm p-4" style="max-width: 420px; width: 100%; border-radius: 1rem;">

        <div class="text-center mb-4">
            <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" height="48" class="mb-3">
            <h4 class="fw-semibold" style="color: #183052;">Cambiar Contraseña</h4>
            <p class="text-muted small mb-0">Sistema de Gestión de Visitas Académicas</p>
            <p class="text-muted small mb-0">Ingresa la nueva contraseña </p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-danger py-2 small"><%= request.getAttribute("error") %></div>
        <% } %>
        <% if (request.getAttribute("mensaje") != null) { %>
        <div class="alert alert-success py-2 small"><%= request.getAttribute("mensaje") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/restablecer-contrasena" method="POST">
            <input type="hidden" name="token" value="<%= request.getAttribute("token") %>">
            <div class="mb-3">
                <label class="form-label" for="contra1">Nueva Contraseña</label>
                <input type="password" id="contra1" name="contra1" class="form-control" placeholder="contraseña nueva"
                       required minlength="8" pattern="(?=.*[A-Za-z])(?=.*\d).{8,}"
                       title="Mínimo 8 caracteres, con letras y números">
                <small class="text-muted">Al menos 8 caracteres, con letras y números.</small>
            </div>
            <div class="mb-3">
                <label class="form-label" for="contra2">Confirmar Contraseña</label>
                <input type="password" id="contra2" name="contra2" class="form-control" placeholder="confirmar contraseña"
                       required data-igual-a="#contra1" data-mensaje="Las contraseñas no coinciden.">
            </div>
            <button type="submit" class="btn w-100 text-white" style="background-color: #183052;">
                Enviar
            </button>
        </form>

        <script src="${pageContext.request.contextPath}/js/validacion-cuenta.js" defer></script>

    </div>
</div>
</body>
</html>
