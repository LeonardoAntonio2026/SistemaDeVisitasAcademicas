<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    sidebar.jsp — Menú lateral de navegación, incluido por layout/header.jsp.

    Se compone de dos partes:
      1. La tarjeta de identidad: nombre y rol del usuario en sesión. Si no
         hay sesión muestra "Invitado / Docente" en vez de quedar en blanco.
      2. Las entradas del menú: Solicitudes, Reportes e Histórico para todos,
         más Gestión de usuarios solo para el Administrador.

    Espera del JSP que lo incluye la variable "activeNav", que marca cuál
    entrada se pinta como activa. Los valores que reconoce son:
    inicio, solicitudes, reportes, historico y usuarios ("inicio" y
    "solicitudes" iluminan la misma entrada).

    El div #sidebar-backdrop del final es la capa oscura que tapa el
    contenido cuando el menú se abre en celular; lo controla el script
    de header.jsp.

    @author Leonardo Antonio Arroyo Rodriguez
    @since 24/08/2026
--%>
<aside id="sidebar" class="d-flex flex-column">
    <div class="fw-semibold text-light p-3 m-2 rounded-4"
         style="background: var(--color-azul);">
        <div class="d-flex align-items-center gap-2">
            <div class="rounded-circle bg-white d-flex align-items-center justify-content-center flex-shrink-0"
                 style="width: 42px; height: 42px;">
                <i class="bi bi-person-fill text-dark" style="font-size: 1.4rem;"></i>
            </div>
            <div>
                <div class="mb-0" style="font-size: 0.75rem;"><c:out value="${sessionScope.nombreUsuario != null ? sessionScope.nombreUsuario : 'Invitado'}"/></div>
                <div class="text-white-50 fw-normal" style="font-size: 0.7rem;"><c:out value="${sessionScope.rol != null ? sessionScope.rol : 'Docente'}"/></div>
            </div>
        </div>
    </div>
    <ul class="nav nav-pills flex-column mb-auto">

        <li class="nav-item m-2 ">
            <a class="nav-link rounded-2 ${activeNav == 'inicio' || activeNav == 'solicitudes' ? 'active' : ''}" href="${pageContext.request.contextPath}/indexSv">
                <i class="bi bi-house-door me-2"></i>Solicitudes
            </a>
        </li>

        <li class="nav-item m-2">
            <a class="nav-link rounded-2 ${activeNav == 'reportes' ? 'active' : ''}" href="${pageContext.request.contextPath}/reportes">
                <i class="bi bi-file-earmark-text me-2"></i>Reportes
            </a>
        </li>
        <li class="nav-item m-2">
            <a class="nav-link rounded-2 ${activeNav == 'historico' ? 'active' : ''}" href="${pageContext.request.contextPath}/historico">
                <i class="bi bi-clock-history me-2"></i>Histórico
            </a>
        </li>
        <%-- Gestión de usuarios: solo el Administrador. El rol se lee de la
             sesión (lo guarda LoginServlet), nunca de la URL. --%>
        <c:if test="${sessionScope.rol == 'Administrador'}">
            <li class="nav-item m-2">
                <a class="nav-link rounded-2 ${activeNav == 'usuarios' ? 'active' : ''}" href="${pageContext.request.contextPath}/usuarios">
                    <i class="bi bi-people me-2"></i>Gestión de usuarios
                </a>
            </li>
        </c:if>

    </ul>



</aside>
<div id="sidebar-backdrop"></div>
