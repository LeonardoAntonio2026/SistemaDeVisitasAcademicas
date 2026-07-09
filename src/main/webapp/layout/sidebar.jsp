<aside id="sidebar" class="d-flex flex-column">
    <div class="fw-semibold text-light p-3 m-2 rounded-4"
         style="background: #183052; ">
        <div class="d-flex align-items-center gap-2">
            <div class="rounded-circle bg-white d-flex align-items-center justify-content-center flex-shrink-0"
                 style="width: 42px; height: 42px;">
                <i class="bi bi-person-fill text-dark" style="font-size: 1.4rem;"></i>
            </div>
            <div>
                <div class="mb-0" style="font-size: 0.75rem;">${sessionScope.nombreUsuario != null ? sessionScope.nombreUsuario : 'Invitado'}</div>
                <div class="text-white-50 fw-normal" style="font-size: 0.7rem;">Docente</div>
            </div>
        </div>
    </div>
    <ul class="nav nav-pills flex-column mb-auto">

        <li class="nav-item m-2 ">
            <a class="nav-link active rounded-2" href="${pageContext.request.contextPath}/">
                <i class="bi bi-house-door me-2"></i>Inicio
            </a>
        </li>
        <li class="nav-item m-2">
            <a class="nav-link rounded-2" href="${pageContext.request.contextPath}/SolicitudDocente.jsp">
                <i class="bi bi-calendar-check me-2"></i>Solicitudes
            </a>
        </li>
        <li class="nav-item m-2">
            <a class="nav-link rounded-2" href="#">
                <i class="bi bi-people me-2"></i>Reportes
            </a>
        </li>
        <li class="nav-item m-2">
            <a class="nav-link rounded-2" href="#">
                <i class="bi bi-person-badge me-2"></i>Historico
            </a>
        </li>

        <li class="nav-item m-2">
            <a class="nav-link rounded-2 text-danger" href="${pageContext.request.contextPath}/logout">
                <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
            </a>
        </li>

    </ul>
    <div class="border-top px-3 py-2 small text-muted">
        UTEZ &copy; 2026
    </div>
</aside>
<div id="sidebar-backdrop"></div>
