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

    <%-- Mismas mini cards de aviso que usan los detalles de la solicitud --%>
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
        <form action="${pageContext.request.contextPath}/usuarios" method="POST" autocomplete="off">
            <input type="hidden" name="action" value="${editando ? 'actualizar' : 'crear'}">
            <c:if test="${editando}">
                <input type="hidden" name="id" value="${usuarioEditado.id}">
            </c:if>

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
                <c:if test="${not editando}">
                    <div class="col-md-6">
                        <label class="form-label" for="contrasena">Contraseña temporal</label>
                        <input id="contrasena" name="contrasena" type="password" class="form-control" required
                               minlength="8" pattern="(?=.*[A-Za-z])(?=.*\d).{8,}" autocomplete="off"
                               title="Mínimo 8 caracteres, con letras y números">
                        <span class="form-ayuda">Al menos 8 caracteres con letras y números. El usuario puede cambiarla desde "¿Olvidaste tu contraseña?".</span>
                    </div>
                </c:if>
            </div>

            <div class="acciones-form ${editando ? '' : 'acciones-form--derecha'}">
                <c:if test="${editando}">
                    <a href="${pageContext.request.contextPath}/usuarios" class="btn-volver text-decoration-none">
                        <i class="bi bi-arrow-left"></i> Cancelar edición
                    </a>
                </c:if>
                <button type="submit" class="btncrear">
                    <i class="bi ${editando ? 'bi-check-lg' : 'bi-person-plus'}"></i>
                    ${editando ? 'Guardar cambios' : 'Crear usuario'}
                </button>
            </div>
        </form>
    </div>
    </c:if>

    <h2 class="titulo-solicitudes">Usuarios registrados</h2>

    <c:choose>
        <c:when test="${empty listaUsuarios}">
            <div class="solicitud-vacia">
                <h5>Aún no hay usuarios</h5>
                <p>Las cuentas que des de alta aparecerán en esta lista</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="detalle-card" style="margin-top: 1rem;">
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
                    <tbody>
                    <c:forEach var="u" items="${listaUsuarios}">
                        <tr>
                            <td>
                                <c:out value="${u.nombre}"/>
                                <c:if test="${u.id == sessionScope.idUsuario}">
                                    <span class="text-muted small">(tú)</span>
                                </c:if>
                            </td>
                            <td><c:out value="${u.correo}"/></td>
                            <td>
                                <span class="badge-estado rol-${fn:toLowerCase(u.nombreRol)}"><c:out value="${u.nombreRol}"/></span>
                            </td>
                            <td style="white-space: nowrap;">
                                <a class="btn-descargar" style="margin-right: 6px;"
                                   href="${pageContext.request.contextPath}/usuarios?action=editar&id=${u.id}">Editar</a>
                                <%-- La cuenta propia no se borra: dejaría al sistema sin quien lo administre.
                                     El borrado es irreversible, así que pasa por una pantalla de confirmación
                                     que detalla qué solicitudes y reportes se van a perder. --%>
                                <c:if test="${u.id != sessionScope.idUsuario}">
                                    <a class="btn-descargar btn-rojo"
                                       href="${pageContext.request.contextPath}/usuarios?action=confirmar&id=${u.id}">Eliminar</a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="layout/footer.jsp" %>
