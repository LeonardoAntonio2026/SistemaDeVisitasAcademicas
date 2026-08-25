<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    header.jsp — Cabecera común de todas las páginas con sesión iniciada.

    Abre el documento (<html>, <head> y el <div id="wrapper">) y lo cierra
    layout/footer.jsp. Toda página que incluya este archivo tiene que incluir
    también el footer, o el HTML queda sin cerrar.

    De qué se encarga:
      1. Calcula las banderas de rol (esRevisor, esAdministrador,
         puedeSolicitar) una sola vez, para que las vistas no repitan la
         comparación con los nombres de los roles.
      2. Carga los estilos y los scripts comunes: Bootstrap local, los tokens
         de color, loading.js y modales.js.
      3. Incluye el menú lateral (layout/sidebar.jsp) y su botón de abrir en
         celular.

    Variables que puede recibir del JSP que lo incluye:
      pageTitle  título de la pestaña (si falta, usa el nombre del sistema)
      activeNav  entrada del menú que se pinta activa (la usa sidebar.jsp)

    OJO: los archivos de /layout NO pasan por FiltroAutenticacion, porque se
    insertan con la directiva include y se resuelven al compilar el JSP. La
    sesión ya la validó el servlet que sirvió la página.

    @author Leonardo Antonio Arroyo Rodriguez
    @since 24/08/2026
--%>
<%-- Rol de la sesión, leído en un solo lugar (es el SesionUtils de las vistas).
     Antes cada página repetía la comparación con los nombres de los roles.

     El Administrador cae de los dos lados a propósito: revisa las solicitudes
     de los demás Y levanta las suyas. Por eso, para decidir qué se ve de UNA
     solicitud, estas banderas no bastan: manda si es propia (esPropia). --%>
<c:set var="esRevisor" scope="request"
       value="${sessionScope.rol == 'Estadias' || sessionScope.rol == 'Administrador'}"/>
<c:set var="esAdministrador" scope="request" value="${sessionScope.rol == 'Administrador'}"/>
<c:set var="puedeSolicitar" scope="request" value="${!esRevisor || esAdministrador}"/>
<!DOCTYPE html>
<html lang="es">
<head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${pageTitle != null ? pageTitle : 'Sistema de Gestión de Visitas'}</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modales.css">
        <script defer src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
        <script defer src="${pageContext.request.contextPath}/js/loading.js"></script>
        <%-- Va en todas las páginas: los avisos y confirmaciones del sistema
             salen en modal, nunca en los cuadros del navegador --%>
        <script defer src="${pageContext.request.contextPath}/js/modales.js"></script>
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
    <div class="d-flex align-items-center justify-content-between w-100">

        <%-- Menú (solo en celular) + logo --%>
        <div class="d-flex align-items-center gap-3">
            <button class="navbar-toggler d-md-none border-0" id="sidebarToggle" type="button" aria-label="Abrir menú">
                <span class="navbar-toggler-icon"></span>
            </button>
            <a class="navbar-brand mb-0" href="${pageContext.request.contextPath}/indexSv">
                <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png"
                     alt="Logo UTEZ" width="70" height="34" class="d-inline-block align-text-top">
                <span class="navbar-brand-texto">Sistema de Gestión de Visitas</span>
            </a>
        </div>

        <%-- Cerrar sesión --%>
        <div class="m-2">
            <a class="nav-link rounded-2 text-danger d-block" href="${pageContext.request.contextPath}/logout"
               title="Cerrar sesión">
                <i class="bi bi-box-arrow-right me-2"></i><span class="texto-cerrar-sesion">Cerrar sesión</span>
            </a>
        </div>

    </div>
</nav>

<div id="wrapper">