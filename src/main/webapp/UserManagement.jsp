<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Esta vista se sirve desde /usuarios (UsuarioServlet). El candado de aquí es
     por si alguien escribe la URL del JSP a mano: el rol sale de la sesión.
     Manda un 403, que el contenedor convierte en la página de "No tienes permiso". --%>
<%
    if (!"Administrador".equals(session.getAttribute("rol"))) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
%>
<% request.setAttribute("pageTitle", "Gestión de usuarios"); %>
<% request.setAttribute("activeNav", "usuarios"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/detalle.css">

    <c:set var="editando" value="${not empty usuarioEditado}"/>
    <c:set var="confirmandoBaja" value="${not empty usuarioABorrar}"/>

    <div class="superior">
        <h2>Gestión de usuarios</h2>
        <p>Da de alta cuentas y define con qué rol entran al sistema</p>
    </div>

    <%-- Mismas mini cards de aviso que usan los detalles de la solicitud. El
         contenedor lo reescribe gestion-usuarios.js después de cada operación
         hecha con fetch; al entrar a la página lo llena el servlet. --%>
    <div id="avisos">
        <c:if test="${not empty mensaje}">
            <div class="instruccion instruccion-exito" style="margin-top: 1rem;">
                <i class="bi bi-check-circle"></i>
                <div>${mensaje}</div>
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="instruccion instruccion-rechazo" style="margin-top: 1rem;">
                <i class="bi bi-exclamation-triangle"></i>
                <div>${error}</div>
            </div>
        </c:if>
    </div>

    <%-- Confirmación de baja como PÁGINA: es el camino de quien no tiene
         JavaScript. Con JavaScript la misma confirmación sale en el modal de
         abajo y la lista nunca se pierde de vista. --%>
    <c:if test="${confirmandoBaja}">
        <h2 class="titulo-solicitudes">Confirmar eliminación</h2>
        <div class="form-section">
            <h6>Vas a eliminar a <c:out value="${usuarioABorrar.nombre}"/></h6>

            <p>Se eliminará la cuenta <strong><c:out value="${usuarioABorrar.correo}"/></strong>
               (<c:out value="${usuarioABorrar.nombreRol}"/>) y todo lo que registró en el sistema.
               <strong>Esta acción no se puede deshacer.</strong></p>

            <c:choose>
                <c:when test="${bajaSolicitudes > 0 or bajaReportes > 0}">
                    <div class="instruccion instruccion-rechazo">
                        <i class="bi bi-exclamation-triangle"></i>
                        <div>
                            <div class="instruccion-titulo">Se borrarán para siempre:</div>
                            <ul style="margin: 6px 0 0 0; padding-left: 20px;">
                                <c:if test="${bajaSolicitudes > 0}">
                                    <li>${bajaSolicitudes} solicitud${bajaSolicitudes == 1 ? '' : 'es'} de visita,
                                        con su desglose de grupos y sus documentos</li>
                                </c:if>
                                <c:if test="${bajaReportes > 0}">
                                    <li>${bajaReportes} reporte${bajaReportes == 1 ? '' : 's'} de visita,
                                        con sus evidencias fotográficas</li>
                                </c:if>
                            </ul>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="form-ayuda">Este usuario no tiene solicitudes ni reportes propios.</p>
                </c:otherwise>
            </c:choose>

            <%-- El trabajo de otros no se toca: solo se desliga --%>
            <c:if test="${bajaAutorizaciones > 0 or bajaAcompanamientos > 0}">
                <p class="form-ayuda" style="margin-top: 12px;">
                    No se borrarán las solicitudes de otros docentes:
                    <c:if test="${bajaAutorizaciones > 0}">
                        en ${bajaAutorizaciones} que evaluó solo se quitará su firma como quien autorizó<c:if test="${bajaAcompanamientos > 0}">, y</c:if>
                    </c:if>
                    <c:if test="${bajaAcompanamientos > 0}">
                        en ${bajaAcompanamientos} solo se le quitará de la lista de docentes acompañantes
                    </c:if>.
                </p>
            </c:if>

            <form action="${pageContext.request.contextPath}/usuarios" method="POST">
                <input type="hidden" name="action" value="eliminar">
                <input type="hidden" name="id" value="${usuarioABorrar.id}">
                <div class="acciones-form">
                    <a href="${pageContext.request.contextPath}/usuarios" class="btn-volver text-decoration-none">
                        <i class="bi bi-arrow-left"></i> Cancelar y volver a la lista
                    </a>
                    <button type="submit" class="btncrear btn-rojo">
                        <i class="bi bi-trash"></i> Sí, eliminar todo
                    </button>
                </div>
            </form>
        </div>
    </c:if>

    <c:if test="${not confirmandoBaja}">
    <h2 class="titulo-solicitudes">${editando ? 'Editar usuario' : 'Nuevo usuario'}</h2>

    <div class="form-section">
        <form id="form-usuario" action="${pageContext.request.contextPath}/usuarios" method="POST" autocomplete="off">
            <input type="hidden" name="action" value="${editando ? 'actualizar' : 'crear'}">
            <c:if test="${editando}">
                <input type="hidden" name="id" value="${usuarioEditado.id}">
            </c:if>

            <h6>Datos de la cuenta</h6>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label" for="nombre">Nombre completo</label>
                    <input id="nombre" name="nombre" type="text" class="form-control"
                           placeholder="Ej. Ana López Ramírez" maxlength="100"
                           value="${fn:escapeXml(usuarioEditado.nombre)}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="correo">Correo electrónico</label>
                    <input id="correo" name="correo" type="email" class="form-control"
                           placeholder="usuario@utez.edu.mx" maxlength="100" autocomplete="off"
                           value="${fn:escapeXml(usuarioEditado.correo)}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="rol">Rol</label>
                    <select id="rol" name="rol" class="form-control" required>
                        <option value="">Selecciona un rol</option>
                        <c:forEach var="r" items="${rolesDisponibles}">
                            <option value="${fn:escapeXml(r)}"
                                    ${usuarioEditado.nombreRol == r ? 'selected' : ''}>${fn:escapeXml(r)}</option>
                        </c:forEach>
                    </select>
                    <span class="form-ayuda">Estadías y Administrador ven las solicitudes de todos los docentes.</span>
                </div>
            </div>

            <%-- Las dos contraseñas van en su propio bloque, una al lado de la
                 otra: emparejadas se leen como un solo campo y la regla se
                 explica una sola vez, debajo de las dos. --%>
            <c:if test="${not editando}">
                <h6 class="mt-4">Contraseña de acceso</h6>

                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label" for="contrasena">Contraseña temporal</label>
                        <input id="contrasena" name="contrasena" type="password" class="form-control" required
                               minlength="8" pattern="(?=.*[A-Za-z])(?=.*\d).{8,}" autocomplete="new-password"
                               title="Mínimo 8 caracteres, con letras y números">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="contrasena2">Confirmar contraseña</label>
                        <%-- data-igual-a lo revisa validacion-cuenta.js mientras se
                             escribe; el servlet las vuelve a comparar de todos modos --%>
                        <input id="contrasena2" name="contrasena2" type="password" class="form-control" required
                               autocomplete="new-password"
                               data-igual-a="#contrasena" data-mensaje="Las contraseñas no coinciden.">
                    </div>
                    <div class="col-12">
                        <span class="form-ayuda">
                            Al menos 8 caracteres con letras y números. Escríbela dos veces para
                            descartar errores de captura; el usuario puede cambiarla después desde
                            "¿Olvidaste tu contraseña?".
                        </span>
                    </div>
                </div>
            </c:if>

            <div class="acciones-form ${editando ? '' : 'acciones-form--derecha'}">
                <c:if test="${editando}">
                    <a href="${pageContext.request.contextPath}/usuarios" class="btn-volver text-decoration-none">
                        <i class="bi bi-arrow-left"></i> Cancelar edición
                    </a>
                </c:if>
                <button type="submit" class="btncrear" data-cargando="Guardando…">
                    <i class="bi ${editando ? 'bi-check-lg' : 'bi-person-plus'}"></i>
                    ${editando ? 'Guardar cambios' : 'Crear usuario'}
                </button>
            </div>
        </form>
    </div>
    </c:if>

    <h2 class="titulo-solicitudes">Usuarios registrados</h2>

    <%-- El estado vacío y la tabla conviven: al crear el primer usuario desde
         el panel se cambia cuál de los dos está oculto, sin recargar. --%>
    <div id="sin-usuarios" class="solicitud-vacia" ${empty listaUsuarios ? '' : 'hidden'}>
        <h5>Aún no hay usuarios</h5>
        <p>Las cuentas que des de alta aparecerán en esta lista</p>
    </div>

    <div id="card-usuarios" class="detalle-card" style="margin-top: 1rem;"
         ${empty listaUsuarios ? 'hidden' : ''}>
        <div class="tabla-scroll">
            <table class="tabla-programas">
                <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Rol</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody id="lista-usuarios">
                <c:forEach var="u" items="${listaUsuarios}">
                    <tr data-id="${u.id}">
                        <td class="celda-nombre">
                            <span class="usuario-nombre"><c:out value="${u.nombre}"/></span>
                            <c:if test="${u.id == sessionScope.idUsuario}">
                                <span class="text-muted small">(tú)</span>
                            </c:if>
                        </td>
                        <td class="celda-correo"><c:out value="${u.correo}"/></td>
                        <td class="celda-rol">
                            <span class="badge-estado rol-${fn:toLowerCase(u.nombreRol)}"><c:out value="${u.nombreRol}"/></span>
                        </td>
                        <td style="white-space: nowrap;">
                            <%-- Los href siguen apuntando al flujo de siempre: si el
                                 JavaScript no cargó, "Editar" y "Eliminar" navegan
                                 como antes. Con él, se abren los modales. --%>
                            <a class="btn-descargar" data-accion="editar" style="margin-right: 6px;"
                               href="${pageContext.request.contextPath}/usuarios?action=editar&id=${u.id}">Editar</a>
                            <%-- La cuenta propia no se borra: dejaría al sistema sin quien lo administre.
                                 El borrado es irreversible, así que pasa por una confirmación que
                                 detalla qué solicitudes y reportes se van a perder. --%>
                            <c:if test="${u.id != sessionScope.idUsuario}">
                                <a class="btn-descargar btn-rojo" data-accion="eliminar"
                                   href="${pageContext.request.contextPath}/usuarios?action=confirmar&id=${u.id}">Eliminar</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <%-- Molde de la fila que se agrega al dar de alta un usuario sin recargar.
         Un usuario recién creado nunca es el de la sesión, así que siempre
         lleva sus dos botones. --%>
    <template id="tpl-fila-usuario">
        <tr>
            <td class="celda-nombre"><span class="usuario-nombre"></span></td>
            <td class="celda-correo"></td>
            <td class="celda-rol"><span class="badge-estado"></span></td>
            <td style="white-space: nowrap;">
                <a class="btn-descargar" data-accion="editar" style="margin-right: 6px;"
                   href="${pageContext.request.contextPath}/usuarios?action=editar&id=">Editar</a>
                <a class="btn-descargar btn-rojo" data-accion="eliminar"
                   href="${pageContext.request.contextPath}/usuarios?action=confirmar&id=">Eliminar</a>
            </td>
        </tr>
    </template>

    <%-- ==================== Modal: editar usuario ==================== --%>
    <div class="modal fade" id="modal-editar" tabindex="-1" aria-labelledby="titulo-modal-editar" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <form id="form-editar" action="${pageContext.request.contextPath}/usuarios" method="POST" autocomplete="off">
                    <input type="hidden" name="action" value="actualizar">
                    <input type="hidden" name="id" value="">

                    <div class="modal-header">
                        <h6 class="modal-title" id="titulo-modal-editar">Editar usuario</h6>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>

                    <div class="modal-body">
                        <%-- Los errores de esta edición se quedan aquí dentro: cerrar
                             el modal por un correo repetido perdería lo escrito --%>
                        <div id="aviso-modal-editar"></div>

                        <div class="row g-3">
                            <div class="col-12">
                                <label class="form-label" for="editar-nombre">Nombre completo</label>
                                <input id="editar-nombre" name="nombre" type="text" class="form-control"
                                       maxlength="100" required>
                            </div>
                            <div class="col-12">
                                <label class="form-label" for="editar-correo">Correo electrónico</label>
                                <input id="editar-correo" name="correo" type="email" class="form-control"
                                       maxlength="100" required>
                            </div>
                            <div class="col-12">
                                <label class="form-label" for="editar-rol">Rol</label>
                                <select id="editar-rol" name="rol" class="form-control" required>
                                    <c:forEach var="r" items="${rolesDisponibles}">
                                        <option value="${fn:escapeXml(r)}">${fn:escapeXml(r)}</option>
                                    </c:forEach>
                                </select>
                                <span class="form-ayuda">
                                    La contraseña no se cambia desde aquí: el usuario la restablece
                                    con "¿Olvidaste tu contraseña?".
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn-volver" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btncrear" data-cargando="Guardando…">
                            <i class="bi bi-check-lg"></i> Guardar cambios
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <%-- ==================== Modal: confirmar baja ==================== --%>
    <div class="modal fade" id="modal-eliminar" tabindex="-1" aria-labelledby="titulo-modal-eliminar" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <form id="form-eliminar" action="${pageContext.request.contextPath}/usuarios" method="POST">
                    <input type="hidden" name="action" value="eliminar">
                    <input type="hidden" name="id" value="">

                    <div class="modal-header">
                        <h6 class="modal-title" id="titulo-modal-eliminar">Confirmar eliminación</h6>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>

                    <div class="modal-body">
                        <p>
                            Se eliminará la cuenta de <strong id="baja-nombre"></strong>
                            (<span id="baja-correo"></span>) y todo lo que registró en el sistema.
                            <strong>Esta acción no se puede deshacer.</strong>
                        </p>

                        <%-- Lo que se pierde para siempre y lo que solo se desliga:
                             los rellena gestion-usuarios.js con el conteo del servlet --%>
                        <div id="baja-destruye" class="instruccion instruccion-rechazo" hidden>
                            <i class="bi bi-exclamation-triangle"></i>
                            <div>
                                <div class="instruccion-titulo">Se borrarán para siempre:</div>
                                <ul id="baja-lista" style="margin: 6px 0 0 0; padding-left: 20px;"></ul>
                            </div>
                        </div>

                        <p id="baja-limpia" class="form-ayuda" hidden>
                            Este usuario no tiene solicitudes ni reportes propios.
                        </p>

                        <p id="baja-desliga" class="form-ayuda" style="margin-top: 12px;" hidden></p>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn-volver" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btncrear btn-rojo" data-cargando="Eliminando…">
                            <i class="bi bi-trash"></i> Sí, eliminar todo
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/validacion-cuenta.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/gestion-usuarios.js" defer></script>
</main>

<%@ include file="layout/footer.jsp" %>
