<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Iniciar Sesión");
    String error = request.getParameter("error");
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
        <i class="bi bi-exclamation-circle"></i>
        <%
            if ("credenciales".equals(error)) {
                out.print("Correo o contraseña incorrectos");
            } else {
                out.print("Correo inválido");
            }
        %>
    </div>
    <% } %>

    <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" class="auth-logo">
    <div class="auth-subtitle">UNIVERSIDAD TECNOLÓGICA<br>EMILIANO ZAPATA DEL ESTADO DE MORELOS</div>

    <h1 class="auth-title">Sistema de Visitas Academicas</h1>

    <div class="auth-card">
        <form action="${pageContext.request.contextPath}/login" method="POST">

            <div class="auth-field">
                <label for="correo">Correo</label>
                <div class="auth-input-group">
                    <span class="auth-input-icon"><i class="bi bi-person-fill"></i></span>
                    <input type="email" id="correo" name="correo" placeholder="correo@utez.edu.mx" required>
                </div>
            </div>

            <div class="auth-field">
                <label for="contrasena">Contraseña</label>
                <div class="auth-input-group">
                    <span class="auth-input-icon"><i class="bi bi-lock-fill"></i></span>
                    <input type="password" id="contrasena" name="contrasena" placeholder="introduce tu contraseña" required>
                    <button type="button" class="auth-toggle-eye" id="toggleContrasena" aria-label="Mostrar contraseña">
                        <i class="bi bi-eye-slash" id="toggleIcon"></i>
                    </button>
                </div>
            </div>

            <button type="submit" class="btn-auth">Iniciar Sesión</button>

            <a href="${pageContext.request.contextPath}/RecuperarContrasena.jsp" class="auth-link">Recuperar contraseña</a>
        </form>
    </div>

</div>

<script>
    document.getElementById('toggleContrasena').addEventListener('click', function () {
        var input = document.getElementById('contrasena');
        var icon = document.getElementById('toggleIcon');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        } else {
            input.type = 'password';
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        }
    });
</script>

</body>
</html>
