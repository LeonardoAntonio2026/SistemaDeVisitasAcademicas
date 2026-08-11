<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- El mismo formulario sirve para crear y para editar (con ${solicitud} precargada).
     "editando" lo decide el servlet: al volver con errores de validación también
     viene ${solicitud} cargada, pero eso NO significa que se esté editando. --%>
<% request.setAttribute("pageTitle", Boolean.TRUE.equals(request.getAttribute("editando")) ? "Editar solicitud" : "Nueva Solicitud"); %>
<% request.setAttribute("activeNav", "solicitudes"); %>
<%-- El catálogo de divisiones y programas educativos vive en el modelo:
     así el docente elige de una lista en vez de escribir a mano --%>
<% request.setAttribute("divisiones", com.example.demo.model.CatalogoAcademico.DIVISIONES); %>
<% request.setAttribute("nombresDivision", com.example.demo.model.CatalogoAcademico.getNombres()); %>
<% request.setAttribute("programasPorDivision", com.example.demo.model.CatalogoAcademico.getProgramas()); %>
<%-- Tope inferior del selector de fecha: no se agenda una visita en el pasado --%>
<% request.setAttribute("hoy", java.time.LocalDate.now().toString()); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css">

<main id="main-content">
    <c:set var="s" value="${solicitud}"/>

    <%-- autocomplete="off": el autorrelleno del navegador escoge por su cuenta
         los <select> del desglose (división, programa, cuatrimestre, grupo) y
         deja capturados grupos que el docente nunca eligió --%>
    <form action="solicitud" method="POST" id="form-solicitud" autocomplete="off"
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

        <%-- Errores que devolvió la validación del servidor (RNF-07) --%>
        <c:if test="${not empty errores}">
            <div class="form-errores" id="form-errores">
                <div class="form-errores-titulo">
                    <i class="bi bi-exclamation-triangle"></i> Revisa los siguientes datos antes de continuar
                </div>
                <ul>
                    <c:forEach var="e" items="${errores}">
                        <li><c:out value="${e}"/></li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <div class="form-section">
            <h6>Datos del lugar a visitar</h6>

            <div class="mb-3">
                <label class="form-label">Nombre de la empresa o actividad</label>
                <input type="text" name="nombreEmpresa" class="form-control" placeholder="ej. CISCO" required
                       maxlength="150" value="<c:out value="${s.nombreEmpresaActividad}"/>">
            </div>

            <div class="mb-3">
                <label class="form-label">Lugar o dirección</label>
                <input type="text" name="direccionLugar" class="form-control" placeholder="ej. Av. Insurgentes" required
                       maxlength="200" value="<c:out value="${s.lugarDireccion}"/>">
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label">Teléfonos del contacto</label>
                    <input type="tel" name="telefonoContacto" class="form-control" placeholder="ej. 7776268823" required
                           pattern="[0-9]{10}" inputmode="numeric" maxlength="10"
                           title="10 dígitos, sin espacios ni guiones"
                           value="<c:out value="${s.telefonoContacto}"/>">
                    <small class="form-ayuda">10 dígitos, sin espacios ni guiones.</small>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Correo electrónico del contacto</label>
                    <input type="email" name="correoContacto" class="form-control" placeholder="contacto@empresa.com" required
                           maxlength="100" value="<c:out value="${s.correoContacto}"/>">
                </div>
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-4">
                    <label class="form-label">Fecha de inicio</label>
                    <input type="date" name="fechaInicio" class="form-control" value="${s.fechaInicio}" required
                           min="${hoy}" title="La visita no puede agendarse en una fecha pasada">
                </div>
            </div>

            <div class="mb-1">
                <label class="form-label">Objetivo de la visita</label>
                <textarea name="objetivoVisita" class="form-control" rows="3" required maxlength="500"
                          placeholder="Describe el objetivo académico de la visita"><c:out value="${s.objetivo}"/></textarea>
            </div>
        </div>

        <div class="form-section">
            <h6>Datos de los participantes de la visita</h6>

            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label">Área solicitante</label>
                    <input type="text" name="areaSolicitante" class="form-control" required placeholder="ej. DACEA"
                           maxlength="100" value="<c:out value="${s.areaSolicitante}"/>">
                </div>
            </div>

            <%-- El responsable se precarga con el docente en sesión, pero se puede cambiar --%>
            <div class="row g-3 mb-3">
                <div class="col-md-8">
                    <label class="form-label">Docente responsable de la visita</label>
                    <input type="text" name="docenteResponsable" class="form-control" required maxlength="150"
                           placeholder="Nombre completo del docente"
                           value="<c:out value="${empty s ? sessionScope.nombreUsuario : s.docenteResponsable}"/>">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Celular</label>
                    <input type="tel" name="celularResponsable" required class="form-control" placeholder="ej. 7771234567"
                           pattern="[0-9]{10}" inputmode="numeric" maxlength="10"
                           title="10 dígitos, sin espacios ni guiones"
                           value="<c:out value="${s.celularResponsable}"/>">
                </div>
            </div>

            <div class="mb-4">
                <label class="form-label">Docentes acompañantes</label>
                <div class="tags-input-wrapper" id="acompanantes-wrapper">
                    <%-- En edición se pintan los chips igual que los arma solicitud-form.js --%>
                    <c:forEach var="d" items="${s.docentesAcompanantes}">
                        <span class="tag-chip">${d.nombre} <button type="button" class="tag-remove" aria-label="Quitar">&times;</button><input
                                type="hidden" name="docentesAcompanantes" value="${d.id}"></span>
                    </c:forEach>
                    <input type="text" class="tags-input" id="acompanantes-input" autocomplete="off"
                           placeholder="Escribe el nombre del docente y presiona Enter">
                </div>
                <div class="autocomplete-lista" id="acompanantes-sugerencias"></div>
                <small class="form-ayuda">Solo aparecen docentes registrados en el sistema.</small>
            </div>

            <%-- ===== Grupos que participan =====
                 Todo se elige de listas: la división filtra los programas y los
                 grupos que ya se usaron se van descartando. El desglose por
                 división de abajo se calcula solo, ya no se captura. --%>
            <label class="form-label d-block mb-1">Grupos que participan en la visita</label>
            <small class="form-ayuda mb-2">Agrega una fila por cada grupo. Al elegir la división académica se muestran sus programas educativos.</small>

            <div class="programa-tabla">
                <div class="programa-header">
                    <span>División académica</span>
                    <span>Programa educativo</span>
                    <span>Cuatrimestre</span>
                    <span>Grupo</span>
                    <span>Estudiantes</span>
                    <span></span>
                </div>

                <%-- Las filas las arma solicitud-form.js con la plantilla de abajo:
                     así el marcado de una fila vive en un solo lugar --%>
                <div id="programas-container"></div>
            </div>

            <div id="programas-msg" class="form-mismatch-msg" style="display:none;">
                <i class="bi bi-exclamation-triangle"></i>
                <span id="programas-msg-text"></span>
            </div>

            <button type="button" class="btn-agregar mt-3" id="btn-agregar-grupo">
                <i class="bi bi-plus-lg"></i> Agregar grupo
            </button>

            <%-- ===== Desglose por división académica (calculado) ===== --%>
            <label class="form-label d-block mb-1 mt-4">Número de estudiantes participantes por división académica</label>
            <div class="tabla-scroll" id="division-resumen">
                <div class="division-table">
                    <div class="division-header">
                        <c:forEach var="division" items="${divisiones}">
                            <span title="${nombresDivision[division]}">${division}</span>
                        </c:forEach>
                        <span>Total</span>
                    </div>
                    <div class="division-valores">
                        <c:forEach var="division" items="${divisiones}">
                            <span data-division="${division}">0</span>
                        </c:forEach>
                        <span data-division-total>0</span>
                    </div>
                </div>
            </div>
            <small class="form-ayuda">Se calcula automáticamente con los grupos capturados. Es el desglose que se imprime en el FO-UTEZ-EST-08.</small>
        </div>

        <%-- ===== Datos para solicitud-form.js (no se ven) ===== --%>

        <%-- Catálogo de programas educativos por división --%>
        <div id="catalogo-programas" hidden>
            <c:forEach var="entrada" items="${programasPorDivision}">
                <c:forEach var="programa" items="${entrada.value}">
                    <span data-division="${entrada.key}" data-programa="<c:out value="${programa}"/>"></span>
                </c:forEach>
            </c:forEach>
        </div>

        <%-- Grupos ya capturados: en edición vienen de la BD y al volver con
             errores de validación vienen de lo que se acababa de escribir --%>
        <div id="programas-iniciales" hidden>
            <c:forEach var="p" items="${s.programas}">
                <span data-programa="<c:out value="${p.programa}"/>"
                      data-cuatrimestre="${p.cuatrimestre}"
                      data-grupo="<c:out value="${p.grupo}"/>"
                      data-estudiantes="${p.noEstudiantes}"></span>
            </c:forEach>
        </div>

        <%-- Plantilla de una fila del desglose --%>
        <template id="tpl-programa-row">
            <div class="programa-row">
                <div class="programa-campo" data-etiqueta="División académica">
                    <select class="form-control campo-division" required aria-label="División académica">
                        <option value="">Elige una división…</option>
                        <c:forEach var="division" items="${divisiones}">
                            <option value="${division}" title="${nombresDivision[division]}">${division}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="programa-campo" data-etiqueta="Programa educativo">
                    <select class="form-control campo-programa" name="programaEducativo" required
                            aria-label="Programa educativo">
                        <option value="">Elige primero la división</option>
                    </select>
                </div>
                <div class="programa-campo" data-etiqueta="Cuatrimestre">
                    <select class="form-control campo-cuatrimestre" name="cuatrimestre" required
                            aria-label="Cuatrimestre">
                        <option value="">—</option>
                        <c:forEach var="n" begin="1" end="11">
                            <option value="${n}">${n}°</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="programa-campo" data-etiqueta="Grupo">
                    <select class="form-control campo-grupo" name="grupo" required aria-label="Grupo">
                        <option value="">—</option>
                        <c:forTokens items="A,B,C,D,E,F,G,H,I,J" delims="," var="letra">
                            <option value="${letra}">${letra}</option>
                        </c:forTokens>
                    </select>
                </div>
                <div class="programa-campo" data-etiqueta="Estudiantes">
                    <input type="number" name="numEstudiantesGrupo" class="form-control campo-estudiantes"
                           min="1" max="999" step="1" required placeholder="0" aria-label="Número de estudiantes">
                </div>
                <div class="programa-campo programa-campo--accion">
                    <button type="button" class="btn-delete-row" title="Quitar este grupo" aria-label="Quitar este grupo">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                            <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
                            <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/>
                        </svg>
                    </button>
                </div>
            </div>
        </template>

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
                    <input type="text" class="tags-input" id="tags-input" maxlength="100"
                           placeholder="Escribe y presiona Enter">
                </div>
                <small class="form-ayuda">Escribe cada asignatura y presiona Enter para agregarla.</small>
                <div id="asignaturas-msg" class="form-mismatch-msg" style="display:none;">
                    <i class="bi bi-exclamation-triangle"></i>
                    <span id="asignaturas-msg-text"></span>
                </div>
            </div>
        </div>

        <div class="acciones-form">
            <%-- El botón dice a dónde lleva y que se pierde lo capturado: un
                 "Volver" a secas se entendía como "regresar un paso" --%>
            <c:choose>
                <c:when test="${editando}">
                    <c:set var="volverUrl">${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}</c:set>
                    <c:set var="volverTexto">Cancelar y volver a la solicitud</c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="volverUrl">${pageContext.request.contextPath}/solicitud</c:set>
                    <c:set var="volverTexto">Cancelar y volver a solicitudes</c:set>
                </c:otherwise>
            </c:choose>
            <a href="${volverUrl}" class="btn-volver text-decoration-none"
               onclick="return confirm('Se perderán los datos capturados que no se hayan guardado. ¿Salir del formulario?');">
                <i class="bi bi-arrow-left"></i> ${volverTexto}
            </a>
            <%-- No dice "Crear/Enviar": guardar solo deja la solicitud lista;
                 todavía falta descargar el formato, firmarlo, subirlo y enviar --%>
            <button type="submit" class="btncrear">
                <i class="bi bi-check-lg"></i> ${editando ? 'Guardar cambios' : 'Guardar y continuar'}
            </button>
        </div>

    </form>
</main>

<script src="${pageContext.request.contextPath}/js/solicitud-form.js"></script>
<%@ include file="layout/footer.jsp" %>
