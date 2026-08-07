<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cambiar Contraseña - Visitas Académicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
</head>
<body style="background-color: var(--color-fondo);">
<div class="container d-flex align-items-center justify-content-center" style="min-height: 100vh;">
    <div class="card border-0 shadow-sm p-4" style="max-width: 420px; width: 100%; border-radius: var(--radio-lg);">

        <div class="text-center mb-4">
            <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" height="48" class="mb-3">
            <h4 class="fw-semibold" style="color: var(--color-azul);">Cambiar contraseña</h4>
            <p class="text-muted small mb-0">Sistema de Gestión de Visitas Académicas</p>
        </div>

        <%-- Enlace vencido, ya usado o manipulado: no tiene caso mostrar el
             formulario, porque el POST volvería a rebotar aquí sin explicación. --%>
        <c:choose>
            <c:when test="${tokenInvalido}">
                <div class="alert alert-danger py-2 small">
                    <i class="bi bi-exclamation-circle me-1"></i>
                    Este enlace ya no es válido: pudo haber vencido (duran 24 horas) o ya se usó para
                    cambiar la contraseña.
                </div>
                <a href="${pageContext.request.contextPath}/olvide-contrasena"
                   class="btn w-100 text-white" style="background-color: var(--color-azul);">
                    Solicitar un enlace nuevo
                </a>
            </c:when>
            <c:otherwise>
                <p class="text-muted small text-center mb-3">Ingresa tu nueva contraseña.</p>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger py-2 small">${error}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/restablecer-contrasena" method="POST">
                    <input type="hidden" name="token" value="<c:out value='${token}'/>">
                    <div class="mb-3">
                        <label class="form-label" for="contra1">Nueva contraseña</label>
                        <input type="password" id="contra1" name="contra1" class="form-control" placeholder="contraseña nueva"
                               required minlength="8" pattern="(?=.*[A-Za-z])(?=.*\d).{8,}"
                               title="Mínimo 8 caracteres, con letras y números">
                        <small class="text-muted">Al menos 8 caracteres, con letras y números.</small>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="contra2">Confirmar contraseña</label>
                        <input type="password" id="contra2" name="contra2" class="form-control" placeholder="confirmar contraseña"
                               required data-igual-a="#contra1" data-mensaje="Las contraseñas no coinciden.">
                    </div>
                    <button type="submit" class="btn w-100 text-white" style="background-color: var(--color-azul);">
                        Guardar contraseña
                    </button>
                </form>

                <script src="${pageContext.request.contextPath}/js/validacion-cuenta.js" defer></script>
            </c:otherwise>
        </c:choose>

    </div>
</div>
</body>
</html>
