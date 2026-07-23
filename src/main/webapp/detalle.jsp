<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% request.setAttribute("pageTitle", "Detalles de la visita"); %>
<% request.setAttribute("activeNav", "solicitudes"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stepper.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/detalle.css">

    <c:set var="s" value="${solicitud}"/>
    <c:set var="esDocente" value="${sessionScope.rol == null || sessionScope.rol == 'Docente'}"/>
    <c:set var="esPropia" value="${sessionScope.idUsuario == s.idUsuarioSolicitante}"/>
    <c:set var="estado" value="${s.nombreEstado}"/>

    <div class="superior">
        <h2>Detalles de la visita</h2>
        <p>
            <c:choose>
                <c:when test="${!esDocente && estado == 'En revisión'}">Evalúa la solicitud enviada por el docente</c:when>
                <c:otherwise>Consulta la información y el avance de la solicitud</c:otherwise>
            </c:choose>
        </p>
    </div>

    <%-- Confirmaciones: archivo subido / datos actualizados --%>
    <c:if test="${not empty param.subido}">
        <div class="instruccion instruccion-exito" style="margin-top: 1rem;">
            <i class="bi bi-check-circle"></i>
            <div>
                <div class="instruccion-titulo">Archivo subido correctamente</div>
                <p>Tu documento ya quedó guardado en el sistema. Lo puedes ver en la sección <strong>Archivos</strong>.</p>
            </div>
        </div>
    </c:if>
    <c:if test="${not empty param.actualizado}">
        <div class="instruccion instruccion-exito" style="margin-top: 1rem;">
            <i class="bi bi-check-circle"></i>
            <div>
                <div class="instruccion-titulo">Datos actualizados</div>
                <p>Los cambios se guardaron. Recuerda que el formato FO-UTEZ-EST-08 se genera con los datos nuevos: descárgalo, fírmalo y súbelo.</p>
            </div>
        </div>
    </c:if>

    <%-- Mensajes de error al subir archivos (RN-07) --%>
    <c:if test="${not empty param.error}">
        <div class="instruccion instruccion-rechazo" style="margin-top: 1rem;">
            <i class="bi bi-exclamation-triangle"></i>
            <div>
                <div class="instruccion-titulo">No se pudo subir el archivo</div>
                <p>
                    <c:choose>
                        <c:when test="${param.error == 'tipo'}">Solo se permiten archivos PDF.</c:when>
                        <c:when test="${param.error == 'tamano'}">El archivo supera el tamaño máximo de 10 MB.</c:when>
                        <c:when test="${param.error == 'vacio'}">Selecciona un archivo antes de subir.</c:when>
                        <c:otherwise>Ocurrió un problema al guardar. Intenta de nuevo.</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </c:if>

    <%-- ===================== Card de resumen ===================== --%>
    <div class="detalle-card" style="margin-top: 1rem;">
        <div class="resumen-top">
            <div>
                <h3 class="resumen-titulo">${s.nombreEmpresaActividad}</h3>
                <div class="resumen-meta">
                    <span><i class="bi bi-pin-map"></i>${empty s.lugarDireccion ? 'Sin dirección' : s.lugarDireccion}</span>
                    <c:if test="${not empty s.fechaInicio}">
                        <span><i class="bi bi-calendar-event"></i>${s.fechaInicio}</span>
                    </c:if>
                    <span><i class="bi bi-clock-history"></i>Solicitada el ${s.fechaCreacion}</span>
                </div>
            </div>
            <span class="badge-estado estado-${fn:replace(fn:toLowerCase(estado), ' ', '-')}">${estado}</span>
        </div>

        <%-- Mini card de instrucciones: el paso que toca hacer ahora --%>
        <c:choose>
            <c:when test="${estado == 'Pendiente' && esDocente && existeFirmado}">
                <div class="instruccion instruccion-accion">
                    <i class="bi bi-send"></i>
                    <div>
                        <div class="instruccion-titulo">Formato firmado cargado: solo falta enviar</div>
                        <p>Tu FO-UTEZ-EST-08 firmado ya está en el sistema. Revisa que los datos sean correctos y da click en <strong>Enviar solicitud</strong> para mandarla a Estadías. Mientras no la envíes, puedes <strong>editar los datos</strong>.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Pendiente' && esDocente}">
                <div class="instruccion instruccion-accion">
                    <i class="bi bi-exclamation-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Sube el formato FO-UTEZ-EST-08 firmado</div>
                        <p>Descarga el documento FO-UTEZ-EST-08, fírmalo y súbelo. Cuando esté cargado da click en <strong>Enviar solicitud</strong> para mandarlo a Estadías. Si necesitas corregir algo, puedes <strong>editar los datos</strong> antes de enviar.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'En revisión' && esDocente}">
                <div class="instruccion instruccion-info">
                    <i class="bi bi-send-check"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud enviada</div>
                        <p>Tu solicitud fue recibida por el área de Estadías. Espera su revisión; te avisaremos por correo cuando haya una decisión.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'En revisión' && !esDocente}">
                <div class="instruccion instruccion-info">
                    <i class="bi bi-clock"></i>
                    <div>
                        <div class="instruccion-titulo">Esta solicitud está en espera de revisión</div>
                        <p>Revisa la solicitud y apruébala o recházala.</p>
                        <p class="instruccion-detalle">Enviado por: ${s.nombreSolicitante}</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Aprobada' && esDocente}">
                <div class="instruccion instruccion-exito">
                    <i class="bi bi-check-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud aprobada</div>
                        <p>La visita cuenta con el visto bueno. Descarga la carta responsiva, fírmala y súbela para completar tu solicitud.</p>
                        <c:if test="${not empty s.detallesDecision}">
                            <p class="instruccion-detalle">Detalles: ${s.detallesDecision}</p>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Aprobada' && !esDocente}">
                <div class="instruccion instruccion-info">
                    <i class="bi bi-clock"></i>
                    <div>
                        <div class="instruccion-titulo">Se espera la carta responsiva</div>
                        <p>La solicitud fue aprobada. El docente debe cargar la carta responsiva con las firmas correspondientes.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Rechazada'}">
                <div class="instruccion instruccion-rechazo">
                    <i class="bi bi-x-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud rechazada</div>
                        <p>
                            <c:choose>
                                <c:when test="${not empty s.detallesDecision}">Motivo: ${s.detallesDecision}</c:when>
                                <c:otherwise>El área de Estadías rechazó esta solicitud.</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Completada'}">
                <div class="instruccion instruccion-exito">
                    <i class="bi bi-check-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud completada</div>
                        <p>
                            <c:choose>
                                <c:when test="${esDocente}">Sube el reporte de la solicitud cuando completes tu visita. Lo encuentras en la sección <a href="${pageContext.request.contextPath}/reportes">Reportes</a>.</c:when>
                                <c:otherwise>El proceso de esta solicitud terminó. El reporte de la visita quedó pendiente por el docente.</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:when>
        </c:choose>

        <%-- Stepper de progreso (4 pasos) --%>
        <c:set var="stepperEstado" value="${estado}"/>
        <%@ include file="layout/stepper.jsp" %>
    </div>

    <%-- ===================== Card datos del lugar ===================== --%>
    <div class="detalle-card">
        <h6>Datos del lugar a visitar</h6>
        <div class="datos-grid">
            <div>
                <div class="dato-label">Nombre de la empresa o actividad</div>
                <div class="dato-valor">${s.nombreEmpresaActividad}</div>
            </div>
            <div>
                <div class="dato-label">Lugar o dirección</div>
                <div class="dato-valor">${empty s.lugarDireccion ? '—' : s.lugarDireccion}</div>
            </div>
            <div>
                <div class="dato-label">Teléfonos del contacto</div>
                <div class="dato-valor">${empty s.telefonoContacto ? '—' : s.telefonoContacto}</div>
            </div>
            <div>
                <div class="dato-label">Correo electrónico del contacto</div>
                <div class="dato-valor">${empty s.correoContacto ? '—' : s.correoContacto}</div>
            </div>
            <div>
                <div class="dato-label">Fecha de inicio</div>
                <div class="dato-valor">${empty s.fechaInicio ? '—' : s.fechaInicio}</div>
            </div>
            <div class="dato-full">
                <div class="dato-label">Objetivo de la visita</div>
                <div class="dato-valor">${empty s.objetivo ? '—' : s.objetivo}</div>
            </div>
        </div>
    </div>

    <%-- ===================== Card datos de los participantes ===================== --%>
    <div class="detalle-card">
        <h6>Datos de los participantes</h6>
        <div class="datos-grid">
            <div>
                <div class="dato-label">Área solicitante</div>
                <div class="dato-valor">${empty s.areaSolicitante ? '—' : s.areaSolicitante}</div>
            </div>
            <div>
                <div class="dato-label">Docente responsable</div>
                <div class="dato-valor">${s.nombreSolicitante}</div>
            </div>
        </div>

        <c:if test="${not empty s.programas}">
            <div class="dato-label" style="margin-top: 14px;">Desglose por programa educativo</div>
            <table class="tabla-programas">
                <thead>
                <tr>
                    <th>Programa educativo</th>
                    <th>Cuatrimestre</th>
                    <th>Grupo</th>
                    <th>No. estudiantes</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="p" items="${s.programas}">
                    <tr>
                        <td>${p.divisionAcademica}</td>
                        <td>${p.cuatrimestre}</td>
                        <td>${empty p.grupo ? '—' : p.grupo}</td>
                        <td>${p.noEstudiantes}</td>
                    </tr>
                </c:forEach>
                <tr class="fila-total">
                    <td colspan="3">Total de estudiantes</td>
                    <td>${s.totalEstudiantes}</td>
                </tr>
                </tbody>
            </table>
        </c:if>

        <c:if test="${not empty s.asignaturas}">
            <div class="dato-label" style="margin-top: 14px;">Asignaturas que se reforzarán</div>
            <div>
                <c:forEach var="a" items="${s.asignaturas}">
                    <span class="chip-asignatura">${a}</span>
                </c:forEach>
            </div>
        </c:if>
    </div>

    <%-- ===================== Card archivos ===================== --%>
    <div class="detalle-card">
        <h6>Archivos</h6>

        <%-- Documentos que genera el sistema con los datos de la solicitud --%>
        <c:if test="${estado != 'Pendiente'}">
            <div class="separador-archivos">Generados por el sistema</div>
            <div class="archivo-row">
                <span class="archivo-pill"><i class="bi bi-file-earmark-pdf"></i>FO-UTEZ-EST-08</span>
                <a class="btn-descargar" target="_blank"
                   href="${pageContext.request.contextPath}/documento?gen=fo&solicitud=${s.idSolicitud}">
                    <i class="bi bi-download"></i> Descargar
                </a>
            </div>

            <%-- Documentos generados al aprobar (RF-07) --%>
            <c:if test="${estado == 'Aprobada' || estado == 'Completada'}">
                <div class="archivo-row">
                    <span class="archivo-pill"><i class="bi bi-file-earmark-pdf"></i>OFICIO DE VISITA</span>
                    <a class="btn-descargar" target="_blank"
                       href="${pageContext.request.contextPath}/documento?gen=oficio&solicitud=${s.idSolicitud}">
                        <i class="bi bi-download"></i> Descargar
                    </a>
                </div>
            </c:if>
        </c:if>

        <%-- Archivos subidos por el docente --%>
        <c:if test="${not empty documentos}">
            <div class="separador-archivos">${esDocente && esPropia ? 'Subidos por ti' : 'Subidos por el docente'}</div>
            <c:forEach var="d" items="${documentos}">
                <div class="archivo-row">
                    <span class="archivo-pill">
                        <i class="bi bi-file-earmark-pdf"></i>
                        <span>${fn:toUpperCase(d.nombreTipo)}<small>${d.tamanoLegible} · ${d.fechaCarga}</small></span>
                    </span>
                    <a class="btn-descargar" href="${pageContext.request.contextPath}/documento?id=${d.idDocumento}">
                        <i class="bi bi-download"></i> Descargar
                    </a>
                </div>
            </c:forEach>
        </c:if>

        <%-- Zona del documento por firmar + carga: solo el docente dueño y
             solo en los 2 momentos del proceso en los que toca subir algo --%>
        <c:if test="${esDocente && esPropia && estado == 'Pendiente'}">
            <div class="separador-firmar">Descarga y firma este documento</div>
            <div class="archivo-row">
                <span class="archivo-pill"><i class="bi bi-file-earmark-pdf"></i>FO-UTEZ-EST-08</span>
                <a class="btn-descargar" target="_blank"
                   href="${pageContext.request.contextPath}/documento?gen=fo&solicitud=${s.idSolicitud}">
                    <i class="bi bi-download"></i> Descargar
                </a>
            </div>
            <form action="${pageContext.request.contextPath}/documento" method="POST" enctype="multipart/form-data">
                <input type="hidden" name="action" value="firmado">
                <input type="hidden" name="solicitud" value="${s.idSolicitud}">
                <div class="zona-carga ${existeFirmado ? 'zona-cargada' : ''}">
                    <c:choose>
                        <c:when test="${existeFirmado}">
                            <i class="bi bi-check-circle" style="font-size: 1.6rem;"></i>
                            <p>Tu formato firmado ya está cargado</p>
                            <small>Si te equivocaste de archivo puedes subir otro; reemplazará al anterior.</small>
                        </c:when>
                        <c:otherwise>
                            <i class="bi bi-cloud-arrow-up" style="font-size: 1.6rem; color: var(--color-texto-tenue);"></i>
                            <p>Sube el formato FO-UTEZ-EST-08 firmado</p>
                            <small>Máximo 10 MB · solo PDF</small>
                        </c:otherwise>
                    </c:choose>
                    <input type="file" name="archivo" class="form-control" accept="application/pdf" required>
                    <div class="aviso-seleccion">Archivo seleccionado, pero aún no se sube: da click en <strong>${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}</strong>.</div>
                    <button type="submit" class="btn-subir">${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}</button>
                </div>
            </form>
        </c:if>

        <c:if test="${esDocente && esPropia && estado == 'Aprobada'}">
            <div class="separador-firmar">Descarga y firma este documento</div>
            <div class="archivo-row">
                <span class="archivo-pill"><i class="bi bi-file-earmark-pdf"></i>CARTA RESPONSIVA</span>
                <a class="btn-descargar" target="_blank"
                   href="${pageContext.request.contextPath}/documento?gen=responsiva&solicitud=${s.idSolicitud}">
                    <i class="bi bi-download"></i> Descargar
                </a>
            </div>
            <form action="${pageContext.request.contextPath}/documento" method="POST" enctype="multipart/form-data"
                  onsubmit="return confirm('Al subir tu carta responsiva firmada la solicitud se cerrará como Completada. ¿Continuar?');">
                <input type="hidden" name="action" value="responsiva">
                <input type="hidden" name="solicitud" value="${s.idSolicitud}">
                <div class="zona-carga">
                    <i class="bi bi-cloud-arrow-up" style="font-size: 1.6rem; color: var(--color-texto-tenue);"></i>
                    <p>Sube la CARTA RESPONSIVA firmada</p>
                    <small>Máximo 10 MB · solo PDF</small>
                    <input type="file" name="archivo" class="form-control" accept="application/pdf" required>
                    <div class="aviso-seleccion">Archivo seleccionado, pero aún no se sube: da click en <strong>Subir archivo</strong>.</div>
                    <button type="submit" class="btn-subir">Subir archivo</button>
                </div>
            </form>
        </c:if>
    </div>

    <%-- ===================== Card evaluar solicitud (solo coordinador y En revisión) ===================== --%>
    <c:if test="${!esDocente && estado == 'En revisión'}">
        <div class="detalle-card">
            <h6>Evaluar solicitud</h6>
            <form action="${pageContext.request.contextPath}/detalle" method="POST" id="form-evaluar">
                <input type="hidden" name="id" value="${s.idSolicitud}">
                <label class="form-label" for="motivo">Motivo</label>
                <textarea name="motivo" id="motivo" class="form-control" rows="3"
                          placeholder="Detalles de la decisión"></textarea>
                <div class="acciones-evaluar">
                    <button type="submit" name="action" value="rechazar" class="btn-rechazar"
                            onclick="return validarRechazo();">
                        <i class="bi bi-x-lg"></i> Rechazar solicitud
                    </button>
                    <button type="submit" name="action" value="aprobar" class="btn-aprobar"
                            onclick="return confirm('¿Aprobar esta solicitud? El docente será notificado.');">
                        <i class="bi bi-check-lg"></i> Aprobar solicitud
                    </button>
                </div>
            </form>
        </div>
        <script>
            function validarRechazo() {
                var motivo = document.getElementById('motivo').value.trim();
                if (motivo === '') {
                    alert('Escribe el motivo del rechazo.');
                    return false;
                }
                return confirm('¿Rechazar esta solicitud? El docente será notificado.');
            }
        </script>
    </c:if>

    <%-- ===================== Barra final: Volver / Editar / Enviar ===================== --%>
    <div class="acciones-form">
        <a href="${pageContext.request.contextPath}/solicitud" class="btn-volver-detalle">
            <i class="bi bi-arrow-left"></i> Volver
        </a>
        <c:if test="${esDocente && esPropia && estado == 'Pendiente'}">
            <div class="acciones-derecha">
                <a href="${pageContext.request.contextPath}/solicitud?action=editar&id=${s.idSolicitud}"
                   class="btn-editar-datos" title="Corregir los datos antes de enviar">
                    <i class="bi bi-pencil"></i> Editar datos
                </a>
                <form action="${pageContext.request.contextPath}/detalle" method="POST" style="margin: 0;"
                      onsubmit="return confirm('¿Enviar la solicitud al área de Estadías para su revisión? Ya no podrás editar los datos.');">
                    <input type="hidden" name="id" value="${s.idSolicitud}">
                    <input type="hidden" name="action" value="enviar">
                    <button type="submit" class="btn-enviar-solicitud" ${existeFirmado ? '' : 'disabled'}
                            title="${existeFirmado ? 'Enviar a revisión de Estadías' : 'Primero sube el formato firmado'}">
                        <i class="bi bi-send"></i> Enviar solicitud
                    </button>
                </form>
            </div>
        </c:if>
    </div>

    <script>
        // Al elegir archivo se avisa que falta dar click en Subir (elegir != subir)
        document.querySelectorAll('.zona-carga input[type="file"]').forEach(function (input) {
            input.addEventListener('change', function () {
                var aviso = input.closest('.zona-carga').querySelector('.aviso-seleccion');
                if (aviso) {
                    aviso.classList.toggle('visible', input.files.length > 0);
                }
            });
        });
    </script>
</main>

<%@ include file="layout/footer.jsp" %>
