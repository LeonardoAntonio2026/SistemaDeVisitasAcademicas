<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Vista imprimible de los documentos generados a partir de la solicitud
     o del reporte de visita. El usuario la imprime o la guarda como PDF
     (Ctrl+P), la firma y la sube. tipoFormato: fo | oficio | responsiva | reporte --%>
<c:set var="s" value="${solicitud}"/>
<c:set var="r" value="${reporte}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>
        <c:choose>
            <c:when test="${tipoFormato == 'fo'}">FO-UTEZ-EST-08 - Solicitud de visita académica</c:when>
            <c:when test="${tipoFormato == 'oficio'}">Oficio de visita académica</c:when>
            <c:when test="${tipoFormato == 'reporte'}">Reporte de visita académica</c:when>
            <c:otherwise>Carta responsiva</c:otherwise>
        </c:choose>
    </title>
    <style>
        body {
            font-family: 'Times New Roman', Georgia, serif;
            background: #E8E8ED;
            margin: 0;
            color: #1c1c1e;
        }
        .toolbar {
            background: #183052;
            color: #fff;
            padding: 12px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-family: Arial, sans-serif;
        }
        .toolbar a, .toolbar button {
            background: #ffffff;
            color: #183052;
            border: none;
            border-radius: 8px;
            padding: 8px 18px;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
        }
        .hoja {
            background: #fff;
            width: 216mm;
            max-width: 95%;
            margin: 24px auto;
            padding: 22mm 20mm;
            box-shadow: 0 2px 12px rgba(0,0,0,0.15);
            box-sizing: border-box;
        }
        .encabezado {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 3px solid #183052;
            padding-bottom: 10px;
            margin-bottom: 24px;
            font-family: Arial, sans-serif;
        }
        .encabezado h1 { font-size: 15px; margin: 0; color: #183052; }
        .encabezado .codigo { font-size: 12px; color: #555; }
        h2.titulo-doc { text-align: center; font-size: 17px; margin: 0 0 22px; text-transform: uppercase; }
        table.campos { width: 100%; border-collapse: collapse; font-size: 13.5px; margin-bottom: 18px; }
        table.campos th, table.campos td { border: 1px solid #444; padding: 7px 10px; text-align: left; }
        table.campos th { background: #EFEFF4; width: 34%; font-weight: 700; }
        p.parrafo { font-size: 14px; line-height: 1.7; text-align: justify; }
        .fotos-reporte { display: flex; flex-wrap: wrap; gap: 4%; margin: 10px 0 18px; }
        .fotos-reporte img { width: 48%; max-height: 75mm; object-fit: cover; border: 1px solid #444; margin-bottom: 8px; }
        .firmas { display: flex; justify-content: space-around; margin-top: 70px; gap: 40px; }
        .firma { text-align: center; flex: 1; font-size: 13px; }
        .firma .linea { border-top: 1.5px solid #1c1c1e; margin-bottom: 6px; padding-top: 6px; }
        @media print {
            .toolbar { display: none; }
            body { background: #fff; }
            .hoja { box-shadow: none; margin: 0; width: auto; max-width: none; padding: 6mm 4mm; }
        }
    </style>
</head>
<body>
<div class="toolbar">
    <c:choose>
        <c:when test="${tipoFormato == 'reporte'}">
            <a href="${pageContext.request.contextPath}/reporte?id=${r.idReporte}">&#8592; Volver al reporte</a>
            <span style="font-size: 13px;">Imprime o guarda como PDF, firma el reporte y súbelo en el detalle del reporte</span>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}">&#8592; Volver a detalles</a>
            <span style="font-size: 13px;">Imprime o guarda como PDF, firma el documento y súbelo en los detalles de tu solicitud</span>
        </c:otherwise>
    </c:choose>
    <button onclick="window.print()">Imprimir / Guardar PDF</button>
</div>

<div class="hoja">
    <div class="encabezado">
        <h1>Universidad Tecnológica Emiliano Zapata del Estado de Morelos</h1>
        <span class="codigo">
            <c:choose>
                <c:when test="${tipoFormato == 'fo'}">FO-UTEZ-EST-08</c:when>
                <c:when test="${tipoFormato == 'oficio'}">Oficio de visita</c:when>
                <c:when test="${tipoFormato == 'reporte'}">Reporte de visita</c:when>
                <c:otherwise>Carta responsiva</c:otherwise>
            </c:choose>
        </span>
    </div>

    <c:choose>
        <%-- ========== FO-UTEZ-EST-08: solicitud de visita ========== --%>
        <c:when test="${tipoFormato == 'fo'}">
            <h2 class="titulo-doc">Solicitud de visita académica</h2>
            <table class="campos">
                <tr><th>Nombre de la empresa o actividad</th><td>${s.nombreEmpresaActividad}</td></tr>
                <tr><th>Lugar o dirección</th><td>${empty s.lugarDireccion ? '' : s.lugarDireccion}</td></tr>
                <tr><th>Teléfonos del contacto</th><td>${empty s.telefonoContacto ? '' : s.telefonoContacto}</td></tr>
                <tr><th>Correo electrónico del contacto</th><td>${empty s.correoContacto ? '' : s.correoContacto}</td></tr>
                <tr><th>Fecha de inicio</th><td>${empty s.fechaInicio ? '' : s.fechaInicio}</td></tr>
                <tr><th>Área solicitante</th><td>${empty s.areaSolicitante ? '' : s.areaSolicitante}</td></tr>
                <tr><th>Docente responsable</th><td>${s.nombreSolicitante}</td></tr>
                <tr><th>Objetivo de la visita</th><td>${empty s.objetivo ? '' : s.objetivo}</td></tr>
            </table>

            <c:if test="${not empty s.programas}">
                <table class="campos">
                    <tr>
                        <th style="width:auto;">Programa educativo</th>
                        <th style="width:auto;">Cuatrimestre</th>
                        <th style="width:auto;">Grupo</th>
                        <th style="width:auto;">No. estudiantes</th>
                    </tr>
                    <c:forEach var="p" items="${s.programas}">
                        <tr>
                            <td>${p.divisionAcademica}</td>
                            <td>${p.cuatrimestre}</td>
                            <td>${empty p.grupo ? '' : p.grupo}</td>
                            <td>${p.noEstudiantes}</td>
                        </tr>
                    </c:forEach>
                    <tr>
                        <td colspan="3" style="font-weight:bold;">Total de estudiantes</td>
                        <td style="font-weight:bold;">${s.totalEstudiantes}</td>
                    </tr>
                </table>
            </c:if>

            <c:if test="${not empty s.asignaturas}">
                <p class="parrafo"><strong>Asignaturas que se reforzarán con la visita:</strong>
                    <c:forEach var="a" items="${s.asignaturas}" varStatus="st">${a}<c:if test="${!st.last}">, </c:if></c:forEach>.
                </p>
            </c:if>

            <div class="firmas">
                <div class="firma"><div class="linea">${s.nombreSolicitante}</div>Docente responsable</div>
                <div class="firma"><div class="linea">&nbsp;</div>Director(a) de división académica</div>
            </div>
        </c:when>

        <%-- ========== Oficio de visita (se genera al aprobar) ========== --%>
        <c:when test="${tipoFormato == 'oficio'}">
            <h2 class="titulo-doc">Oficio de visita académica</h2>
            <p class="parrafo">A quien corresponda en <strong>${s.nombreEmpresaActividad}</strong>:</p>
            <p class="parrafo">
                Por este medio se hace constar que la Universidad Tecnológica Emiliano Zapata del Estado de
                Morelos, a través del área de Estadías, autoriza la visita académica de
                <strong>${s.totalEstudiantes}</strong> estudiante(s) a cargo del (de la) docente
                <strong>${s.nombreSolicitante}</strong>, a realizarse
                <c:if test="${not empty s.fechaInicio}">el día <strong>${s.fechaInicio}</strong></c:if>
                en ${empty s.lugarDireccion ? 'sus instalaciones' : s.lugarDireccion}.
            </p>
            <c:if test="${not empty s.objetivo}">
                <p class="parrafo"><strong>Objetivo de la visita:</strong> ${s.objetivo}</p>
            </c:if>
            <p class="parrafo">
                Agradecemos de antemano las facilidades otorgadas para la realización de esta actividad,
                que forma parte de la formación académica de nuestros estudiantes.
            </p>
            <div class="firmas">
                <div class="firma"><div class="linea">&nbsp;</div>Área de Estadías - UTEZ</div>
            </div>
        </c:when>

        <%-- ========== Reporte de visita (lo genera el docente al terminar) ========== --%>
        <c:when test="${tipoFormato == 'reporte'}">
            <h2 class="titulo-doc">Reporte de visita académica</h2>
            <table class="campos">
                <tr><th>Nombre de la empresa o actividad</th><td>${s.nombreEmpresaActividad}</td></tr>
                <tr><th>Lugar o dirección</th><td>${empty s.lugarDireccion ? '' : s.lugarDireccion}</td></tr>
                <tr><th>Fecha de la visita</th><td>${empty r.fecha ? '' : r.fecha}</td></tr>
                <tr><th>Docente responsable</th><td>${s.nombreSolicitante}</td></tr>
                <tr><th>Total de estudiantes</th><td>${s.totalEstudiantes}</td></tr>
            </table>

            <p class="parrafo"><strong>Resultados de la visita:</strong> <c:out value="${r.resultados}"/></p>
            <c:if test="${not empty r.observaciones}">
                <p class="parrafo"><strong>Observaciones:</strong> <c:out value="${r.observaciones}"/></p>
            </c:if>

            <c:if test="${not empty imagenes}">
                <p class="parrafo"><strong>Evidencia fotográfica:</strong></p>
                <div class="fotos-reporte">
                    <c:forEach var="img" items="${imagenes}">
                        <img src="${pageContext.request.contextPath}/reporte?imagen=${img.idImagen}"
                             alt="Evidencia de la visita">
                    </c:forEach>
                </div>
            </c:if>

            <div class="firmas">
                <div class="firma"><div class="linea">${s.nombreSolicitante}</div>Docente responsable</div>
                <div class="firma"><div class="linea">&nbsp;</div>Vo.Bo. Área de Estadías - UTEZ</div>
            </div>
        </c:when>

        <%-- ========== Carta responsiva (se genera al aprobar) ========== --%>
        <c:otherwise>
            <h2 class="titulo-doc">Carta responsiva</h2>
            <p class="parrafo">
                El (la) que suscribe, <strong>${s.nombreSolicitante}</strong>, docente adscrito(a) al área
                <strong>${empty s.areaSolicitante ? '________________' : s.areaSolicitante}</strong> de la
                Universidad Tecnológica Emiliano Zapata del Estado de Morelos, manifiesta que se hace
                responsable del traslado, comportamiento y seguridad de los
                <strong>${s.totalEstudiantes}</strong> estudiante(s) que participarán en la visita académica a
                <strong>${s.nombreEmpresaActividad}</strong>
                <c:if test="${not empty s.lugarDireccion}">, ubicada en ${s.lugarDireccion},</c:if>
                <c:if test="${not empty s.fechaInicio}"> a realizarse el día <strong>${s.fechaInicio}</strong></c:if>.
            </p>
            <p class="parrafo">
                Asimismo, se compromete a vigilar el cumplimiento del reglamento escolar durante toda la
                actividad y a entregar el reporte de la visita al término de la misma.
            </p>
            <div class="firmas">
                <div class="firma"><div class="linea">${s.nombreSolicitante}</div>Docente responsable</div>
                <div class="firma"><div class="linea">&nbsp;</div>Área de Estadías - UTEZ</div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
