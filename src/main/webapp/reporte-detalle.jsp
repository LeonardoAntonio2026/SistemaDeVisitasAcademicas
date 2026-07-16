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

    <c:set var="r" value="${reporte}"/>
    <c:set var="estado" value="${r.nombreEstado}"/>

    <div class="superior">
        <h2>Reporte de visita</h2>
        <p>${r.nombreEmpresaActividad}</p>
    </div>

    <%-- Confirmaciones y errores --%>
    <c:if test="${not empty param.guardado}">
        <div class="instruccion instruccion-exito" style="margin-top: 1rem;">
            <i class="bi bi-check-circle"></i>
            <div>
                <div class="instruccion-titulo">Reporte guardado correctamente</div>
                <p>Tus resultados, observaciones e imágenes ya quedaron registrados.</p>
            </div>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="instruccion instruccion-rechazo" style="margin-top: 1rem;">
            <i class="bi bi-exclamation-triangle"></i>
            <div>
                <div class="instruccion-titulo">No se pudo guardar el reporte</div>
                <p>
                    <c:choose>
                        <c:when test="${param.error == 'tipo'}">Las imágenes deben ser JPG o PNG.</c:when>
                        <c:when test="${param.error == 'tamano'}">Cada imagen debe pesar máximo 5 MB.</c:when>
                        <c:when test="${param.error == 'maximo'}">Un reporte admite máximo 3 imágenes.</c:when>
                        <c:when test="${param.error == 'vacio'}">Escribe los resultados de la visita.</c:when>
                        <c:otherwise>Ocurrió un problema al guardar. Intenta de nuevo.</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </c:if>

    <%-- ===================== Card resumen de la solicitud ===================== --%>
    <div class="detalle-card" style="margin-top: 1rem;">
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
        <a class="btn-descargar" style="margin-top: 10px; display: inline-flex;"
           href="${pageContext.request.contextPath}/detalle?id=${r.idSolicitud}">
            <i class="bi bi-file-earmark-text"></i> Ver solicitud
        </a>
    </div>

    <c:choose>
        <%-- ============================================================
             Caso 1: sigue Pendiente y quien mira NO es el dueño (Estadías
             o Admin viendo un reporte que el docente aún no llena).
             Solo se muestran los datos de la solicitud (ya arriba) y el
             botón "Ver solicitud" - nada más, tal como pide el prototipo.
             ============================================================ --%>
        <c:when test="${estado == 'Pendiente' && !esDueno}">
            <div class="detalle-card">
                <div class="instruccion instruccion-info">
                    <i class="bi bi-clock"></i>
                    <div>
                        <div class="instruccion-titulo">El docente aún no completa este reporte</div>
                        <p>Cuando el docente llene el formulario de resultados, aquí aparecerá la información completa.</p>
                    </div>
                </div>
            </div>
        </c:when>

        <%-- ============================================================
             Caso 2: sigue Pendiente y quien mira SÍ es el docente dueño.
             Formulario completo: resultados, observaciones e imágenes.
             ============================================================ --%>
        <c:when test="${estado == 'Pendiente' && esDueno}">
            <div class="detalle-card">
                <h6>Completar reporte de la visita</h6>
                <form action="${pageContext.request.contextPath}/reporte" method="POST"
                      enctype="multipart/form-data" id="form-reporte">
                    <input type="hidden" name="id" value="${r.idReporte}">

                    <label class="form-label" for="resultados">Resultados de la visita</label>
                    <textarea name="resultados" id="resultados" class="form-control" rows="4" required
                              placeholder="Describe lo que se logró en la visita"></textarea>

                    <label class="form-label" for="observaciones" style="margin-top: 12px;">Observaciones</label>
                    <textarea name="observaciones" id="observaciones" class="form-control" rows="3"
                              placeholder="Observaciones adicionales (opcional)"></textarea>

                    <div class="separador-firmar" style="margin-top: 16px;">Imágenes de la visita (máx. 3, JPG/PNG, 5 MB c/u)</div>

                    <div id="galeria-previa" class="galeria-previa"></div>

                    <button type="button" id="btn-agregar-imagen" class="btn-descargar" style="margin-top: 8px;">
                        <i class="bi bi-plus-lg"></i> Agregar imagen
                    </button>
                    <input type="file" id="input-imagen-oculto" accept="image/jpeg,image/png" style="display:none;">

                    <div class="acciones-form" style="padding: 0; border: none; margin-top: 20px;">
                        <button type="submit" class="btn-subir">Guardar reporte</button>
                    </div>
                </form>
            </div>

            <style>
                .galeria-previa { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 10px; }
                .galeria-item { position: relative; width: 110px; height: 110px; border-radius: 8px; overflow: hidden; border: 1px solid #E0E0E0; }
                .galeria-item img { width: 100%; height: 100%; object-fit: cover; display: block; }
                .galeria-item .quitar-imagen {
                    position: absolute; top: 4px; right: 4px; width: 22px; height: 22px; border-radius: 50%;
                    background: rgba(0,0,0,0.6); color: #fff; border: none; cursor: pointer;
                    display: flex; align-items: center; justify-content: center; font-size: 14px; line-height: 1;
                }
            </style>

            <script>
                (function () {
                    const MAX_IMAGENES = 3;
                    const MAX_BYTES = 5 * 1024 * 1024;
                    let archivos = []; // acumula File objects seleccionados

                    const galeria = document.getElementById('galeria-previa');
                    const btnAgregar = document.getElementById('btn-agregar-imagen');
                    const inputOculto = document.getElementById('input-imagen-oculto');
                    const form = document.getElementById('form-reporte');

                    btnAgregar.addEventListener('click', function () {
                        if (archivos.length >= MAX_IMAGENES) {
                            alert('Ya seleccionaste el máximo de 3 imágenes.');
                            return;
                        }
                        inputOculto.click();
                    });

                    inputOculto.addEventListener('change', function () {
                        const file = inputOculto.files[0];
                        inputOculto.value = '';
                        if (!file) return;

                        if (!['image/jpeg', 'image/png'].includes(file.type)) {
                            alert('Solo se permiten imágenes JPG o PNG.');
                            return;
                        }
                        if (file.size > MAX_BYTES) {
                            alert('Cada imagen debe pesar máximo 5 MB.');
                            return;
                        }
                        if (archivos.length >= MAX_IMAGENES) {
                            alert('Ya seleccionaste el máximo de 3 imágenes.');
                            return;
                        }

                        archivos.push(file);
                        renderGaleria();
                    });

                    function renderGaleria() {
                        galeria.innerHTML = '';
                        archivos.forEach(function (file, idx) {
                            const item = document.createElement('div');
                            item.className = 'galeria-item';

                            const img = document.createElement('img');
                            img.src = URL.createObjectURL(file);
                            item.appendChild(img);

                            const btnQuitar = document.createElement('button');
                            btnQuitar.type = 'button';
                            btnQuitar.className = 'quitar-imagen';
                            btnQuitar.innerHTML = '&times;';
                            btnQuitar.addEventListener('click', function () {
                                archivos.splice(idx, 1);
                                renderGaleria();
                            });
                            item.appendChild(btnQuitar);

                            galeria.appendChild(item);
                        });
                        btnAgregar.disabled = archivos.length >= MAX_IMAGENES;
                    }

                    // Al enviar, sincronizamos "archivos" a un input real con name="imagenes"
                    form.addEventListener('submit', function () {
                        const dt = new DataTransfer();
                        archivos.forEach(function (file) { dt.items.add(file); });

                        let inputFinal = document.getElementById('input-imagenes-final');
                        if (!inputFinal) {
                            inputFinal = document.createElement('input');
                            inputFinal.type = 'file';
                            inputFinal.id = 'input-imagenes-final';
                            inputFinal.name = 'imagenes';
                            inputFinal.multiple = true;
                            inputFinal.style.display = 'none';
                            form.appendChild(inputFinal);
                        }
                        inputFinal.files = dt.files;
                    });
                })();
            </script>
        </c:when>

        <%-- ============================================================
             Caso 3: reporte ya completado (Completado u otro estado
             posterior). Vista de solo lectura para cualquiera con acceso.
             ============================================================ --%>
        <c:otherwise>
            <div class="detalle-card">
                <h6>Resultados de la visita</h6>
                <div class="datos-grid">
                    <div class="dato-full">
                        <div class="dato-label">Resultados</div>
                        <div class="dato-valor">${r.resultados}</div>
                    </div>
                    <div class="dato-full">
                        <div class="dato-label">Observaciones</div>
                        <div class="dato-valor">${empty r.observaciones ? '—' : r.observaciones}</div>
                    </div>
                </div>
            </div>

            <c:if test="${not empty imagenes}">
                <div class="detalle-card">
                    <h6>Imágenes</h6>
                    <div class="galeria-previa">
                        <c:forEach var="img" items="${imagenes}">
                            <div class="galeria-item" style="width:160px; height:160px;">
                                <img src="${pageContext.request.contextPath}/reporte?imagen=${img.idImagen}"
                                     alt="Imagen del reporte">
                            </div>
                        </c:forEach>
                    </div>
                </div>
                <style>
                    .galeria-previa { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 10px; }
                    .galeria-item img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; border: 1px solid #E0E0E0; display:block; }
                </style>
            </c:if>
        </c:otherwise>
    </c:choose>

    <div class="acciones-form">
        <a href="${pageContext.request.contextPath}/reportes" class="btn-volver-detalle">
            <i class="bi bi-arrow-left"></i> Volver a reportes
        </a>
    </div>
</main>

<%@ include file="layout/footer.jsp" %>
