<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% request.setAttribute("pageTitle", "Reporte de visita"); %>
<% request.setAttribute("activeNav", "reportes"); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/detalle.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/reporte.css">
    <script src="${pageContext.request.contextPath}/js/reporte-form.js" defer></script>

    <c:set var="r" value="${reporte}"/>
    <c:set var="estado" value="${r.nombreEstado}"/>

    <div class="superior">
        <h2>Reporte de visita</h2>
        <p>${r.nombreEmpresaActividad}</p>
    </div>

    <%-- ===================== Confirmaciones y errores (PRG) ===================== --%>
    <c:if test="${not empty param.generado}">
        <div class="instruccion instruccion-exito instruccion-reporte">
            <i class="bi bi-check-circle"></i>
            <div>
                <div class="instruccion-titulo">Reporte generado</div>
                <p>Descarga el formato del reporte, fírmalo y súbelo para poder enviarlo a Estadías.</p>
            </div>
        </div>
    </c:if>
    <c:if test="${not empty param.enviado}">
        <div class="instruccion instruccion-exito instruccion-reporte">
            <i class="bi bi-send-check"></i>
            <div>
                <div class="instruccion-titulo">Reporte enviado</div>
                <p>El área de Estadías revisará tu reporte y se te notificará la decisión por correo.</p>
            </div>
        </div>
    </c:if>
    <c:if test="${param.subido == 'firmado'}">
        <div class="instruccion instruccion-exito instruccion-reporte">
            <i class="bi bi-check-circle"></i>
            <div>
                <div class="instruccion-titulo">Reporte firmado cargado</div>
                <p>Ya puedes enviar el reporte al área de Estadías.</p>
            </div>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="instruccion instruccion-rechazo instruccion-reporte">
            <i class="bi bi-exclamation-triangle"></i>
            <div>
                <div class="instruccion-titulo">No se pudo guardar</div>
                <p>
                    <c:choose>
                        <c:when test="${param.error == 'tipo'}">Las imágenes deben ser JPG o PNG.</c:when>
                        <c:when test="${param.error == 'tamano'}">Cada imagen debe pesar máximo 5 MB.</c:when>
                        <c:when test="${param.error == 'maximo'}">El reporte lleva exactamente 3 imágenes; quita alguna.</c:when>
                        <c:when test="${param.error == 'minimo'}">El reporte debe llevar exactamente 3 imágenes de la visita.</c:when>
                        <c:when test="${param.error == 'vacio'}">Escribe los resultados de la visita.</c:when>
                        <c:when test="${param.error == 'imagen'}">Ocurrió un problema al guardar las imágenes. Intenta de nuevo.</c:when>
                        <c:when test="${param.error == 'firmado-tipo'}">El reporte firmado debe ser un PDF.</c:when>
                        <c:when test="${param.error == 'firmado-tamano'}">El reporte firmado debe pesar máximo 10 MB.</c:when>
                        <c:when test="${param.error == 'firmado-vacio'}">Selecciona el PDF del reporte firmado antes de subirlo.</c:when>
                        <c:otherwise>Ocurrió un problema al guardar. Intenta de nuevo.</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </c:if>

    <%-- ===================== Banner de estado (según rol) ===================== --%>
    <c:choose>
        <c:when test="${estado == 'Pendiente' && !esDueno}">
            <div class="instruccion instruccion-info instruccion-reporte">
                <i class="bi bi-clock"></i>
                <div>
                    <div class="instruccion-titulo">El docente todavía no sube el reporte de visita</div>
                    <p>La fecha de la visita es el ${r.fecha}</p>
                    <p>
                        <a href="${pageContext.request.contextPath}/detalle?id=${r.idSolicitud}">
                            Visita de: ${r.nombreSolicitante}
                        </a>
                    </p>
                </div>
            </div>
        </c:when>
        <c:when test="${estado == 'Completado' && esDueno}">
            <div class="instruccion instruccion-info instruccion-reporte">
                <i class="bi bi-hourglass-split"></i>
                <div>
                    <div class="instruccion-titulo">Tu reporte está en revisión</div>
                    <p>El área de Estadías lo evaluará y se te notificará la decisión por correo.</p>
                </div>
            </div>
        </c:when>
        <c:when test="${estado == 'Rechazado'}">
            <div class="instruccion instruccion-rechazo instruccion-reporte">
                <i class="bi bi-exclamation-triangle"></i>
                <div>
                    <div class="instruccion-titulo">Reporte rechazado por el área de Estadías</div>
                    <p><c:out value="${r.motivo}"/></p>
                    <c:if test="${esDueno && empty param.editar}">
                        <p>Corrige el reporte con <strong>Editar formulario</strong> y vuelve a enviarlo.</p>
                    </c:if>
                </div>
            </div>
        </c:when>
        <c:when test="${estado == 'Aprobado'}">
            <div class="instruccion instruccion-exito instruccion-reporte">
                <i class="bi bi-check-circle"></i>
                <div>
                    <div class="instruccion-titulo">Reporte aprobado por el área de Estadías</div>
                    <p>El proceso de esta visita quedó cerrado. Puedes consultarlo desde el Histórico.</p>
                </div>
            </div>
        </c:when>
    </c:choose>

    <%-- ===================== Card resumen de la solicitud ===================== --%>
    <div class="detalle-card card-reporte">
        <div class="resumen-top">
            <div>
                <h3 class="resumen-titulo">${r.nombreEmpresaActividad}</h3>
                <div class="resumen-meta">
                    <span><i class="bi bi-pin-map"></i>${empty r.lugarDireccion ? 'Sin dirección' : r.lugarDireccion}</span>
                    <span><i class="bi bi-calendar-event"></i>Visita: ${r.fecha}</span>
                    <c:if test="${!esDueno}">
                        <span><i class="bi bi-person"></i>${r.nombreSolicitante}</span>
                    </c:if>
                </div>
            </div>
            <span class="badge-estado estado-${fn:toLowerCase(estado)}">${estado}</span>
        </div>
        <a class="btn-descargar btn-verde btn-resumen-solicitud"
           href="${pageContext.request.contextPath}/detalle?id=${r.idSolicitud}">
            <i class="bi bi-arrow-right"></i> Ir a la solicitud
        </a>
    </div>

    <c:choose>
        <%-- ============================================================
             Sub-fase "formulario": el docente captura o corrige los
             resultados, observaciones y las 3 imágenes de evidencia.
             ============================================================ --%>
        <c:when test="${subFase == 'formulario'}">
            <div class="detalle-card">
                <h6>${estado == 'Rechazado' ? 'Corregir reporte de la visita' : 'Completar reporte de la visita'}</h6>
                <form action="${pageContext.request.contextPath}/reporte" method="POST"
                      enctype="multipart/form-data" id="form-reporte">
                    <input type="hidden" name="id" value="${r.idReporte}">
                    <input type="hidden" name="action" value="generar">

                    <label class="form-label" for="resultados">Resultados de la visita</label>
                    <textarea name="resultados" id="resultados" class="form-control" rows="4" required
                              placeholder="Describe lo que se logró en la visita"><c:out value="${r.resultados}"/></textarea>

                    <label class="form-label campo-observaciones" for="observaciones">Observaciones</label>
                    <textarea name="observaciones" id="observaciones" class="form-control" rows="3"
                              placeholder="Observaciones adicionales (opcional)"><c:out value="${r.observaciones}"/></textarea>

                    <div class="separador-firmar separador-imagenes">Imágenes de la visita (exactamente 3 · JPG/PNG · máx. 5 MB c/u)</div>

                    <div id="galeria-previa" class="galeria-previa">
                        <c:forEach var="img" items="${imagenes}">
                            <div class="galeria-item" data-id-imagen="${img.idImagen}">
                                <img src="${pageContext.request.contextPath}/reporte?imagen=${img.idImagen}"
                                     alt="Imagen del reporte">
                                <button type="button" class="quitar-imagen" title="Quitar imagen">&times;</button>
                            </div>
                        </c:forEach>
                    </div>

                    <button type="button" id="btn-agregar-imagen" class="btn-agregar btn-agregar-imagen">
                        <i class="bi bi-plus-lg"></i> Agregar imagen
                    </button>
                    <input type="file" id="input-imagen-oculto" accept="image/jpeg,image/png" style="display:none;">
                </form>
            </div>
        </c:when>

        <%-- ============================================================
             Sub-fase "firmar": ya generó el formulario; descarga el
             formato, sube el PDF firmado y envía el reporte a Estadías.
             ============================================================ --%>
        <c:when test="${subFase == 'firmar'}">
            <div class="detalle-card">
                <h6>Resultados capturados</h6>
                <div class="datos-grid">
                    <div class="dato-full">
                        <div class="dato-label">Resultados</div>
                        <div class="dato-valor"><c:out value="${r.resultados}"/></div>
                    </div>
                    <div class="dato-full">
                        <div class="dato-label">Observaciones</div>
                        <div class="dato-valor"><c:out value="${empty r.observaciones ? '—' : r.observaciones}"/></div>
                    </div>
                </div>
                <c:if test="${not empty imagenes}">
                    <div class="galeria-previa">
                        <c:forEach var="img" items="${imagenes}">
                            <div class="galeria-item">
                                <img src="${pageContext.request.contextPath}/reporte?imagen=${img.idImagen}"
                                     alt="Imagen del reporte">
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
                <a class="btn-descargar btn-resumen-solicitud"
                   href="${pageContext.request.contextPath}/reporte?id=${r.idReporte}&editar=1"
                   title="Al editar deberás volver a firmar y subir el formato">
                    <i class="bi bi-pencil"></i> Editar formulario
                </a>
            </div>

            <div class="detalle-card">
                <h6>Firmar y enviar el reporte</h6>
                <div class="separador-firmar">Descarga y firma este documento</div>
                <div class="archivo-row">
                    <span class="archivo-pill"><i class="bi bi-file-earmark-pdf"></i>REPORTE DE VISITA</span>
                    <a class="btn-descargar" target="_blank"
                       href="${pageContext.request.contextPath}/documento?gen=reporte&reporte=${r.idReporte}">
                        <i class="bi bi-download"></i> Descargar
                    </a>
                </div>
                <form action="${pageContext.request.contextPath}/documento" method="POST" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="reporteFirmado">
                    <input type="hidden" name="reporte" value="${r.idReporte}">
                    <div class="zona-carga ${existeFirmado ? 'zona-cargada' : ''}">
                        <c:choose>
                            <c:when test="${existeFirmado}">
                                <i class="bi bi-check-circle" style="font-size: 1.6rem;"></i>
                                <p>Tu reporte firmado ya está cargado</p>
                                <small>Si te equivocaste de archivo puedes subir otro; reemplazará al anterior.</small>
                            </c:when>
                            <c:otherwise>
                                <i class="bi bi-cloud-arrow-up" style="font-size: 1.6rem; color: var(--color-texto-tenue);"></i>
                                <p>Sube el reporte de visita firmado</p>
                                <small>Máximo 10 MB · solo PDF</small>
                            </c:otherwise>
                        </c:choose>
                        <input type="file" name="archivo" class="form-control" accept="application/pdf" required>
                        <div class="aviso-seleccion">Archivo seleccionado, pero aún no se sube: da click en <strong>${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}</strong>.</div>
                        <button type="submit" class="btn-subir">${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}</button>
                    </div>
                </form>
                <form action="${pageContext.request.contextPath}/reporte" method="POST" id="form-enviar-reporte">
                    <input type="hidden" name="id" value="${r.idReporte}">
                    <input type="hidden" name="action" value="enviar">
                </form>
            </div>
        </c:when>

        <%-- ============================================================
             Vista de solo lectura: reporte enviado (Completado),
             Aprobado o Rechazado (sin editar). En Pendiente visto por
             Estadías solo se muestra el banner y el resumen de arriba.
             ============================================================ --%>
        <c:otherwise>
            <c:if test="${estado != 'Pendiente'}">
                <div class="detalle-card">
                    <h6>Resultados de la visita</h6>
                    <div class="datos-grid">
                        <div class="dato-full">
                            <div class="dato-label">Resultados</div>
                            <div class="dato-valor"><c:out value="${r.resultados}"/></div>
                        </div>
                        <div class="dato-full">
                            <div class="dato-label">Observaciones</div>
                            <div class="dato-valor"><c:out value="${empty r.observaciones ? '—' : r.observaciones}"/></div>
                        </div>
                    </div>
                </div>

                <c:if test="${not empty imagenes}">
                    <div class="detalle-card">
                        <h6>Fotografías de la visita</h6>
                        <div class="galeria-previa">
                            <c:forEach var="img" items="${imagenes}">
                                <div class="galeria-item galeria-item--lg">
                                    <img src="${pageContext.request.contextPath}/reporte?imagen=${img.idImagen}"
                                         alt="Imagen del reporte">
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <div class="detalle-card">
                    <h6>Archivos</h6>
                    <div class="archivo-row">
                        <span class="archivo-pill"><i class="bi bi-file-earmark-text"></i>REPORTE DE VISITA</span>
                        <a class="btn-descargar" target="_blank"
                           href="${pageContext.request.contextPath}/documento?gen=reporte&reporte=${r.idReporte}">
                            <i class="bi bi-download"></i> Descargar
                        </a>
                    </div>
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
                </div>

                <%-- Card evaluar: solo Estadías/Admin con el reporte enviado --%>
                <c:if test="${estado == 'Completado' && !esDueno}">
                    <div class="detalle-card">
                        <h6>Evaluar reporte</h6>
                        <form action="${pageContext.request.contextPath}/reporte" method="POST" id="form-evaluar-reporte">
                            <input type="hidden" name="id" value="${r.idReporte}">
                            <label class="form-label" for="motivo-reporte">Motivo</label>
                            <textarea name="motivo" id="motivo-reporte" class="form-control" rows="3"
                                      placeholder="Detalles de la decisión"></textarea>
                            <div class="acciones-evaluar">
                                <button type="submit" name="action" value="rechazar" class="btn-rechazar">
                                    <i class="bi bi-x-lg"></i> Rechazar reporte
                                </button>
                                <button type="submit" name="action" value="aprobar" class="btn-aprobar">
                                    <i class="bi bi-check-lg"></i> Aprobar reporte
                                </button>
                            </div>
                        </form>
                    </div>
                </c:if>

                <%-- Card corregir: el dueño de un reporte rechazado --%>
                <c:if test="${estado == 'Rechazado' && esDueno}">
                    <div class="detalle-card">
                        <h6>Corregir reporte</h6>
                        <p>Edita los datos del reporte conforme a lo solicitado y vuelve a enviarlo.</p>
                        <a class="btn-descargar btn-verde"
                           href="${pageContext.request.contextPath}/reporte?id=${r.idReporte}&editar=1">
                            <i class="bi bi-pencil-square"></i> Editar formulario
                        </a>
                    </div>
                </c:if>
            </c:if>
        </c:otherwise>
    </c:choose>

    <div class="acciones-form">
        <a href="${pageContext.request.contextPath}/reportes" class="btn-volver-detalle">
            <i class="bi bi-arrow-left"></i> Volver a reportes
        </a>
        <c:choose>
            <c:when test="${subFase == 'formulario'}">
                <button type="submit" form="form-reporte" class="btn-enviar-solicitud">
                    <i class="bi bi-file-earmark-text"></i> Generar reporte
                </button>
            </c:when>
            <c:when test="${subFase == 'firmar'}">
                <button type="submit" form="form-enviar-reporte" class="btn-enviar-solicitud" ${existeFirmado ? '' : 'disabled'}
                        title="${existeFirmado ? 'Enviar a revisión de Estadías' : 'Primero sube el reporte firmado'}">
                    <i class="bi bi-send"></i> Enviar reporte
                </button>
            </c:when>
        </c:choose>
    </div>
</main>

<%@ include file="layout/footer.jsp" %>
