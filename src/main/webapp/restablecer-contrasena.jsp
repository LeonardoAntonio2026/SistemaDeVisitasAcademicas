<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Misma plantilla visual que login.jsp (auth.css): antes esta pantalla usaba
     otra card de Bootstrap y parecía de otro sistema. --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cambiar Contraseña - Visitas Académicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="auth-body">

<div class="auth-wrapper">

    <c:if test="${not empty error}">
        <div class="auth-alert">
            <i class="bi bi-exclamation-circle"></i>
            <c:out value="${error}"/>
        </div>
    </c:if>

    <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" class="auth-logo">
    <div class="auth-subtitle">UNIVERSIDAD TECNOLÓGICA<br>EMILIANO ZAPATA DEL ESTADO DE MORELOS</div>

    <h1 class="auth-title">Cambiar contraseña</h1>

    <div class="auth-card">
        <%-- Enlace vencido, ya usado o manipulado: no tiene caso mostrar el
             formulario, porque el POST volvería a rebotar aquí sin explicación. --%>
        <c:choose>
            <c:when test="${tokenInvalido}">
                <p class="auth-instructions">
                    <i class="bi bi-exclamation-circle"></i>
                    Este enlace ya no es válido: pudo haber vencido (duran 24 horas) o ya se usó para
                    cambiar la contraseña.
                </p>
                <a href="${pageContext.request.contextPath}/olvide-contrasena" class="btn-auth">
                    Solicitar un enlace nuevo
                </a>
            </c:when>
            <c:otherwise>
                <p class="auth-instructions">Ingresa tu nueva contraseña.</p>

                <form action="${pageContext.request.contextPath}/restablecer-contrasena" method="POST">
                    <input type="hidden" name="token" value="<c:out value='${token}'/>">

                    <div class="auth-field">
                        <label for="contra1">Nueva contraseña</label>
                        <div class="auth-input-group">
                            <span class="auth-input-icon"><i class="bi bi-lock-fill"></i></span>
                            <input type="password" id="contra1" name="contra1" placeholder="contraseña nueva"
                                   required minlength="8" pattern="(?=.*[A-Za-z])(?=.*\d).{8,}"
                                   title="Mínimo 8 caracteres, con letras y números">
                        </div>
                        <small class="auth-ayuda">Al menos 8 caracteres, con letras y números.</small>
                    </div>

                    <div class="auth-field">
                        <label for="contra2">Confirmar contraseña</label>
                        <div class="auth-input-group">
                            <span class="auth-input-icon"><i class="bi bi-lock-fill"></i></span>
                            <input type="password" id="contra2" name="contra2" placeholder="confirmar contraseña"
                                   required data-igual-a="#contra1" data-mensaje="Las contraseñas no coinciden.">
                        </div>
                    </div>

                    <button type="submit" class="btn-auth">Guardar contraseña</button>
                </form>

                <script src="${pageContext.request.contextPath}/js/validacion-cuenta.js" defer></script>
            </c:otherwise>
        </c:choose>
    </div>

</div>

</body>
</html>
