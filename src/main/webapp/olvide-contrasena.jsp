<%--
  Vista de Recuperar Contraseña.
  Renderiza el formulario donde el usuario captura su correo para recibir el
  enlace de restablecimiento. La procesa OlvideContrasenaServlet.
  Muestra alertas de error o éxito según lo que deje el servlet.

  @author Hugo Alberto Ramirez Martinez
  @since 25/08/2026
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Misma plantilla visual que login.jsp (auth.css): antes esta pantalla usaba
     otra card de Bootstrap y parecía de otro sistema. --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Recuperar Contraseña - Visitas Académicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap-icons.min.css">
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

    <c:if test="${not empty mensaje}">
        <div class="auth-alert auth-alert-exito">
            <i class="bi bi-check-circle"></i>
            <c:out value="${mensaje}"/>
        </div>
    </c:if>

    <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" class="auth-logo">
    <div class="auth-subtitle">UNIVERSIDAD TECNOLÓGICA<br>EMILIANO ZAPATA DEL ESTADO DE MORELOS</div>

    <h1 class="auth-title">Recuperar contraseña</h1>

    <div class="auth-card">
        <p class="auth-instructions">Ingresa el correo de tu cuenta y te enviaremos un enlace para crear una nueva contraseña.</p>

        <form action="${pageContext.request.contextPath}/olvide-contrasena" method="POST">

            <div class="auth-field">
                <label for="correo">Correo</label>
                <div class="auth-input-group">
                    <span class="auth-input-icon"><i class="bi bi-envelope-fill"></i></span>
                    <input type="email" id="correo" name="correo" placeholder="correo@utez.edu.mx" required maxlength="100">
                </div>
            </div>

            <button type="submit" class="btn-auth">Enviar enlace</button>

            <a href="${pageContext.request.contextPath}/login.jsp" class="auth-link">Volver a iniciar sesión</a>
        </form>
    </div>

</div>

</body>
</html>
