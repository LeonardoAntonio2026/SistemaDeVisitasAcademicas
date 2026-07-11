<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Crear cuenta - Visitas Académicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body style="background-color: #F2F2F7;">
<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh; padding: 2rem 0;">
    <div class="card border-0 shadow-sm p-4" style="max-width: 480px; width: 100%; border-radius: 1rem;">

        <div class="text-center mb-4">
            <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" height="48" class="mb-3">
            <h4 class="fw-semibold" style="color: #183052;">Crear cuenta</h4>
            <p class="text-muted small mb-0">Sistema de Gestión de Visitas Académicas</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger py-2 small"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/register" method="POST">
            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label">Nombre(s)</label>
                    <input type="text" name="nombre" class="form-control" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Apellidos</label>
                    <input type="text" name="apellidos" class="form-control">
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Correo electrónico</label>
                <input type="email" name="correo1" class="form-control" placeholder="docente@utez.edu.mx" required>
            </div>
            <div class="mb-3">
                <label class="form-label">Confirmar correo</label>
                <input type="email" name="correo2" class="form-control" required>
            </div>
            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label">Contraseña</label>
                    <input type="password" name="contra1" class="form-control" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Confirmar contraseña</label>
                    <input type="password" name="contra2" class="form-control" required>
                </div>
            </div>
            <button type="submit" class="btn w-100 text-white" style="background-color: #183052;">
                Registrarme
            </button>
        </form>

        <p class="text-center small mt-3 mb-0">
            ¿Ya tienes cuenta?
            <a href="${pageContext.request.contextPath}/login.jsp" style="color: #183052;">Inicia sesión</a>
        </p>
    </div>
</div>
</body>
</html>
