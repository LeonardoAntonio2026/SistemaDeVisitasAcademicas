<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- El mismo formulario sirve para crear y para editar (con ${solicitud} precargada) --%>
<% request.setAttribute("pageTitle", request.getAttribute("solicitud") != null ? "Editar solicitud" : "Nueva Solicitud"); %>
<% request.setAttribute("activeNav", "solicitudes"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css">

<main id="main-content">
    <c:set var="s" value="${solicitud}"/>
    <c:set var="editando" value="${not empty s}"/>

    <form action="solicitud" method="POST"
          <c:if test="${editando}">onsubmit="return confirm('¿Guardar los cambios? Si ya habías subido el formato FO-UTEZ-EST-08 firmado se eliminará: descarga el formato actualizado, fírmalo y súbelo de nuevo.');"</c:if>>
        <input type="hidden" name="action" value="${editando ? 'update' : 'create'}">
        <c:if test="${editando}">
            <input type="hidden" name="id" value="${s.idSolicitud}">
        </c:if>

        <div class="d-flex align-items-center gap-2 mb-4">
            <h4 class="page-title text-title-request mb-0">${editando ? 'Editar solicitud' : 'Nueva Solicitud'}</h4>
        </div>

        <c:if test="${editando}">
            <div class="alert alert-warning d-flex align-items-start gap-2" style="font-size: 14px;">
                <i class="bi bi-exclamation-triangle"></i>
                <div>Estás editando una solicitud que aún no se envía. Al guardar, el formato FO-UTEZ-EST-08 se genera con los datos nuevos, así que deberás descargarlo, firmarlo y subirlo otra vez.</div>
            </div>
        </c:if>

        <div class="form-section">
            <h6>Datos del lugar a visitar</h6>

            <div class="mb-3">
                <label class="form-label">Nombre de la empresa o actividad</label>
                <input type="text" name="nombreEmpresa" class="form-control" placeholder="ej. CISCO" required
                       value="${s.nombreEmpresaActividad}">
            </div>

            <div class="mb-3">
                <label class="form-label">Lugar o dirección</label>
                <input type="text" name="direccionLugar" class="form-control" placeholder="ej. Av. Insurgentes"
                       value="${s.lugarDireccion}">
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label">Teléfonos del contacto</label>
                    <input type="text" name="telefonoContacto" class="form-control" placeholder="ej. 7776268823"
                           value="${s.telefonoContacto}">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Correo electrónico del contacto</label>
                    <input type="email" name="correoContacto" class="form-control" placeholder="contacto@empresa.com"
                           value="${s.correoContacto}">
                </div>
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-4">
                    <label class="form-label">Fecha de inicio</label>
                    <input type="date" name="fechaInicio" class="form-control" value="${s.fechaInicio}">
                </div>
            </div>

            <div class="mb-1">
                <label class="form-label">Objetivo de la visita</label>
                <textarea name="objetivoVisita" class="form-control" rows="3" placeholder="Describe el objetivo académico de la visita">${s.objetivo}</textarea>
            </div>
        </div>

        <div class="form-section">
            <h6>Datos de los participantes de la visita</h6>

            <div class="row g-3 mb-4">
                <div class="col-md-6">
                    <label class="form-label">Área solicitante</label>
                    <input type="text" name="areaSolicitante" class="form-control" placeholder="ej. DACEA"
                           value="${s.areaSolicitante}">
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

        <div class="form-section">
            <h6>Desglose por programa educativo</h6>

            <div class="programa-header mb-1">
                <span class="form-label mb-0">Programa educativo</span>
                <span class="form-label mb-0">Cuatrimestre</span>
                <span class="form-label mb-0">Grupo</span>
                <span class="form-label mb-0">No. estudiantes</span>
                <span></span>
            </div>

            <div id="programas-container">
                <c:choose>
                    <c:when test="${editando && not empty s.programas}">
                        <c:forEach var="p" items="${s.programas}">
                            <div class="programa-row">
                                <input type="text" name="programaEducativo" class="form-control" placeholder="Ejemplo" value="${p.divisionAcademica}">
                                <input type="number" name="cuatrimestre" class="form-control" placeholder="5" min="1" max="11" value="${p.cuatrimestre}">
                                <input type="text" name="grupo" class="form-control" placeholder="A" value="${p.grupo}">
                                <input type="number" name="numEstudiantesGrupo" class="form-control" placeholder="4" min="0" value="${p.noEstudiantes}">
                                <button type="button" class="btn-delete-row" title="Eliminar fila">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                                        <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/>
                                    </svg>
                                </button>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="programa-row">
                            <input type="text" name="programaEducativo" class="form-control" placeholder="Ejemplo">
                            <input type="number" name="cuatrimestre" class="form-control" placeholder="5" min="1" max="11">
                            <input type="text" name="grupo" class="form-control" placeholder="A">
                            <input type="number" name="numEstudiantesGrupo" class="form-control" placeholder="4" min="0">
                            <button type="button" class="btn-delete-row" title="Eliminar fila">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                    <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                                    <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/>
                                </svg>
                            </button>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <button type="button" class="btn-agregar mt-3" id="btn-agregar-grupo">+ Agregar grupo</button>
        </div>

        <div class="form-section">
            <h6>Asignaturas que se reforzarán con la visita</h6>

            <div class="mb-1">
                <label class="form-label">Asignaturas</label>
                <div class="tags-input-wrapper" id="tags-wrapper">
                    <%-- En edición se pintan los chips igual que los arma solicitud-form.js --%>
                    <c:forEach var="a" items="${s.asignaturas}">
                        <span class="tag-chip">${a} <button type="button" class="tag-remove" aria-label="Quitar">&times;</button><input
                                type="hidden" name="asignaturas" value="${a}"></span>
                    </c:forEach>
                    <input type="text" class="tags-input" id="tags-input" placeholder="Escribe y presiona Enter">
                </div>
            </div>
        </div>

        <div class="acciones-form">
            <c:set var="volverUrl">${pageContext.request.contextPath}/solicitud</c:set>
            <c:if test="${editando}">
                <c:set var="volverUrl">${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}</c:set>
            </c:if>
            <a href="${volverUrl}" class="btn-volver text-decoration-none">
                <i class="bi bi-arrow-left"></i> Volver
            </a>
            <button type="submit" class="btncrear">
                <i class="bi ${editando ? 'bi-check-lg' : 'bi-send'}"></i> ${editando ? 'Guardar cambios' : 'Crear Solicitud'}
            </button>
        </div>

    </form>
</main>

<script src="${pageContext.request.contextPath}/js/solicitud-form.js"></script>
<%@ include file="layout/footer.jsp" %>
