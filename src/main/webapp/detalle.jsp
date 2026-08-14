<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% request.setAttribute("pageTitle", "Detalles de la visita"); %>
<%-- Una solicitud terminada (rechazada o completada) ya no vive en Solicitudes
     sino en el Histórico: el menú y el botón de volver apuntan allá --%>
<c:set var="esTerminada" scope="request"
       value="${solicitud.nombreEstado == 'Rechazada' || solicitud.nombreEstado == 'Completada'}"/>
<c:set var="activeNav" scope="request" value="${esTerminada ? 'historico' : 'solicitudes'}"/>
<% request.setAttribute("divisiones", com.example.demo.model.CatalogoAcademico.DIVISIONES); %>
<% request.setAttribute("nombresDivision", com.example.demo.model.CatalogoAcademico.getNombres()); %>
<%@ include file="layout/header.jsp" %>
<%@ include file="layout/sidebar.jsp" %>

<main id="main-content">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/stepper.css">
    <%-- form.css trae los campos (Motivo, carga de archivos) y el resumen por
         división, para que se vean igual aquí que en el formulario --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/detalle.css">

    <c:set var="s" value="${solicitud}"/>
    <c:set var="esDocente" value="${sessionScope.rol == null || sessionScope.rol == 'Docente'}"/>
    <c:set var="esPropia" value="${sessionScope.idUsuario == s.idUsuarioSolicitante}"/>
    <c:set var="estado" value="${s.nombreEstado}"/>

    <%-- El botón dice a dónde lleva: "Volver" a secas se entendía como
         "regresar al paso anterior" y aquí sale de la solicitud. Una
         solicitud terminada ya no aparece en Solicitudes: se vuelve al
         Histórico, que es donde quedó. --%>
    <c:choose>
        <c:when test="${esTerminada}">
            <c:set var="volverUrl" value="${pageContext.request.contextPath}/historico"/>
            <c:set var="volverTexto" value="Volver al histórico"/>
        </c:when>
        <c:otherwise>
            <c:set var="volverUrl" value="${pageContext.request.contextPath}/solicitud"/>
            <c:set var="volverTexto" value="Volver a solicitudes"/>
        </c:otherwise>
    </c:choose>

    <%-- Volver también aquí arriba: quien entró solo a consultar no tiene por
         qué recorrer la página entera para encontrar la salida --%>
    <a href="${volverUrl}" class="volver-arriba">
        <i class="bi bi-arrow-left"></i> ${volverTexto}
    </a>

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

    <%-- Errores de la subida de archivos (RN-07) y de las acciones de la página
         (enviar / aprobar / rechazar). El título cambia según de cuál se trate. --%>
    <c:if test="${not empty param.error}">
        <c:set var="errorDeArchivo"
               value="${param.error == 'tipo' || param.error == 'tamano' || param.error == 'vacio'}"/>
        <div class="instruccion instruccion-rechazo" style="margin-top: 1rem;">
            <i class="bi bi-exclamation-triangle"></i>
            <div>
                <div class="instruccion-titulo">
                    ${errorDeArchivo ? 'No se pudo subir el archivo' : 'No se pudo completar la operación'}
                </div>
                <p>
                    <c:choose>
                        <c:when test="${param.error == 'tipo'}">Solo se permiten archivos PDF.</c:when>
                        <c:when test="${param.error == 'tamano'}">El archivo supera el tamaño máximo de 10 MB.</c:when>
                        <c:when test="${param.error == 'vacio'}">Selecciona un archivo antes de subir.</c:when>
                        <c:when test="${param.error == 'sinfirmado'}">
                            Primero sube el formato FO-UTEZ-EST-08 firmado: sin él la solicitud no se puede enviar a revisión.
                        </c:when>
                        <c:when test="${param.error == 'enviada'}">
                            Esta solicitud ya se había enviado a Estadías, así que no se envió otra vez.
                        </c:when>
                        <c:when test="${param.error == 'sinmotivo'}">
                            Para rechazar una solicitud es obligatorio escribir el motivo, porque es lo que se le notifica al docente.
                        </c:when>
                        <c:when test="${param.error == 'yaevaluada'}">
                            Esta solicitud ya fue evaluada por alguien más: recarga la página para ver en qué estado quedó.
                        </c:when>
                        <c:otherwise>
                            Ocurrió un problema al guardar en la base de datos y no se registró ningún cambio. Intenta de nuevo.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </c:if>


    <div class="detalle-card" style="margin-top: 1rem;">
        <div class="resumen-top">
            <div>
                <h3 class="resumen-titulo"><c:out value="${s.nombreEmpresaActividad}"/></h3>
                <div class="resumen-meta">
                    <span><i class="bi bi-pin-map"></i><c:out value="${empty s.lugarDireccion ? 'Sin dirección' : s.lugarDireccion}"/></span>
                    <c:if test="${not empty s.fechaInicio}">
                        <span><i class="bi bi-calendar-event"></i>${s.fechaInicio}</span>
                    </c:if>
                    <span><i class="bi bi-clock-history"></i>Solicitada el ${s.fechaCreacion}</span>
                </div>
            </div>
            <span class="badge-estado estado-${s.claseEstado}">${s.estadoLegible}</span>
        </div>

        <%-- Mini card de instrucciones: qué falta y a quién le toca. Cuando algo
             fue rechazado manda el rechazo, nunca el "sube tal archivo": es lo
             primero que el docente tiene que leer, y con el motivo a la vista. --%>
        <c:set var="estadoReporte" value="${s.estadoReporte}"/>
        <c:choose>
            <c:when test="${estado == 'Pendiente' && esDocente && existeFirmado}">
                <div class="instruccion instruccion-accion">
                    <i class="bi bi-send"></i>
                    <div>
                        <div class="instruccion-titulo">Formato firmado cargado: falta enviar la solicitud</div>
                        <p>El FO-UTEZ-EST-08 firmado ya está en el sistema. Verifica que los datos sean correctos y presiona <strong>Enviar solicitud a Estadías</strong>, debajo de la sección <strong>Archivos</strong>. Mientras la solicitud no se envíe, los datos todavía se pueden editar.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Pendiente' && esDocente}">
                <div class="instruccion instruccion-accion">
                    <i class="bi bi-exclamation-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Sube el formato FO-UTEZ-EST-08 firmado</div>
                        <p>Descarga el formato FO-UTEZ-EST-08 en la sección <strong>Archivos</strong>, fírmalo y súbelo. El botón <strong>Enviar solicitud a Estadías</strong> se habilita hasta que el formato esté cargado. Los datos se pueden editar mientras la solicitud no se envíe.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'En revisión' && esDocente}">
                <div class="instruccion instruccion-info">
                    <i class="bi bi-send-check"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud enviada a revisión</div>
                        <p>El área de Estadías recibió la solicitud. Se notificará por correo cuando haya una decisión.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'En revisión' && !esDocente}">
                <div class="instruccion instruccion-accion">
                    <i class="bi bi-clipboard-check"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud pendiente de revisión</div>
                        <p>Revisa los datos y los archivos de la solicitud, y apruébala o recházala al final de la página.</p>
                        <p class="instruccion-detalle">Enviada por: <c:out value="${s.nombreSolicitante}"/></p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Aprobada' && esDocente}">
                <div class="instruccion instruccion-accion">
                    <i class="bi bi-file-earmark-arrow-up"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud aprobada: falta la carta responsiva</div>
                        <p>Descarga la carta responsiva en la sección <strong>Archivos</strong>, fírmala y súbela. Al subirla, la solicitud se cierra y se genera el reporte de la visita.</p>
                        <c:if test="${not empty s.detallesDecision}">
                            <p class="instruccion-detalle">Comentarios de Estadías: <c:out value="${s.detallesDecision}"/></p>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Aprobada' && !esDocente}">
                <div class="instruccion instruccion-info">
                    <i class="bi bi-clock"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud aprobada: en espera de la carta responsiva</div>
                        <p>El docente <c:out value="${s.nombreSolicitante}"/> debe cargar la carta responsiva con las firmas correspondientes.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Rechazada'}">
                <div class="instruccion instruccion-rechazo">
                    <i class="bi bi-x-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud rechazada por el área de Estadías</div>
                        <p>
                            <c:choose>
                                <c:when test="${not empty s.detallesDecision}"><strong>Motivo:</strong> <c:out value="${s.detallesDecision}"/></c:when>
                                <c:otherwise>No se registró un motivo.</c:otherwise>
                            </c:choose>
                        </p>
                        <c:if test="${esDocente}">
                            <p class="instruccion-detalle">Una solicitud rechazada ya no se puede editar. Si la visita continúa, registra una nueva solicitud considerando el motivo.</p>
                        </c:if>
                    </div>
                </div>
            </c:when>

            <%-- ===== Solicitud cerrada: lo que falta (o no) es el reporte ===== --%>
            <c:when test="${estado == 'Completada' && estadoReporte == 'Rechazado'}">
                <div class="instruccion instruccion-rechazo">
                    <i class="bi bi-x-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Reporte de la visita rechazado por el área de Estadías</div>
                        <p>
                            <c:choose>
                                <c:when test="${not empty reporte.motivo}"><strong>Motivo:</strong> <c:out value="${reporte.motivo}"/></c:when>
                                <c:otherwise>No se registró un motivo.</c:otherwise>
                            </c:choose>
                        </p>
                        <c:if test="${esDocente}">
                            <p>Corrige el reporte y envíalo nuevamente:
                                <a href="${pageContext.request.contextPath}/reporte?id=${s.idReporte}">ir al reporte de esta visita</a>.</p>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Completada' && estadoReporte == 'Completado'}">
                <div class="instruccion instruccion-info">
                    <i class="bi bi-hourglass-split"></i>
                    <div>
                        <div class="instruccion-titulo">Reporte enviado a revisión</div>
                        <p>El área de Estadías está revisando el reporte de la visita. Se notificará por correo cuando haya una decisión.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Completada' && estadoReporte == 'Aprobado'}">
                <div class="instruccion instruccion-exito">
                    <i class="bi bi-check-circle"></i>
                    <div>
                        <div class="instruccion-titulo">Proceso concluido</div>
                        <p>La visita se realizó y su reporte fue aprobado. La solicitud se puede consultar en el <a href="${pageContext.request.contextPath}/historico">Histórico</a>.</p>
                    </div>
                </div>
            </c:when>
            <c:when test="${estado == 'Completada'}">
                <%-- Reporte creado pero sin llenar: es el pendiente que más se
                     confundía, porque el estado en la base dice "Completada" --%>
                <div class="instruccion instruccion-accion">
                    <i class="bi bi-journal-text"></i>
                    <div>
                        <div class="instruccion-titulo">Solicitud cerrada: falta el reporte de la visita</div>
                        <p>
                            <c:choose>
                                <c:when test="${esDocente}">
                                    La solicitud quedó cerrada, pero el proceso concluye hasta entregar el reporte.
                                    Complétalo después de realizar la visita:
                                    <a href="${pageContext.request.contextPath}/reporte?id=${s.idReporte}">ir al reporte de esta visita</a>.
                                </c:when>
                                <c:otherwise>
                                    La solicitud quedó cerrada. Falta que el docente <c:out value="${s.nombreSolicitante}"/> entregue el reporte de la visita.
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:when>
        </c:choose>

        <%-- Línea de tiempo del trámite completo (5 pasos) --%>
        <c:set var="stepperEstado" value="${estado}"/>
        <c:set var="stepperEstadoReporte" value="${estadoReporte}"/>
        <%@ include file="layout/stepper.jsp" %>
    </div>

    <%-- ===================== Card archivos =====================
         Va pegada a la card de instrucciones porque casi todo lo que ahí se
         pide ("descarga el formato", "sube la carta responsiva") se hace
         aquí: si esta card queda hasta el final hay que recorrer los datos
         de la solicitud para encontrar aquello de lo que habla el aviso. --%>
    <div class="detalle-card">
        <h6>Archivos</h6>

        <%-- Documentos que genera el sistema con los datos de la solicitud.
             El FO se muestra siempre: aunque ya esté firmado y subido, si el
             docente quiere rehacer la firma tiene que poder bajarlo otra vez.
             Estos abren su vista imprimible, de ahí se guardan como PDF. --%>
        <div class="separador-archivos">Generados por el sistema</div>
        <div class="archivo-row">
            <span class="archivo-pill">
                <i class="bi bi-file-earmark-pdf"></i>
                <span>FO-UTEZ-EST-08<small>Formato de la solicitud</small></span>
            </span>
            <div class="archivo-acciones">
                <a class="btn-descargar" target="_blank"
                   href="${pageContext.request.contextPath}/documento?gen=fo&solicitud=${s.idSolicitud}">
                    <i class="bi bi-eye"></i> Ver y descargar
                </a>
            </div>
        </div>

        <%-- Documentos generados al aprobar (RF-07) --%>
        <c:if test="${estado == 'Aprobada' || estado == 'Completada'}">
            <div class="archivo-row">
                <span class="archivo-pill">
                    <i class="bi bi-file-earmark-pdf"></i>
                    <span>OFICIO DE VISITA<small>Autorización para el lugar de la visita</small></span>
                </span>
                <div class="archivo-acciones">
                    <a class="btn-descargar" target="_blank"
                       href="${pageContext.request.contextPath}/documento?gen=oficio&solicitud=${s.idSolicitud}">
                        <i class="bi bi-eye"></i> Ver y descargar
                    </a>
                </div>
            </div>
        </c:if>

        <c:if test="${esDocente && esPropia && estado == 'Aprobada'}">
            <div class="archivo-row">
                <span class="archivo-pill">
                    <i class="bi bi-file-earmark-pdf"></i>
                    <span>CARTA RESPONSIVA<small>Requiere firma del docente responsable</small></span>
                </span>
                <div class="archivo-acciones">
                    <a class="btn-descargar" target="_blank"
                       href="${pageContext.request.contextPath}/documento?gen=responsiva&solicitud=${s.idSolicitud}">
                        <i class="bi bi-eye"></i> Ver y descargar
                    </a>
                </div>
            </div>
        </c:if>

        <%-- Archivos subidos por el docente. "Ver" los abre en una pestaña
             aparte: es la forma de comprobar que el PDF que se subió es el
             correcto sin bajarlo y abrirlo a mano. El FO firmado es el único
             que se puede reemplazar (y solo antes de enviar): por eso su fila
             lleva "Volver a cargar", que es lo que abre la zona de carga. --%>
        <c:if test="${not empty documentos}">
            <div class="separador-archivos">${esDocente && esPropia ? 'Subidos por ti' : 'Subidos por el docente'}</div>
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
                        <c:if test="${esDocente && esPropia && estado == 'Pendiente' && d.nombreTipo == tipoFoFirmado}">
                            <button type="button" class="btn-recargar" data-abre-carga="carga-fo-firmado">
                                <i class="bi bi-arrow-repeat"></i> Volver a cargar
                            </button>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </c:if>

        <%-- Zonas de carga: solo el docente dueño y solo en los 2 momentos del
             proceso en los que toca subir algo. Si el archivo ya está cargado
             la zona nace oculta (se ve el archivo, no un recuadro vacío que
             hace pensar que todavía falta subir algo). --%>
        <c:if test="${esDocente && esPropia && estado == 'Pendiente'}">
            <form id="carga-fo-firmado" action="${pageContext.request.contextPath}/documento" method="POST"
                  enctype="multipart/form-data" class="form-carga" ${existeFirmado ? 'hidden' : ''}>
                <input type="hidden" name="action" value="firmado">
                <input type="hidden" name="solicitud" value="${s.idSolicitud}">
                <div class="separador-firmar">${existeFirmado ? 'Reemplazar el formato firmado' : 'Carga del formato firmado'}</div>
                <div class="zona-carga">
                    <i class="bi bi-cloud-arrow-up" style="font-size: 1.6rem; color: var(--color-texto-tenue);"></i>
                    <p>${existeFirmado ? 'Selecciona el nuevo PDF; reemplazará al archivo actual' : 'Sube el formato FO-UTEZ-EST-08 firmado'}</p>
                    <small>Máximo 10 MB · solo PDF</small>
                    <input type="file" name="archivo" class="form-control" accept="application/pdf" required>
                    <div class="aviso-seleccion">Archivo seleccionado, pero aún no se sube: da click en <strong>${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}</strong>.</div>
                    <button type="submit" class="btn-subir">
                        <i class="bi bi-upload"></i> ${existeFirmado ? 'Reemplazar archivo' : 'Subir archivo'}
                    </button>
                </div>
            </form>
        </c:if>

        <c:if test="${esDocente && esPropia && estado == 'Aprobada'}">
            <form action="${pageContext.request.contextPath}/documento" method="POST" enctype="multipart/form-data"
                  class="form-carga"
                  data-confirmar="Al subir la carta responsiva firmada la solicitud se cierra y se genera el reporte de la visita."
                  data-confirmar-titulo="Cerrar la solicitud"
                  data-confirmar-detalle="Después de esto ya no podrás cambiar los documentos de la solicitud."
                  data-confirmar-tipo="aviso"
                  data-confirmar-ok="Sí, subir y cerrar">
                <input type="hidden" name="action" value="responsiva">
                <input type="hidden" name="solicitud" value="${s.idSolicitud}">
                <div class="separador-firmar">Carga de la carta responsiva firmada</div>
                <div class="zona-carga">
                    <i class="bi bi-cloud-arrow-up" style="font-size: 1.6rem; color: var(--color-texto-tenue);"></i>
                    <p>Sube la CARTA RESPONSIVA firmada</p>
                    <small>Máximo 10 MB · solo PDF</small>
                    <input type="file" name="archivo" class="form-control" accept="application/pdf" required>
                    <div class="aviso-seleccion">Archivo seleccionado, pero aún no se sube: da click en <strong>Subir archivo</strong>.</div>
                    <button type="submit" class="btn-subir">
                        <i class="bi bi-upload"></i> Subir archivo
                    </button>
                </div>
            </form>
        </c:if>
    </div>

    <%-- ===================== Acciones del docente: Editar / Enviar =====================
         Van pegadas a la card de Archivos y no al pie de la página: enviar es
         lo que sigue justo después de subir el formato firmado, y ahí abajo
         obligaba a recorrer los datos de la solicitud para llegar al botón. --%>
    <c:if test="${esDocente && esPropia && estado == 'Pendiente'}">
        <div class="acciones-form acciones-form--derecha acciones-tras-card">
            <a href="${pageContext.request.contextPath}/solicitud?action=editar&id=${s.idSolicitud}"
               class="btn-editar-datos" title="Corregir los datos antes de enviar">
                <i class="bi bi-pencil"></i> Editar datos
            </a>
            <form action="${pageContext.request.contextPath}/detalle" method="POST" style="margin: 0;"
                  data-confirmar="La solicitud pasa al área de Estadías para su revisión."
                  data-confirmar-titulo="Enviar solicitud a Estadías"
                  data-confirmar-detalle="Ya no podrás editar los datos ni reemplazar el formato firmado."
                  data-confirmar-tipo="aviso"
                  data-confirmar-ok="Sí, enviar">
                <input type="hidden" name="id" value="${s.idSolicitud}">
                <input type="hidden" name="action" value="enviar">
                <button type="submit" class="btn-enviar-solicitud" ${existeFirmado ? '' : 'disabled'}
                        title="${existeFirmado ? 'Enviar a revisión de Estadías' : 'Primero sube el formato firmado'}">
                    <i class="bi bi-send"></i> Enviar solicitud a Estadías
                </button>
            </form>
        </div>
    </c:if>

    <%-- ===================== Card datos del lugar ===================== --%>
    <div class="detalle-card">
        <h6>Datos del lugar a visitar</h6>
        <div class="datos-grid">
            <div>
                <div class="dato-label">Nombre de la empresa o actividad</div>
                <div class="dato-valor"><c:out value="${s.nombreEmpresaActividad}"/></div>
            </div>
            <div>
                <div class="dato-label">Lugar o dirección</div>
                <div class="dato-valor"><c:out value="${empty s.lugarDireccion ? '—' : s.lugarDireccion}"/></div>
            </div>
            <div>
                <div class="dato-label">Teléfonos del contacto</div>
                <div class="dato-valor"><c:out value="${empty s.telefonoContacto ? '—' : s.telefonoContacto}"/></div>
            </div>
            <div>
                <div class="dato-label">Correo electrónico del contacto</div>
                <div class="dato-valor"><c:out value="${empty s.correoContacto ? '—' : s.correoContacto}"/></div>
            </div>
            <div>
                <div class="dato-label">Fecha de inicio</div>
                <div class="dato-valor">${empty s.fechaInicio ? '—' : s.fechaInicio}</div>
            </div>
            <div class="dato-full">
                <div class="dato-label">Objetivo de la visita</div>
                <div class="dato-valor"><c:out value="${empty s.objetivo ? '—' : s.objetivo}"/></div>
            </div>
        </div>
    </div>

    <%-- ===================== Card datos de los participantes ===================== --%>
    <div class="detalle-card">
        <h6>Datos de los participantes</h6>
        <div class="datos-grid">
            <div>
                <div class="dato-label">Área solicitante</div>
                <div class="dato-valor"><c:out value="${empty s.areaSolicitante ? '—' : s.areaSolicitante}"/></div>
            </div>
            <div>
                <div class="dato-label">Docente responsable de la visita</div>
                <%-- Las solicitudes viejas no tienen el campo: se muestra quien la creó --%>
                <div class="dato-valor"><c:out value="${empty s.docenteResponsable ? s.nombreSolicitante : s.docenteResponsable}"/></div>
            </div>
            <div>
                <div class="dato-label">Celular del responsable</div>
                <div class="dato-valor"><c:out value="${empty s.celularResponsable ? '—' : s.celularResponsable}"/></div>
            </div>
        </div>

        <div class="dato-label" style="margin-top: 14px;">Docentes acompañantes</div>
        <c:choose>
            <c:when test="${not empty s.docentesAcompanantes}">
                <div>
                    <c:forEach var="d" items="${s.docentesAcompanantes}">
                        <span class="chip-asignatura"><c:out value="${d.nombre}"/></span>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="dato-valor">—</div>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty s.programas}">
            <div class="dato-label" style="margin-top: 14px;">Grupos que participan</div>
            <div class="tabla-scroll">
                <table class="tabla-programas">
                    <thead>
                    <tr>
                        <th>División</th>
                        <th>Programa educativo</th>
                        <th>Cuatrimestre</th>
                        <th>Grupo</th>
                        <th>Estudiantes</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${s.programas}">
                        <tr>
                            <td title="${fn:escapeXml(nombresDivision[p.division])}"><c:out value="${p.divisionMostrable}"/></td>
                            <td><c:out value="${p.programa}"/></td>
                            <td>${p.cuatrimestre}°</td>
                            <td><c:out value="${empty p.grupo ? '—' : p.grupo}"/></td>
                            <td>${p.noEstudiantes}</td>
                        </tr>
                    </c:forEach>
                    <tr class="fila-total">
                        <td colspan="4">Total de estudiantes</td>
                        <td>${s.totalEstudiantes}</td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <%-- Desglose por división: se calcula con los grupos de arriba --%>
            <div class="dato-label" style="margin-top: 14px;">Número de estudiantes por división académica</div>
            <div class="tabla-scroll">
                <table class="tabla-programas">
                    <thead>
                    <tr>
                        <c:forEach var="division" items="${divisiones}">
                            <th title="${fn:escapeXml(nombresDivision[division])}"><c:out value="${division}"/></th>
                        </c:forEach>
                        <th>Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <c:forEach var="division" items="${divisiones}">
                            <td>${empty s.estudiantesPorDivision[division] ? 0 : s.estudiantesPorDivision[division]}</td>
                        </c:forEach>
                        <td>${s.totalPorDivision}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </c:if>

        <c:if test="${not empty s.asignaturas}">
            <div class="dato-label" style="margin-top: 14px;">Asignaturas que se reforzarán</div>
            <div>
                <c:forEach var="a" items="${s.asignaturas}">
                    <span class="chip-asignatura"><c:out value="${a}"/></span>
                </c:forEach>
            </div>
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
                <%-- Cada botón trae su propia confirmación (js/modales.js):
                     el rechazo además exige que el motivo esté capturado --%>
                <div class="acciones-evaluar">
                    <button type="submit" name="action" value="rechazar" class="btn-rechazar"
                            data-confirmar="El docente será notificado del rechazo y del motivo que escribiste."
                            data-confirmar-titulo="Rechazar solicitud"
                            data-confirmar-tipo="peligro"
                            data-confirmar-ok="Sí, rechazar"
                            data-confirmar-requiere="#motivo"
                            data-confirmar-requiere-titulo="Falta el motivo"
                            data-confirmar-requiere-mensaje="Escribe el motivo del rechazo: es lo que verá el docente para saber qué corregir.">
                        <i class="bi bi-x-lg"></i> Rechazar solicitud
                    </button>
                    <button type="submit" name="action" value="aprobar" class="btn-aprobar"
                            data-confirmar="El docente será notificado y podrá continuar con la carta responsiva."
                            data-confirmar-titulo="Aprobar solicitud"
                            data-confirmar-tipo="exito"
                            data-confirmar-ok="Sí, aprobar">
                        <i class="bi bi-check-lg"></i> Aprobar solicitud
                    </button>
                </div>
            </form>
        </div>
    </c:if>

    <%-- ===================== Barra final: Volver ===================== --%>
    <div class="acciones-form">
        <a href="${volverUrl}" class="btn-volver-detalle">
            <i class="bi bi-arrow-left"></i> ${volverTexto}
        </a>
    </div>

    <script src="${pageContext.request.contextPath}/js/carga-archivo.js"></script>
</main>

<%@ include file="layout/footer.jsp" %>
