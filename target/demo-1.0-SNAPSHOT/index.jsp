<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Visitas"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<link rel="stylesheet" href="css/form.css">
<link rel="stylesheet" href="css/card.css">

<main class="container my-4">

    <h4 class="page-title text-title-request mb-4">Nueva Solicitud</h4>

    <%-- ── Card 1: Datos del lugar ── --%>
    <div class="card-custom">
        <h6 class="card-title">Datos del lugar a visitar</h6>

        <div class="mb-3">
            <label class="form-label">Nombre de la empresa o actividad</label>
            <input type="text" class="form-control" placeholder="ej. CISCO">
        </div>

        <div class="mb-3">
            <label class="form-label">Lugar o dirección</label>
            <input type="text" class="form-control" placeholder="ej. Av. Insurgentes">
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label class="form-label">Teléfonos del contacto</label>
                <input type="text" class="form-control" placeholder="ej. 7776268823">
            </div>
            <div class="col-md-6">
                <label class="form-label">Correo electrónico del contacto</label>
                <input type="email" class="form-control" placeholder="contacto@empresa.com">
            </div>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-4">
                <label class="form-label">Fecha de inicio</label>
                <input type="date" class="form-control" placeholder="ejemplo">
            </div>
            <div class="col-md-4">
                <label class="form-label">Fecha de término</label>
                <input type="date" class="form-control" placeholder="ejemplo">
            </div>
            <div class="col-md-4">
                <label class="form-label">Hora de la visita</label>
                <input type="time" class="form-control" placeholder="ejemplo">
            </div>
        </div>

        <div class="mb-1">
            <label class="form-label">Objetivo de la visita</label>
            <textarea class="form-control" rows="3" placeholder="Describe el objetivo académico de la visita"></textarea>
        </div>
    </div>

    <%-- ── Card 2: Participantes ── --%>
    <div class="card-custom">
        <h6 class="card-title">Datos de los participantes de la visita</h6>

        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label class="form-label">Área solicitante</label>
                <input type="text" class="form-control" placeholder="ej. 7776268823">
            </div>
            <div class="col-md-6">
                <label class="form-label">Docente responsable de la visita</label>
                <input type="text" class="form-control" placeholder="Nombre del docente">
            </div>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-6">
                <label class="form-label">Celular del docente <span class="text-danger">*</span></label>
                <input type="text" class="form-control" placeholder="ej. 7776268823">
            </div>
            <div class="col-md-6">
                <label class="form-label">Docentes acompañantes</label>
                <input type="text" class="form-control" placeholder="Nombres separados por ,">
            </div>
        </div>

        <div class="mb-1">
            <label class="form-label d-block mb-2">Número de estudiantes participantes por división académica</label>
            <div class="division-table">
                <div class="division-header">
                    <span>DACEA</span>
                    <span>DATEFI</span>
                    <span>DATID</span>
                    <span>DAMI</span>
                    <span>Total</span>
                </div>
                <div class="division-inputs">
                    <input type="number" class="form-control" value="0" min="0">
                    <input type="number" class="form-control" value="0" min="0">
                    <input type="number" class="form-control" value="0" min="0">
                    <input type="number" class="form-control" value="0" min="0">
                    <input type="number" class="form-control division-total" value="0" readonly tabindex="-1">
                </div>
            </div>
        </div>
    </div>

    <%-- ── Card 3: Desglose por programa educativo ── --%>
    <div class="card-custom">
        <h6 class="card-title">Desglose por programa educativo</h6>

        <div class="programa-header mb-1">
            <span class="form-label mb-0">Programa educativo</span>
            <span class="form-label mb-0">Cuatrimestre</span>
            <span class="form-label mb-0">Grupo</span>
            <span class="form-label mb-0">No. estudiantes</span>
            <span></span>
        </div>

        <div id="programas-container">
            <div class="programa-row">
                <input type="text" class="form-control" placeholder="Ejemplo">
                <input type="text" class="form-control" placeholder="5">
                <input type="text" class="form-control" placeholder="A">
                <input type="number" class="form-control" placeholder="4" min="0">
                <button type="button" class="btn-delete-row" title="Eliminar fila">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                        <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/>
                    </svg>
                </button>
            </div>
            <div class="programa-row">
                <input type="text" class="form-control" placeholder="Ejemplo">
                <input type="text" class="form-control" placeholder="5">
                <input type="text" class="form-control" placeholder="A">
                <input type="number" class="form-control" placeholder="4" min="0">
                <button type="button" class="btn-delete-row" title="Eliminar fila">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                        <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/>
                    </svg>
                </button>
            </div>
            <div class="programa-row">
                <input type="text" class="form-control" placeholder="Ejemplo">
                <input type="text" class="form-control" placeholder="5">
                <input type="text" class="form-control" placeholder="A">
                <input type="number" class="form-control" placeholder="4" min="0">
                <button type="button" class="btn-delete-row" title="Eliminar fila">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                        <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/>
                    </svg>
                </button>
            </div>
        </div>

        <button type="button" class="btn-add-row mt-3" id="btn-agregar-grupo">+ Agregar grupo</button>
    </div>

    <%-- ── Card 4: Asignaturas ── --%>
    <div class="card-custom">
        <h6 class="card-title">Asignaturas que se reforzaran con la visita</h6>

        <div class="mb-1">
            <label class="form-label">Asignaturas</label>
            <div class="tags-input-wrapper" id="tags-wrapper">
                <span class="tag-chip">Fundamentos de Redes, Programación orientada a Objetos <button type="button" class="tag-remove" aria-label="Quitar">&times;</button></span>
                <input type="text" class="tags-input" id="tags-input" placeholder="">
            </div>
        </div>
    </div>

</main>
<script defer src="js/visitas-form.js"></script>

</div><%-- #wrapper --%>
</body>
</html>

