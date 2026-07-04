<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Recuperar Contraseña");
    String error = request.getParameter("error");
    String enviado = request.getParameter("enviado");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="auth-body">

<div class="auth-wrapper">

    <% if (error != null) { %>
    <div class="auth-alert">
        <i class="bi bi-exclamation-circle"></i> Correo inválido
    </div>
    <% } %>

    <% if (enviado != null) { %>
    <div class="auth-alert" style="background:#e8f6ee; color:#1e7a45; border-color:#bfe6cd;">
        <i class="bi bi-check-circle"></i> Correo de recuperación enviado
    </div>
    <% } %>

    <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" class="auth-logo">
    <div class="auth-subtitle">UNIVERSIDAD TECNOLÓGICA<br>EMILIANO ZAPATA DEL ESTADO DE MORELOS</div>

    <h1 class="auth-title">Sistema de Visitas Academicas</h1>

    <div class="auth-card">
        <p class="auth-instructions">Ingresa el correo electronico vinculado a tu cuenta para recibir un correo de recuperación.</p>

        <form action="${pageContext.request.contextPath}/recuperar-contrasena" method="POST">

            <div class="auth-field">
                <label for="correo">Correo</label>
                <div class="auth-input-group">
                    <span class="auth-input-icon"><i class="bi bi-person-fill"></i></span>
                    <input type="email" id="correo" name="correo" placeholder="correo@utez.edu.mx" required>
                </div>
            </div>

            <button type="submit" class="btn-auth">Recuperar contraseña</button>

            <a href="${pageContext.request.contextPath}/Login.jsp" class="auth-link">Volver a iniciar sesión</a>
        </form>
    </div>

</div>

</body>
</html>
