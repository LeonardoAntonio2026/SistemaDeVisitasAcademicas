<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Crear cuenta - Visitas Académicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
</head>
<body style="background-color: var(--color-fondo);">
<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh; padding: 2rem 0;">
    <div class="card border-0 shadow-sm p-4" style="max-width: 480px; width: 100%; border-radius: var(--radio-lg);">

        <div class="text-center mb-4">
            <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" height="48" class="mb-3">
            <h4 class="fw-semibold" style="color: var(--color-azul);">Crear cuenta</h4>
            <p class="text-muted small mb-0">Sistema de Gestión de Visitas Académicas</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger py-2 small"><%= request.getAttribute("error") %></div>
        <% } %>

        <%-- Los value vienen de param: al volver con error el usuario no reescribe todo --%>
        <form action="${pageContext.request.contextPath}/register" method="POST">
            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label" for="nombre">Nombre(s)</label>
                    <input type="text" id="nombre" name="nombre" class="form-control" required maxlength="50"
                           value="<c:out value="${param.nombre}"/>">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="apellidos">Apellidos</label>
                    <input type="text" id="apellidos" name="apellidos" class="form-control" maxlength="50"
                           value="<c:out value="${param.apellidos}"/>">
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="correo1">Correo electrónico</label>
                <input type="email" id="correo1" name="correo1" class="form-control" placeholder="docente@utez.edu.mx"
                       required maxlength="100" value="<c:out value="${param.correo1}"/>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="correo2">Confirmar correo</label>
                <input type="email" id="correo2" name="correo2" class="form-control" required maxlength="100"
                       data-igual-a="#correo1" data-mensaje="Los correos no coinciden."
                       value="<c:out value="${param.correo2}"/>">
            </div>
            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label" for="contra1">Contraseña</label>
                    <input type="password" id="contra1" name="contra1" class="form-control" required
                           minlength="8" pattern="(?=.*[A-Za-z])(?=.*\d).{8,}"
                           title="Mínimo 8 caracteres, con letras y números">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="contra2">Confirmar contraseña</label>
                    <input type="password" id="contra2" name="contra2" class="form-control" required
                           data-igual-a="#contra1" data-mensaje="Las contraseñas no coinciden.">
                </div>
            </div>
            <p class="text-muted small">La contraseña debe tener al menos 8 caracteres e incluir letras y números.</p>
            <button type="submit" class="btn w-100 text-white" style="background-color: var(--color-azul);">
                Registrarme
            </button>
        </form>

        <script src="${pageContext.request.contextPath}/js/validacion-cuenta.js" defer></script>

        <p class="text-center small mt-3 mb-0">
            ¿Ya tienes cuenta?
            <a href="${pageContext.request.contextPath}/login.jsp" style="color: var(--color-azul);">Inicia sesión</a>
        </p>
    </div>
</div>
</body>
</html>
