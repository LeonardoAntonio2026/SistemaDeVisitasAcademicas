<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${pageTitle != null ? pageTitle : 'Sistema de Gestión de Visitas'}</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
        <script defer src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
        <script defer src="${pageContext.request.contextPath}/js/loading.js"></script>
    <script defer>
        document.addEventListener('DOMContentLoaded', function () {
            var toggle = document.getElementById('sidebarToggle');
            var sidebar = document.getElementById('sidebar');
            var backdrop = document.getElementById('sidebar-backdrop');
            if (toggle && sidebar && backdrop) {
                function showSidebar() {
                    sidebar.classList.add('show');
                    backdrop.classList.add('show');
                    document.body.style.overflow = 'hidden';
                }
                function hideSidebar() {
                    sidebar.classList.remove('show');
                    backdrop.classList.remove('show');
                    document.body.style.overflow = '';
                }
                toggle.addEventListener('click', function () {
                    if (sidebar.classList.contains('show')) {
                        hideSidebar();
                    } else {
                        showSidebar();
                    }
                });
                backdrop.addEventListener('click', hideSidebar);
                document.addEventListener('keydown', function (e) {
                    if (e.key === 'Escape' && sidebar.classList.contains('show')) {
                        hideSidebar();
                    }
                });
            }
        });
    </script>
</head>
<body style="background-color: var(--color-fondo)">

<div id="barra-carga"></div>

<nav class="navbar px-3" style="background: var(--color-superficie); margin: 1rem 1rem 0; border-radius: var(--radio-lg); box-shadow: var(--sombra-card); border: none;">
    <!-- Se añade w-100 y justify-content-between -->
    <div class="d-flex align-items-center justify-content-between w-100">

        <!-- Bloque Izquierdo: Menú + Logo agrupados con gap-3 -->
        <div class="d-flex align-items-center gap-3">
            <button class="navbar-toggler d-md-none border-0" id="sidebarToggle" type="button" aria-label="Abrir menú">
                <span class="navbar-toggler-icon"></span>
            </button>
            <a class="navbar-brand mb-0" href="${pageContext.request.contextPath}/indexSv">
                <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png"
                     alt="Logo UTEZ" width="70" height="34" class="d-inline-block align-text-top">
                Sistema de Gestión de Visitas
            </a>
        </div>

        <!-- Bloque Derecho: Cerrar Sesión -->
        <div class="m-2">
            <a class="nav-link rounded-2 text-danger d-block" href="${pageContext.request.contextPath}/logout">
                <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
            </a>
        </div>

    </div>
</nav>

<div id="wrapper">