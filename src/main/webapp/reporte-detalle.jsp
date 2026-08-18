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

    <%-- Volver también aquí arriba: quien entró solo a consultar no tiene por
         qué recorrer la página entera para encontrar la salida --%>
    <a href="${pageContext.request.contextPath}/reportes" class="volver-arriba">
        <i class="bi bi-arrow-left"></i> Volver a reportes
    </a>

    <div class="superior">
        <h2>Reporte de visita</h2>
        <p><c:out value="${r.nombreEmpresaActividad}"/></p>
    </div>

    <%-- ===================== Confirmaciones y errores (PRG) ===================== --%>
    <c:if test="${not empty param.generado}">
        <div class="instruccion instruccion-exito instruccion-reporte">
            <i class="bi bi-check-circle"></i>
            <div>
                <div class="instruccion-titulo">Reporte generado</div>
                <p>Abre el formato del reporte, guárdalo como PDF, fírmalo y súbelo para poder enviarlo a Estadías.</p>
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
                        <c:when test="${param.error == 'sinformulario'}">
                            Primero completa el formulario del reporte: sin los resultados capturados no se puede enviar.
                        </c:when>
                        <c:when test="${param.error == 'sinfirmado'}">
                            Primero sube el PDF del reporte firmado: sin él no se puede enviar a Estadías.
                        </c:when>
                        <c:when test="${param.error == 'yaenviado'}">
                            Este reporte ya se había enviado a Estadías, así que no se envió otra vez.
                        </c:when>
                        <c:when test="${param.error == 'sinmotivo'}">
                            Para rechazar un reporte es obligatorio escribir el motivo, porque es lo que se le notifica al docente.
                        </c:when>
                        <c:when test="${param.error == 'yaevaluado'}">
                            Este reporte ya fue evaluado por alguien más: recarga la página para ver en qué estado quedó.
                        </c:when>
                        <c:otherwise>
                            Ocurrió un problema al guardar en la base de datos y no se registró ningún cambio. Intenta de nuevo.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </c:if>

    <%-- ===================== Banner de estado (según rol) =====================
         Igual que en la solicitud: si algo fue rechazado, eso es lo primero
         que se lee, con el motivo a la vista y qué hay que hacer. --%>
    <c:choose>
        <c:when test="${estado == 'Rechazado'}">
            <div class="instruccion instruccion-rechazo instruccion-reporte">
                <i class="bi bi-x-circle"></i>
                <div>
                    <div class="instruccion-titulo">Reporte rechazado por el área de Estadías</div>
                    <p>
                        <c:choose>
                            <c:when test="${not empty r.motivo}"><strong>Motivo:</strong> <c:out value="${r.motivo}"/></c:when>
                            <c:otherwise>No se registró un motivo.</c:otherwise>
                        </c:choose>
                    </p>
                    <c:if test="${esDueno && empty param.editar}">
                        <p>Corrige lo señalado con <strong>Editar formulario</strong>, firma nuevamente el formato y envía el reporte a revisión.</p>
                    </c:if>
                </div>
            </div>
        </c:when>
        <c:when test="${estado == 'Pendiente' && !esDueno}">
            <div class="instruccion instruccion-info instruccion-reporte">
                <i class="bi bi-clock"></i>
                <div>
                    <div class="instruccion-titulo">Reporte pendiente de entrega</div>
                    <p>La fecha de la visita es el ${r.fecha}</p>
                    <p>
                        <a href="${pageContext.request.contextPath}/detalle?id=${r.idSolicitud}">
                            Visita de: <c:out value="${r.nombreSolicitante}"/>
                        </a>
                    </p>
                </div>
            </div>
        </c:when>
        <c:when test="${estado == 'Completado' && esDueno}">
            <div class="instruccion instruccion-info instruccion-reporte">
                <i class="bi bi-hourglass-split"></i>
                <div>
                    <div class="instruccion-titulo">Reporte enviado a revisión</div>
                    <p>El área de Estadías evaluará el reporte y se notificará la decisión por correo.</p>
                </div>
            </div>
        </c:when>
        <c:when test="${estado == 'Completado' && !esDueno}">
            <div class="instruccion instruccion-accion instruccion-reporte">
                <i class="bi bi-clipboard-check"></i>
                <div>
                    <div class="instruccion-titulo">Reporte pendiente de revisión</div>
                    <p>Revisa los resultados y las evidencias, y aprueba o rechaza el reporte al final de la página.</p>
                    <p class="instruccion-detalle">Enviado por: <c:out value="${r.nombreSolicitante}"/></p>
                </div>
            </div>
        </c:when>
        <c:when test="${estado == 'Aprobado'}">
            <div class="instruccion instruccion-exito instruccion-reporte">
                <i class="bi bi-check-circle"></i>
                <div>
                    <div class="instruccion-titulo">Reporte aprobado por el área de Estadías</div>
                    <p>La visita y su reporte quedaron cerrados. Se pueden consultar en el
                        <a href="${pageContext.request.contextPath}/historico">Histórico</a>.</p>
                </div>
            </div>
        </c:when>
    </c:choose>

    <%-- ===================== Card resumen de la solicitud ===================== --%>
    <div class="detalle-card card-reporte">
        <div class="resumen-top">
            <div>
                <h3 class="resumen-titulo"><c:out value="${r.nombreEmpresaActividad}"/></h3>
                <div class="resumen-meta">
                    <span><i class="bi bi-pin-map"></i><c:out value="${empty r.lugarDireccion ? 'Sin dirección' : r.lugarDireccion}"/></span>
                    <span><i class="bi bi-calendar-event"></i>Visita: ${r.fecha}</span>
                    <c:if test="${!esDueno}">
                        <span><i class="bi bi-person"></i><c:out value="${r.nombreSolicitante}"/></span>
                    </c:if>
                </div>
            </div>
            <span class="badge-estado estado-${r.claseEstado}">${r.estadoLegible}</span>
        </div>
        <%-- Consulta, no acción: va en contorno para no competir con lo que
             sí toca hacer en esta página --%>
        <a class="btn-descargar btn-contorno btn-resumen-solicitud"
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
                <%-- id y action viajan en la URL, no como campos ocultos: si una
                     imagen pasa del tope, el servidor no puede leer el cuerpo y
                     solo le queda la URL para saber a dónde devolver el aviso. --%>
                <form method="POST" enctype="multipart/form-data" id="form-reporte"
                      action="${pageContext.request.contextPath}/reporte?id=${r.idReporte}&action=generar">

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
            <%-- El botón "Generar reporte" está en la barra final de la página:
                 esta card es la última, así que ahí queda igual de pegado a
                 ella y además alineado con el "Volver" --%>
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
                <%-- Mismo botón de editar (contorno azul) que en el detalle de la solicitud --%>
                <a class="btn-editar-datos btn-resumen-solicitud"
                   href="${pageContext.request.contextPath}/reporte?id=${r.idReporte}&editar=1"
                   title="Al editar deberás volver a firmar y subir el formato">
                    <i class="bi bi-pencil"></i> Editar formulario
                </a>
            </div>

            <div class="detalle-card">
                <h6>Firmar y enviar el reporte</h6>

                <div class="separador-archivos">Generado por el sistema</div>
                <div class="archivo-row">
                    <span class="archivo-pill">
                        <i class="bi bi-file-earmark-pdf"></i>
                        <span>REPORTE DE VISITA<small>Formato del reporte</small></span>
                    </span>
                    <div class="archivo-acciones">
                        <a class="btn-descargar" target="_blank"
                           href="${pageContext.request.contextPath}/documento?gen=reporte&reporte=${r.idReporte}">
                            <i class="bi bi-box-arrow-up-right"></i> Abrir formato
                        </a>
                    </div>
                </div>

                <%-- Con el firmado ya cargado se ve el archivo, no un recuadro
                     de carga vacío que se lee como "todavía falta subir algo".
                     "Ver" lo abre en otra pestaña para comprobar que el PDF
                     que se subió es el correcto. --%>
                <c:if test="${not empty documentos}">
                    <div class="separador-archivos">Subidos por ti</div>
                    <c:forEach var="d" items="${documentos}">
                        <div class="archivo-row">
                            <span class="archivo-pill archivo-pill--subido">
                                <i class="bi bi-file-earmark-check"></i>
                                <span>${fn:toUpperCase(d.nombreTipo)}<small>${d.tamanoLegible} · ${d.fechaCarga}</small></span>
                            </span>
                            <div class="archivo-acciones">
                                <a class="btn-descargar" download
                                   href="${pageContext.request.contextPath}/documento?id=${d.idDocumento}">
                                    <i class="bi bi-download"></i> Descargar
                                </a>
                                <a class="btn-ver" target="_blank"
                                   title="Abre el archivo en otra pestaña para revisar que sea el correcto"
                                   href="${pageContext.request.contextPath}/documento?ver=${d.idDocumento}">
                                    <i class="bi bi-eye"></i> Ver
                                </a>
                                <%-- "Reemplazar", no "Volver a cargar": convivía con
                                     "Volver a reportes" y los dos "Volver" hacían
                                     cosas de categorías distintas --%>
                                <button type="button" class="btn-recargar" data-abre-carga="carga-reporte-firmado">
                                    <i class="bi bi-arrow-repeat"></i> Reemplazar
                                </button>
                            </div>
                        </div>
                    </c:forEach>
                </c:if>

                <%-- Igual que arriba: los identificadores van en la URL para que
                     sobrevivan si el PDF pasa del tope y no se puede leer el cuerpo. --%>
                <form id="carga-reporte-firmado" method="POST" class="form-carga" ${existeFirmado ? 'hidden' : ''}
                      action="${pageContext.request.contextPath}/documento?action=reporteFirmado&reporte=${r.idReporte}"
                      enctype="multipart/form-data">
                    <div class="separador-firmar">${existeFirmado ? 'Reemplazar el reporte firmado' : 'Carga del reporte firmado'}</div>
                    <div class="zona-carga">
                        <i class="bi bi-cloud-arrow-up" style="font-size: 1.6rem; color: var(--color-texto-tenue);"></i>
                        <p>${existeFirmado ? 'Selecciona el nuevo PDF; reemplazará al archivo actual' : 'Sube el reporte de visita firmado'}</p>
                        <small>Máximo 10 MB · solo PDF</small>
                        <input type="file" name="archivo" class="form-control" accept="application/pdf" required>
                        <div class="aviso-seleccion">Archivo seleccionado, pero aún no se sube: da click en <strong>${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}</strong>.</div>
                        <button type="submit" class="btn-subir">
                            <i class="bi bi-upload"></i> ${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}
                        </button>
                    </div>
                </form>
                <%-- El botón que envía este form está en la barra final de la
                     página; la confirmación la pinta js/modales.js --%>
                <form action="${pageContext.request.contextPath}/reporte" method="POST" id="form-enviar-reporte"
                      data-confirmar="El reporte pasa al área de Estadías para su revisión."
                      data-confirmar-titulo="Enviar reporte a Estadías"
                      data-confirmar-detalle="Ya no podrás editar el reporte ni reemplazar el archivo firmado."
                      data-confirmar-tipo="aviso"
                      data-confirmar-ok="Sí, enviar">
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
                <%-- Los archivos van antes que el contenido del reporte, igual
                     que en el detalle de la solicitud: es lo que se viene a
                     revisar y no tiene por qué estar hasta el final --%>
                <div class="detalle-card">
                    <h6>Archivos</h6>
                    <div class="separador-archivos">Generado por el sistema</div>
                    <div class="archivo-row">
                        <span class="archivo-pill">
                            <i class="bi bi-file-earmark-text"></i>
                            <span>REPORTE DE VISITA<small>Generado con los datos capturados</small></span>
                        </span>
                        <div class="archivo-acciones">
                            <a class="btn-descargar" target="_blank"
                               href="${pageContext.request.contextPath}/documento?gen=reporte&reporte=${r.idReporte}">
                                <i class="bi bi-box-arrow-up-right"></i> Abrir formato
                            </a>
                        </div>
                    </div>
                    <c:if test="${not empty documentos}">
                        <div class="separador-archivos">${esDueno ? 'Subidos por ti' : 'Subidos por el docente'}</div>
                        <c:forEach var="d" items="${documentos}">
                            <div class="archivo-row">
                                <span class="archivo-pill archivo-pill--subido">
                                    <i class="bi bi-file-earmark-check"></i>
                                    <span>${fn:toUpperCase(d.nombreTipo)}<small>${d.tamanoLegible} · ${d.fechaCarga}</small></span>
                                </span>
                                <div class="archivo-acciones">
                                    <a class="btn-descargar" download
                                       href="${pageContext.request.contextPath}/documento?id=${d.idDocumento}">
                                        <i class="bi bi-download"></i> Descargar
                                    </a>
                                    <a class="btn-ver" target="_blank"
                                       title="Abre el archivo en otra pestaña para revisarlo sin descargarlo"
                                       href="${pageContext.request.contextPath}/documento?ver=${d.idDocumento}">
                                        <i class="bi bi-eye"></i> Ver
                                    </a>
                                </div>
                            </div>
                        </c:forEach>
                    </c:if>
                </div>

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

                <%-- Card evaluar: solo Estadías/Admin con el reporte enviado --%>
                <c:if test="${estado == 'Completado' && !esDueno}">
                    <div class="detalle-card">
                        <h6>Evaluar reporte</h6>
                        <form action="${pageContext.request.contextPath}/reporte" method="POST" id="form-evaluar-reporte">
                            <input type="hidden" name="id" value="${r.idReporte}">
                            <label class="form-label" for="motivo-reporte">Motivo</label>
                            <textarea name="motivo" id="motivo-reporte" class="form-control" rows="3"
                                      placeholder="Detalles de la decisión"></textarea>
                            <%-- Mismas confirmaciones que al evaluar la solicitud:
                                 el rechazo exige motivo y se ve en rojo --%>
                            <div class="acciones-evaluar">
                                <button type="submit" name="action" value="rechazar" class="btn-rechazar"
                                        data-confirmar="El docente será notificado del rechazo y del motivo que escribiste."
                                        data-confirmar-titulo="Rechazar reporte"
                                        data-confirmar-tipo="peligro"
                                        data-confirmar-ok="Sí, rechazar"
                                        data-confirmar-requiere="#motivo-reporte"
                                        data-confirmar-requiere-titulo="Falta el motivo"
                                        data-confirmar-requiere-mensaje="Escribe el motivo del rechazo: es lo que verá el docente para saber qué corregir.">
                                    <i class="bi bi-x-lg"></i> Rechazar reporte
                                </button>
                                <button type="submit" name="action" value="aprobar" class="btn-aprobar"
                                        data-confirmar="El docente será notificado y el reporte pasará al histórico."
                                        data-confirmar-titulo="Aprobar reporte"
                                        data-confirmar-tipo="exito"
                                        data-confirmar-ok="Sí, aprobar">
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
                        <%-- Mismo botón de editar (contorno azul) que en el detalle de la solicitud --%>
                        <a class="btn-editar-datos"
                           href="${pageContext.request.contextPath}/reporte?id=${r.idReporte}&editar=1">
                            <i class="bi bi-pencil"></i> Editar formulario
                        </a>
                    </div>
                </c:if>
            </c:if>
        </c:otherwise>
    </c:choose>

    <%-- ===================== Barra final: Volver / acción principal =====================
         Los dos van en la misma fila (Volver a la izquierda, la acción a la
         derecha). Antes la acción colgaba de su card en una barra aparte y,
         como esa card es la última de la página, quedaban dos renglones de
         botones pegados y alineados a lados opuestos. --%>
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
                    <i class="bi bi-send"></i> Enviar reporte a Estadías
                </button>
            </c:when>
        </c:choose>
    </div>

    <script src="${pageContext.request.contextPath}/js/carga-archivo.js"></script>
</main>

<%@ include file="layout/footer.jsp" %>
