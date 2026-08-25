<%--
/**
 * Vista JSP del desglose académico de una solicitud ya registrada.
 * <p>
 * Se sirve desde /desglose (DesgloseServlet) y administra las tres entidades
 * que cuelgan de la solicitud: docentes acompañantes (SOLICITUD_DOCENTE),
 * grupos que participan (PROGRAMA_EDUCATIVO) y asignaturas a reforzar
 * (ASIGNATURA_REFORZAR_SOLICITUD).
 * </p>
 * <p>
 * Es a propósito el mismo marcado que la sección "Datos de los participantes"
 * de SolicitudDocente.jsp: las mismas filas .programa-row, los mismos chips y
 * el mismo resumen por división. Quien capturó la solicitud ya sabe usar esto;
 * lo único distinto es que aquí cada cambio se guarda solo, con un mensaje
 * JSON, en vez de esperar al botón de guardar del formulario.
 * </p>
 * <p>
 * El JSP solo pinta el armazón y los catálogos fijos de la UTEZ. Ni un dato del
 * desglose viene escrito aquí: las tres listas las pide y las pinta
 * desglose.js, incluyendo la primera carga.
 * </p>
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 25/08/2026
 */
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- El candado de aquí es por si alguien escribe la URL del JSP a mano: sin la
     solicitud en el request no hay nada que administrar, y el servlet es quien
     revisa de quién es y si todavía se puede editar. --%>
<%
    if (request.getAttribute("solicitud") == null) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
%>
<% request.setAttribute("pageTitle", "Desglose académico"); %>
<% request.setAttribute("activeNav", "solicitudes"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/desglose.css">

<main id="main-content">
    <c:set var="s" value="${solicitud}"/>

    <div class="d-flex align-items-center gap-2 mb-2">
        <h4 class="page-title text-title-request mb-0">Desglose académico</h4>
    </div>

    <%-- El estado ya viene puesto en "cargando" desde el servidor: si esperara
         a que corra desglose.js, entre que carga la página y arranca el script
         se vería una pantalla vacía sin explicación. --%>
    <div class="desglose-estado" id="estado-desglose" aria-live="polite">
        <i class="bi bi-arrow-repeat girando" id="estado-icono"></i>
        <span id="estado-texto">Cargando el desglose…</span>
    </div>

    <%-- El mismo aviso que el formulario da al editar, por la misma razón:
         cambiar el desglose regenera el FO-UTEZ-EST-08. Aquí no puede ir en un
         data-confirmar porque no hay un botón de guardar que interceptar; el
         cambio se avisa cuando de verdad ocurre, desde desglose.js. --%>
    <div class="alert alert-warning d-flex align-items-start gap-2" style="font-size: 14px;">
        <i class="bi bi-info-circle"></i>
        <div>
            Estás cambiando el desglose de la visita a
            <strong><c:out value="${s.nombreEmpresaActividad}"/></strong>.
            Cada cambio se guarda al momento, no hay botón de guardar.
            Si ya habías subido el formato FO-UTEZ-EST-08 firmado, se eliminará y
            tendrás que descargarlo, firmarlo y subirlo otra vez.
            <%-- Lo enseña desglose.js la primera vez que un cambio da de baja
                 el formato: pasa el aviso de "va a ocurrir" a "ya ocurrió" --%>
            <span id="aviso-formato-hecho" hidden>
                <br><strong>Ya ocurrió:</strong> el formato firmado que tenías subido se eliminó
                por los cambios que acabas de hacer.
            </span>
        </div>
    </div>

    <%-- Todo el contenido va dentro de #desglose para poder atenuarlo de un
         golpe mientras el servidor contesta. Arranca en "ocupado" porque la
         primera carga también es una petición. --%>
    <div id="desglose" class="ocupado">

    <div class="form-section">
        <h6>Datos de los participantes de la visita</h6>

        <%-- ==================== Docentes acompañantes ==================== --%>

        <div class="mb-4">
            <label class="form-label">Docentes acompañantes</label>
            <%-- Los chips los arma desglose.js con lo que responde el servlet.
                 A diferencia del formulario, aquí el chip NO lleva un input
                 hidden: no hay formulario que enviar, el id vive en el
                 data-id del chip y viaja en el mensaje JSON. --%>
            <div class="tags-input-wrapper" id="acompanantes-wrapper">
                <input type="text" class="tags-input" id="acompanantes-input" autocomplete="off"
                       placeholder="Escribe el nombre del docente y elígelo de la lista">
            </div>
            <div class="autocomplete-lista" id="acompanantes-sugerencias"></div>
            <small class="form-ayuda" id="acompanantes-ayuda">
                Solo aparecen docentes registrados en el sistema. Haz clic en un docente ya agregado
                para cambiarlo por otro.
            </small>
            <div id="acompanantes-msg" class="form-mismatch-msg" style="display:none;">
                <i class="bi bi-exclamation-triangle"></i>
                <span id="acompanantes-msg-text"></span>
            </div>
        </div>

        <%-- ==================== Grupos que participan ==================== --%>

        <label class="form-label d-block mb-1">Grupos que participan en la visita</label>
        <small class="form-ayuda mb-2">
            Cada fila es un grupo. Al cambiar cualquier dato se guarda solo; la fila que agregues
            se guarda en cuanto la completes.
        </small>

        <div class="programa-tabla">
            <div class="programa-header">
                <span>División académica</span>
                <span>Programa educativo</span>
                <span>Cuatrimestre</span>
                <span>Grupo</span>
                <span>Estudiantes</span>
                <span></span>
            </div>

            <div id="programas-container"></div>

            <%-- Una lista vacía y una que todavía no carga se ven igual si no
                 se dice cuál es cuál --%>
            <div class="desglose-vacio" id="sin-grupos" hidden>
                Todavía no hay grupos capturados. Agrega el primero con el botón de abajo.
            </div>
        </div>

        <div id="programas-msg" class="form-mismatch-msg" style="display:none;">
            <i class="bi bi-exclamation-triangle"></i>
            <span id="programas-msg-text"></span>
        </div>

        <button type="button" class="btn-agregar mt-3" id="btn-agregar-grupo">
            <i class="bi bi-plus-lg"></i> Agregar grupo
        </button>

        <%-- ==================== Resumen por división ==================== --%>

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
        <small class="form-ayuda">
            Lo calcula el servidor con los grupos guardados. Es el desglose que se imprime
            en el FO-UTEZ-EST-08.
        </small>
    </div>

    <%-- ==================== Asignaturas ==================== --%>

    <div class="form-section">
        <h6>Asignaturas que se reforzarán con la visita</h6>

        <div class="mb-1">
            <label class="form-label">Asignaturas</label>
            <div class="tags-input-wrapper" id="tags-wrapper">
                <input type="text" class="tags-input" id="tags-input" maxlength="100"
                       placeholder="Escribe y presiona Enter">
            </div>
            <small class="form-ayuda">
                Escribe cada asignatura y presiona Enter para agregarla. Haz clic en una ya
                agregada para corregirle el nombre.
            </small>
            <div id="asignaturas-msg" class="form-mismatch-msg" style="display:none;">
                <i class="bi bi-exclamation-triangle"></i>
                <span id="asignaturas-msg-text"></span>
            </div>
        </div>
    </div>

    </div><%-- /#desglose --%>

    <%-- Donde el formulario tiene "Guardar" aquí no va nada: ya se guardó. Solo
         queda la salida. --%>
    <div class="acciones-form">
        <a href="${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}"
           class="btn-volver text-decoration-none">
            <i class="bi bi-arrow-left"></i> Volver a la solicitud
        </a>
    </div>

    <%-- ==================== Datos para desglose.js (no se ven) ==================== --%>

    <%-- Catálogo de programas educativos por división, igual que en el
         formulario: de aquí sale la cascada división -> programa --%>
    <div id="catalogo-programas" hidden>
        <c:forEach var="entrada" items="${programasPorDivision}">
            <c:forEach var="programa" items="${entrada.value}">
                <span data-division="${entrada.key}" data-programa="<c:out value="${programa}"/>"></span>
            </c:forEach>
        </c:forEach>
    </div>

    <%-- Plantilla de una fila del desglose. Es la misma de SolicitudDocente.jsp
         salvo por los name: aquí los campos no se envían como formulario, se
         leen para armar el mensaje JSON. --%>
    <template id="tpl-programa-row">
        <div class="programa-row">
            <div class="programa-campo" data-etiqueta="División académica">
                <select class="form-control campo-division" aria-label="División académica">
                    <option value="">Elige una división…</option>
                    <c:forEach var="division" items="${divisiones}">
                        <option value="${division}" title="${nombresDivision[division]}">${division}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="programa-campo" data-etiqueta="Programa educativo">
                <select class="form-control campo-programa" aria-label="Programa educativo">
                    <option value="">Elige primero la división</option>
                </select>
            </div>
            <div class="programa-campo" data-etiqueta="Cuatrimestre">
                <select class="form-control campo-cuatrimestre" aria-label="Cuatrimestre">
                    <option value="">—</option>
                    <c:forEach var="n" begin="1" end="11">
                        <option value="${n}">${n}°</option>
                    </c:forEach>
                </select>
            </div>
            <div class="programa-campo" data-etiqueta="Grupo">
                <select class="form-control campo-grupo" aria-label="Grupo">
                    <option value="">—</option>
                    <c:forTokens items="A,B,C,D,E,F,G,H,I,J" delims="," var="letra">
                        <option value="${letra}">${letra}</option>
                    </c:forTokens>
                </select>
            </div>
            <div class="programa-campo" data-etiqueta="Estudiantes">
                <input type="number" class="form-control campo-estudiantes"
                       min="1" max="999" step="1" placeholder="0" aria-label="Número de estudiantes">
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

    <%-- Molde de un chip, para asignaturas y para acompañantes.

         El texto va en un <span> con role="button" y no en un <button> de
         verdad: un botón trae el borde y el fondo que le pone el navegador, y
         dentro de un chip eso se ve como si algo se hubiera roto. Quitárselo
         por CSS funciona, pero basta con que form.css llegue cacheado para que
         el chip aparezca descuadrado. El span no tiene nada que quitarle.
         El role y el tabindex lo dejan igual de accesible: desglose.js atiende
         Enter y Espacio como si fuera un botón. --%>
    <template id="tpl-chip">
        <span class="tag-chip">
            <span class="chip-texto" role="button" tabindex="0"></span>
            <button type="button" class="tag-remove" aria-label="Quitar">&times;</button>
        </span>
    </template>
</main>

<%-- Lo único que el JSP le pasa al script: a qué solicitud pertenece la página
     y a dónde mandar los mensajes. De ahí en adelante todos los datos viajan
     en el JSON. --%>
<script src="${pageContext.request.contextPath}/js/desglose.js"
        data-solicitud="${s.idSolicitud}"
        data-url="${pageContext.request.contextPath}/desglose"
        data-docentes="${pageContext.request.contextPath}/docentes"></script>

<%@ include file="layout/footer.jsp" %>
