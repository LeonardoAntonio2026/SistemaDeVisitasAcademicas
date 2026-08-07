<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Recuperar Contraseña - Visitas Académicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
</head>
<body style="background-color: var(--color-fondo);">
<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh;">
    <div class="card border-0 shadow-sm p-4" style="max-width: 420px; width: 100%; border-radius: var(--radio-lg);">

        <div class="text-center mb-4">
            <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" height="48" class="mb-3">
            <h4 class="fw-semibold" style="color: var(--color-azul);">Recuperar contraseña</h4>
            <p class="text-muted small mb-0">Sistema de Gestión de Visitas Académicas</p>
            <p class="text-muted small mb-0">Ingresa el correo de tu cuenta y te enviaremos un enlace para crear una nueva contraseña.</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-danger py-2 small"><%= request.getAttribute("error") %></div>
        <% } %>
        <% if (request.getAttribute("mensaje") != null) { %>
        <div class="alert alert-success py-2 small"><%= request.getAttribute("mensaje") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/olvide-contrasena" method="POST">
            <div class="mb-3">
                <label class="form-label" for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo" class="form-control"
                       placeholder="docente@utez.edu.mx" required maxlength="100">
            </div>
            <button type="submit" class="btn w-100 text-white" style="background-color: var(--color-azul);">
                Enviar enlace
            </button>
        </form>

        <a href="${pageContext.request.contextPath}/login.jsp" class="text-center small mt-3 text-decoration-none"
           style="color: var(--color-azul);">Volver a iniciar sesión</a>

    </div>
</div>
</body>
</html>
