<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Vista imprimible de los documentos generados a partir de la solicitud
     o del reporte de visita. El usuario la imprime o la guarda como PDF
     (Ctrl+P), la firma y la sube. tipoFormato: fo | oficio | responsiva | reporte

     El FO, la carta responsiva y el reporte calcan los formatos oficiales del
     sistema de calidad (FO-UTEZ-EST-08 Rev. 08, la carta responsiva grupal y el
     reporte de visita académica): mismos rótulos, mismo orden y mismos textos
     legales, para que lo que se imprime aquí sea aceptado tal cual. El oficio no
     es un formato controlado, lo redacta el sistema. --%>
<c:set var="s" value="${solicitud}"/>
<c:set var="r" value="${reporte}"/>
<% request.setAttribute("divisiones", com.example.demo.model.CatalogoAcademico.DIVISIONES); %>
<%-- Fecha con letra para el encabezado de la carta responsiva --%>
<% request.setAttribute("hoyLetra", com.example.demo.utils.FechaTexto.hoyLargo()); %>
<%-- Docente responsable capturado en el formato; las solicitudes viejas no lo
     tienen, ahí se usa el docente que creó la solicitud --%>
<c:set var="responsable" value="${empty s.docenteResponsable ? s.nombreSolicitante : s.docenteResponsable}"/>
<c:set var="img" value="${pageContext.request.contextPath}/img"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>
        <c:choose>
            <c:when test="${tipoFormato == 'fo'}">FO-UTEZ-EST-08 - Formato de visita académica</c:when>
            <c:when test="${tipoFormato == 'oficio'}">Oficio de visita académica</c:when>
            <c:when test="${tipoFormato == 'reporte'}">Reporte de visita académica</c:when>
            <c:otherwise>Carta responsiva visitas académicas</c:otherwise>
        </c:choose>
    </title>
    <style>
        /* Carta, con los márgenes del formato oficial. Se declara aquí y no con
           padding en .hoja porque el padding solo separa el principio y el final
           del bloque: en un documento de varias hojas las de en medio saldrían
           pegadas al borde. */
        @page { size: letter; margin: 12mm 12mm 10mm; }

        body {
            font-family: Arial, Helvetica, sans-serif;
            background: #E8E8ED;
            margin: 0;
            color: #000;
        }
        .toolbar {
            background: #183052;
            color: #fff;
            padding: 12px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
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
            white-space: nowrap;
        }
        .hoja {
            background: #fff;
            width: 216mm;
            max-width: 95%;
            margin: 24px auto;
            padding: 12mm 12mm 10mm;
            box-shadow: 0 2px 12px rgba(0,0,0,0.15);
            box-sizing: border-box;
        }
        /* Los documentos que llevan banda institucional se arman como columna
           flexible para empujarla al pie con margin-top:auto, en vez de fijarla
           con position:fixed (que la repetía en cada hoja y dejaba que el texto
           le pasara por debajo). La carta responsiva NO usa esto: su tabla de
           firmas tiene que poder partirse entre hojas, y el flex estorba. */
        .hoja.con-pie {
            display: flex;
            flex-direction: column;
            min-height: 252mm;
        }

        /* ---------- Encabezados ---------- */
        .enc-formato {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 4px;
        }
        .enc-formato h1, .enc-reporte h1 {
            font-size: 15pt;
            font-weight: 700;
            margin: 0;
            text-align: center;
            flex: 1;
        }
        .enc-formato .logo-estadias { height: 14mm; }
        .enc-formato .logo-utez { height: 14mm; }
        .enc-reporte {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            margin-bottom: 10px;
        }
        .enc-reporte h1 { text-align: left; }
        .enc-reporte .logos { display: flex; align-items: center; gap: 10px; }
        .enc-reporte .logos img { height: 15mm; }
        .codigo-formato {
            text-align: right;
            font-size: 10.5pt;
            font-weight: 700;
            line-height: 1.4;
            margin-bottom: 8px;
        }

        /* ---------- Tablas de los formatos ---------- */
        /* Las medidas de aquí abajo están ajustadas para que el FO y el reporte
           quepan en UNA hoja carta con su banda al pie. Si le subes el relleno
           o la letra, revisa que sigan cabiendo. */
        table.tabla {
            width: 100%;
            border-collapse: collapse;
            font-size: 10.5pt;
            margin-bottom: 9px;
        }
        table.tabla th, table.tabla td {
            border: 1px solid #000;
            padding: 3px 6px;
            text-align: left;
            vertical-align: top;
        }
        table.tabla th { font-weight: 700; background: #EDEBE4; }
        table.tabla .seccion {
            background: #D8D5CC;
            text-align: center;
            text-transform: uppercase;
        }
        /* Rótulo de sección que en el formato original no va en mayúsculas */
        table.tabla .seccion-normal { text-transform: none; }
        .centro { text-align: center; }
        table.tabla .fino { font-weight: 400; font-size: 8.5pt; }
        /* Celdas de texto libre: el formato las imprime altas aunque vayan vacías */
        table.tabla .alta { height: 11mm; }
        table.tabla .muy-alta { height: 26mm; }
        /* Dos tablas seguidas se leen como una sola caja, igual que en el formato */
        table.tabla.pegada { margin-bottom: 0; }
        .tabla-fecha { width: 45%; margin-left: auto; }
        .nota-seccion { font-size: 10.5pt; font-weight: 700; margin: 10px 0 6px; }

        /* ---------- Firmas ---------- */
        .firmas {
            display: flex;
            justify-content: space-around;
            gap: 30px;
            margin-top: 10px;
        }
        .firmas.una { justify-content: center; }
        /* Ancho suficiente para que el rótulo de abajo quepa en una línea: a dos
           líneas el bloque de firmas crecía ~5mm y empujaba la banda a otra hoja */
        .firma { flex: 1; max-width: 92mm; text-align: center; font-size: 10pt; }
        /* Hueco para firmar de puño y letra */
        .firma .rotulo { font-weight: 700; margin-bottom: 6mm; }
        .firma .nombre { font-size: 10pt; min-height: 14px; }
        .firma .linea { border-top: 1px solid #000; margin-top: 2px; padding-top: 4px; }

        /* ---------- Pie institucional ---------- */
        /* La banda va al ancho del contenido, no sangrada al borde de la hoja:
           con márgenes negativos se salía del área imprimible y el navegador
           encogía TODO el documento al 94% para que cupiera. */
        .pie-institucional { margin: 8px 0 0; }
        /* Firmas y banda son el cierre del documento y se mueven juntas: si no
           caben, las dos pasan a la hoja siguiente. Así nunca sale una hoja
           final con la banda sola, que era lo que se veía como "sobrepuesto". */
        .hoja.con-pie .cierre { margin-top: auto; }
        .pie-institucional img { width: 100%; display: block; }
        .leyenda-seguro {
            font-size: 7.5pt;
            font-style: italic;
            color: #8a1b1b;
            text-align: center;
            line-height: 1.3;
            margin: 0 4mm 4px;
        }

        /* ---------- Carta responsiva (documento de texto corrido) ---------- */
        .titulo-carta { font-size: 12pt; font-weight: 700; text-align: center; margin: 0 0 18px; }
        .lugar-fecha { font-size: 11pt; margin-bottom: 18px; }
        .destinatario { font-size: 11pt; font-weight: 700; line-height: 1.5; margin-bottom: 16px; }
        p.parrafo { font-size: 10.5pt; line-height: 1.55; text-align: justify; margin: 0 0 12px; }
        ul.compromisos { font-size: 10.5pt; line-height: 1.55; margin: 0 0 12px; padding-left: 22px; }
        ul.compromisos li { margin-bottom: 4px; }
        .dato-lleno { font-weight: 700; }
        /* Hueco que se llena a mano al momento de firmar */
        .hueco { display: inline-block; min-width: 90px; border-bottom: 1px solid #000; }
        table.firmantes { width: 100%; border-collapse: collapse; font-size: 10pt; margin-top: 6px; }
        table.firmantes th, table.firmantes td { border: 1px solid #000; padding: 5px 7px; }
        table.firmantes th { background: #EDEBE4; text-align: center; }
        table.firmantes td { height: 8mm; }
        table.firmantes tr { page-break-inside: avoid; }
        /* El encabezado va en <thead> y con table-header-group para que se
           repita en TODAS las hojas: si no, la segunda hoja de firmas queda con
           las columnas sin rotular y el estudiante no sabe qué va en cada una. */
        table.firmantes thead { display: table-header-group; }
        /* Y que el encabezado no se quede solo al final de una hoja */
        table.firmantes thead tr { page-break-after: avoid; }

        /* ---------- Anexo de evidencia fotográfica del reporte ---------- */
        /* Dos fotos por renglón. Ojo con dos detalles que las tiraban a una por
           renglón: el borde suma al ancho si el box-sizing es el de por defecto
           (de ahí el border-box), y ancho + hueco no deben empatar en 100%
           exacto porque el redondeo desborda. La altura es fija y no max-height
           porque object-fit necesita altura definida para recortar. */
        .fotos-reporte {
            display: flex;
            flex-wrap: wrap;
            align-items: flex-start;
            gap: 8px 3%;
            margin: 10px 0 0;
        }
        .fotos-reporte img {
            box-sizing: border-box;
            width: 47.5%;
            height: 62mm;
            object-fit: cover;
            border: 1px solid #444;
        }

        @media print {
            .toolbar { display: none; }
            body { background: #fff; }
            /* Los márgenes los pone @page: si además los pusiera .hoja se
               sumarían y el contenido se apretaría de más */
            .hoja {
                box-shadow: none;
                margin: 0;
                width: auto;
                max-width: none;
                padding: 0;
                page-break-after: always;
            }
            /* Sin esto el navegador agrega una hoja final en blanco */
            .hoja:last-child { page-break-after: auto; }
            /* Alto de la caja de impresión: carta (279mm) menos los márgenes,
               con un par de milímetros de holgura para que el redondeo del
               navegador no empuje la banda a una hoja de más */
            .hoja.con-pie { min-height: 254mm; }
            /* Nada de esto se ve bien partido a la mitad entre dos hojas */
            tr, .firmas, .pie-institucional, .cierre { page-break-inside: avoid; }
            .salto-pagina { page-break-before: always; }
        }
    </style>
</head>
<body>
<div class="toolbar">
    <c:choose>
        <c:when test="${tipoFormato == 'reporte'}">
            <a href="${pageContext.request.contextPath}/reporte?id=${r.idReporte}">&#8592; Volver al reporte de la visita</a>
            <span style="font-size: 13px;">Imprime o guarda como PDF, firma el reporte y súbelo en el detalle del reporte</span>
        </c:when>
        <c:when test="${tipoFormato == 'responsiva'}">
            <a href="${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}">&#8592; Volver a la solicitud</a>
            <span style="font-size: 13px;">Imprímela, recoge las firmas de los estudiantes, escanéala y súbela en los detalles de tu solicitud</span>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/detalle?id=${s.idSolicitud}">&#8592; Volver a la solicitud</a>
            <span style="font-size: 13px;">Imprime o guarda como PDF, firma el documento y súbelo en los detalles de tu solicitud</span>
        </c:otherwise>
    </c:choose>
    <button onclick="window.print()">Imprimir / Guardar PDF</button>
</div>

<c:choose>
    <%-- ================================================================
         FO-UTEZ-EST-08 Rev. 08 — Formato de visita académica
         ================================================================ --%>
    <c:when test="${tipoFormato == 'fo'}">
        <div class="hoja con-pie">
            <div class="enc-formato">
                <img class="logo-estadias" src="${img}/formato-estadias.png" alt="Estadías">
                <h1>FORMATO DE VISITA ACADÉMICA</h1>
                <img class="logo-utez" src="${img}/formato-utez.png" alt="UTEZ">
            </div>
            <div class="codigo-formato">FO-UTEZ-EST-08<br>Rev. 08</div>

            <table class="tabla tabla-fecha">
                <tr><th style="width:35%">Fecha:</th><td>${empty s.fechaCreacion ? '' : s.fechaCreacion}</td></tr>
            </table>

            <table class="tabla">
                <tr><th class="seccion" colspan="4">Datos del lugar a visitar</th></tr>
                <tr>
                    <th style="width:32%">Nombre de la empresa o actividad:</th>
                    <td colspan="3"><c:out value="${s.nombreEmpresaActividad}"/></td>
                </tr>
                <tr>
                    <th>Lugar o dirección:</th>
                    <td colspan="3"><c:out value="${empty s.lugarDireccion ? '' : s.lugarDireccion}"/></td>
                </tr>
                <tr>
                    <th>Teléfonos del contacto:</th>
                    <td colspan="3"><c:out value="${empty s.telefonoContacto ? '' : s.telefonoContacto}"/></td>
                </tr>
                <tr>
                    <th>Correo electrónico:</th>
                    <td colspan="3"><c:out value="${empty s.correoContacto ? '' : s.correoContacto}"/></td>
                </tr>
                <%-- La hora y la fecha de término son parte del formato oficial,
                     pero el sistema no las captura: van en blanco para llenarse
                     a mano antes de firmar --%>
                <tr>
                    <th>Fecha de inicio de la visita:</th>
                    <td style="width:22%">${empty s.fechaInicio ? '' : s.fechaInicio}</td>
                    <th style="width:20%">Hora de la visita:</th>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <th>Fecha de término de la visita:</th>
                    <td colspan="3">&nbsp;</td>
                </tr>
                <tr>
                    <th>Objetivo de la visita:</th>
                    <td colspan="3" class="alta"><c:out value="${empty s.objetivo ? '' : s.objetivo}"/></td>
                </tr>
            </table>

            <table class="tabla pegada">
                <tr><th class="seccion" colspan="4">Datos de los participantes de la visita</th></tr>
                <tr>
                    <th style="width:32%">Área solicitante:</th>
                    <td colspan="3"><c:out value="${empty s.areaSolicitante ? '' : s.areaSolicitante}"/></td>
                </tr>
                <tr>
                    <th>Docente responsable de la visita:</th>
                    <td><c:out value="${responsable}"/></td>
                    <th style="width:14%">Celular:</th>
                    <td style="width:18%"><c:out value="${empty s.celularResponsable ? '' : s.celularResponsable}"/></td>
                </tr>
                <tr>
                    <th>Docentes acompañantes:</th>
                    <td colspan="3">
                        <c:forEach var="d" items="${s.docentesAcompanantes}" varStatus="st"><c:out value="${d.nombre}"/><c:if test="${!st.last}">, </c:if></c:forEach>
                    </td>
                </tr>
            </table>

            <table class="tabla">
                <tr>
                    <th class="seccion seccion-normal" colspan="5">Número de estudiantes participantes por división académica:</th>
                </tr>
                <tr>
                    <c:forEach var="division" items="${divisiones}">
                        <th class="centro"><c:out value="${division}"/></th>
                    </c:forEach>
                    <th class="centro">Total de estudiantes</th>
                </tr>
                <tr>
                    <c:forEach var="division" items="${divisiones}">
                        <td class="centro">${empty s.estudiantesPorDivision[division] ? 0 : s.estudiantesPorDivision[division]}</td>
                    </c:forEach>
                    <td class="centro"><strong>${s.totalPorDivision}</strong></td>
                </tr>
            </table>

            <p class="nota-seccion">La siguiente sección es de llenado exclusivo para visita académica:</p>

            <table class="tabla">
                <tr>
                    <th style="width:52%">Programa educativo:
                        <span class="fino">(desglose la participación de estudiantes por programa educativo, cuatrimestre, grado y grupo)</span>
                    </th>
                    <th class="centro">Cuatrimestre</th>
                    <th class="centro">Grupo</th>
                    <th class="centro">No. de Estudiantes</th>
                </tr>
                <c:forEach var="p" items="${s.programas}">
                    <tr>
                        <td><c:out value="${p.programa}"/></td>
                        <td class="centro">${p.cuatrimestre}</td>
                        <td class="centro"><c:out value="${empty p.grupo ? '' : p.grupo}"/></td>
                        <td class="centro">${p.noEstudiantes}</td>
                    </tr>
                </c:forEach>
            </table>

            <table class="tabla">
                <tr>
                    <th style="width:38%">Asignaturas que se reforzarán con la visita:</th>
                    <td><c:forEach var="a" items="${s.asignaturas}" varStatus="st"><c:out value="${a}"/><c:if test="${!st.last}">, </c:if></c:forEach></td>
                </tr>
            </table>

            <div class="cierre">
    <div class="firmas">
                    <div class="firma">
                        <div class="rotulo">Solicita</div>
                        <div class="nombre"><c:out value="${responsable}"/></div>
                        <div class="linea">Nombre del docente responsable de la visita</div>
                    </div>
                    <div class="firma">
                        <div class="rotulo">Autoriza</div>
                        <%-- Se llena al aprobar; mientras el formato se firma en blanco --%>
                        <div class="nombre"><c:out value="${empty s.nombreAutoriza ? '' : s.nombreAutoriza}"/></div>
                        <div class="linea">Nombre y cargo del director de carrera/titular del área</div>
                    </div>
                </div>

    <div class="pie-institucional">
                    <%-- La leyenda del pie del formato original: al firmar, el docente
                         se hace responsable del seguro facultativo de sus estudiantes --%>
                    <p class="leyenda-seguro">
                        El docente responsable de la visita académica declara que todos los estudiantes
                        participantes cuentan con seguro facultativo activo al momento de la realización de la
                        actividad. Al firmar el presente documento, el docente asume la responsabilidad de
                        verificar y asegurar el cumplimiento de este requisito.
                    </p>
                    <img src="${img}/formato-banda.png" alt="">
                </div>
            </div>
        </div>
    </c:when>

    <%-- ================================================================
         Oficio de visita — lo redacta el sistema, no es formato controlado
         ================================================================ --%>
    <c:when test="${tipoFormato == 'oficio'}">
        <div class="hoja con-pie">
            <div class="enc-formato">
                <img class="logo-estadias" src="${img}/formato-estadias.png" alt="Estadías">
                <h1>OFICIO DE VISITA ACADÉMICA</h1>
                <img class="logo-utez" src="${img}/formato-utez.png" alt="UTEZ">
            </div>

            <p class="parrafo" style="margin-top:18px;">Emiliano Zapata, Morelos a ${hoyLetra}.</p>
            <p class="parrafo">A quien corresponda en <strong><c:out value="${s.nombreEmpresaActividad}"/></strong>:</p>
            <p class="parrafo">
                Por este medio se hace constar que la Universidad Tecnológica Emiliano Zapata del Estado de
                Morelos, a través del área de Estadías, autoriza la visita académica de
                <strong>${s.totalEstudiantes}</strong> estudiante(s) a cargo del (de la) docente
                <strong><c:out value="${responsable}"/></strong>, a realizarse
                <c:if test="${not empty s.fechaInicioEnLetra}">el día <strong>${s.fechaInicioEnLetra}</strong></c:if>
                en <c:out value="${empty s.lugarDireccion ? 'sus instalaciones' : s.lugarDireccion}"/>.
            </p>
            <c:if test="${not empty s.objetivo}">
                <p class="parrafo"><strong>Objetivo de la visita:</strong> <c:out value="${s.objetivo}"/></p>
            </c:if>
            <p class="parrafo">
                Agradecemos de antemano las facilidades otorgadas para la realización de esta actividad,
                que forma parte de la formación académica de nuestros estudiantes.
            </p>

            <div class="cierre">
    <div class="firmas una">
                    <div class="firma">
                        <div class="rotulo">&nbsp;</div>
                        <div class="nombre"><c:out value="${empty s.nombreAutoriza ? '' : s.nombreAutoriza}"/></div>
                        <div class="linea">Área de Estadías &mdash; UTEZ</div>
                    </div>
                </div>

    <div class="pie-institucional">
                    <img src="${img}/formato-banda.png" alt="">
                </div>
            </div>
        </div>
    </c:when>

    <%-- ================================================================
         Reporte de visita académica
         ================================================================ --%>
    <c:when test="${tipoFormato == 'reporte'}">
        <div class="hoja con-pie">
            <div class="enc-reporte">
                <h1>REPORTE DE VISITA ACADÉMICA</h1>
                <div class="logos">
                    <img src="${img}/formato-estadias.png" alt="Estadías">
                    <img src="${img}/formato-utez.png" alt="UTEZ">
                </div>
            </div>

            <table class="tabla tabla-fecha">
                <tr><th style="width:35%">Fecha:</th><td>${empty r.fecha ? '' : r.fecha}</td></tr>
            </table>

            <table class="tabla">
                <tr><th class="seccion" colspan="4">Datos del lugar visitado</th></tr>
                <tr>
                    <th style="width:30%">Nombre de la empresa o actividad:</th>
                    <td colspan="3"><c:out value="${s.nombreEmpresaActividad}"/></td>
                </tr>
                <%-- La fecha de término no se captura en la solicitud: se anota
                     a mano igual que en el formato oficial --%>
                <tr>
                    <th>Fecha de inicio de la visita / actividad:</th>
                    <td style="width:18%">${empty s.fechaInicio ? '' : s.fechaInicio}</td>
                    <th style="width:30%">Fecha de término de la visita / actividad:</th>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <th>Objetivo de la visita / actividad:</th>
                    <td colspan="3" class="alta"><c:out value="${empty s.objetivo ? '' : s.objetivo}"/></td>
                </tr>
            </table>

            <table class="tabla pegada">
                <tr><th class="seccion" colspan="2">Datos de los participantes en la visita / actividad</th></tr>
                <tr>
                    <th style="width:30%">División o Área participante:</th>
                    <td><c:out value="${empty s.areaSolicitante ? '' : s.areaSolicitante}"/></td>
                </tr>
                <tr>
                    <th>Docente (s) responsable (s) de la visita / actividad:</th>
                    <td><c:out value="${s.docentesEnLetra}"/></td>
                </tr>
            </table>

            <table class="tabla">
                <tr>
                    <th style="width:52%">Programa educativo:
                        <span class="fino">(desglose la participación de estudiantes por programa educativo, cuatrimestre, grado y grupo)</span>
                    </th>
                    <th class="centro">Cuatrimestre</th>
                    <th class="centro">Grupo</th>
                    <th class="centro">No. de Estudiantes</th>
                </tr>
                <c:forEach var="p" items="${s.programas}">
                    <tr>
                        <td><c:out value="${p.programa}"/></td>
                        <td class="centro">${p.cuatrimestre}</td>
                        <td class="centro"><c:out value="${empty p.grupo ? '' : p.grupo}"/></td>
                        <td class="centro">${p.noEstudiantes}</td>
                    </tr>
                </c:forEach>
                <tr>
                    <th class="centro" colspan="3">TOTAL DE ESTUDIANTES PARTICIPANTES</th>
                    <td class="centro"><strong>${s.totalEstudiantes}</strong></td>
                </tr>
            </table>

            <table class="tabla">
                <tr>
                    <th style="width:38%">Asignaturas que se reforzaron con la visita:</th>
                    <td><c:forEach var="a" items="${s.asignaturas}" varStatus="st"><c:out value="${a}"/><c:if test="${!st.last}">, </c:if></c:forEach></td>
                </tr>
            </table>

            <table class="tabla">
                <tr><th class="seccion">Resultados</th></tr>
                <tr><td class="muy-alta"><c:out value="${r.resultados}"/></td></tr>
            </table>

            <table class="tabla">
                <tr><th class="seccion">Observaciones / Comentarios</th></tr>
                <tr><td class="alta"><c:out value="${empty r.observaciones ? '' : r.observaciones}"/></td></tr>
            </table>

            <div class="cierre">
    <div class="firmas una">
                    <div class="firma">
                        <div class="rotulo">&nbsp;</div>
                        <div class="nombre"><c:out value="${responsable}"/></div>
                        <div class="linea">Nombre y firma del docente responsable de la visita</div>
                    </div>
                </div>

    <div class="pie-institucional">
                    <img src="${img}/formato-banda.png" alt="">
                </div>
            </div>
        </div>

        <%-- La evidencia fotográfica no forma parte del formato oficial: va como
             anexo en su propia hoja para no alterar el reporte --%>
        <c:if test="${not empty imagenes}">
            <div class="hoja salto-pagina">
                <div class="enc-reporte">
                    <h1>ANEXO. EVIDENCIA FOTOGRÁFICA</h1>
                    <div class="logos">
                        <img src="${img}/formato-utez.png" alt="UTEZ">
                    </div>
                </div>
                <table class="tabla">
                    <tr>
                        <th style="width:30%">Visita:</th>
                        <td><c:out value="${s.nombreEmpresaActividad}"/></td>
                    </tr>
                </table>
                <div class="fotos-reporte">
                    <c:forEach var="foto" items="${imagenes}">
                        <img src="${pageContext.request.contextPath}/reporte?imagen=${foto.idImagen}"
                             alt="Evidencia de la visita">
                    </c:forEach>
                </div>
            </div>
        </c:if>
    </c:when>

    <%-- ================================================================
         Carta responsiva grupal — la firman los estudiantes
         ================================================================ --%>
    <c:otherwise>
        <div class="hoja sin-pie">
            <h2 class="titulo-carta">CARTA RESPONSIVA VISITAS ACADÉMICAS</h2>

            <p class="lugar-fecha">Emiliano Zapata, Morelos a ${hoyLetra}.</p>

            <p class="destinatario">
                UNIVERSIDAD TECNOLÓGICA EMILIANO ZAPATA<br>
                DEL ESTADO DE MORELOS.<br>
                P R E S E N T E
            </p>

            <p class="parrafo">
                Por este medio, los suscritos estudiantes del programa educativo de
                <span class="dato-lleno"><c:out value="${s.programasEnLetra}"/></span> y bajo protesta de
                decir verdad, confirmamos nuestra participación en la visita a
                &ldquo;<span class="dato-lleno"><c:out value="${s.nombreEmpresaActividad}"/></span>&rdquo;, a
                celebrarse el día <span class="dato-lleno">${s.fechaInicioEnLetra}</span>, en
                <span class="dato-lleno"><c:out value="${s.lugarDireccion}"/></span>; bajo el programa anexo
                al presente documento.
            </p>

            <p class="parrafo">
                Conocedores que la actividad se documentará como una visita de estudio de la Universidad
                Tecnológica Emiliano Zapata del Estado de Morelos (UTEZ) y debido al horario del encuentro
                <%-- El horario no se captura en la solicitud: se anota a mano
                     antes de recoger las firmas, como en el formato oficial --%>
                (<span class="hueco">&nbsp;</span> a
                <span class="hueco">&nbsp;</span> hrs.), declaramos que los traslados y gastos derivados a
                nuestra participación en el evento antes mencionado los realizaremos con nuestros propios
                medios y recursos, asimismo que conocemos el alcance del seguro de la empresa que se
                contrató para el traslado.
            </p>

            <p class="parrafo">Derivado de lo anterior nos obligamos a:</p>
            <ul class="compromisos">
                <li>Respetar las reglas impuestas tanto por la UTEZ, como por los organizadores de la salida.</li>
                <li>Buscar siempre estar informado de las actividades grupales programadas.</li>
                <li>Abstenerme de cualquier conducta ilegal o inapropiada que pueda denigrar la buena imagen
                    de la UTEZ o que sea perjudicial para sus objetivos y;</li>
                <li>No poner en riesgo mi integridad física ni la de mis compañeros.</li>
            </ul>

            <p class="parrafo">
                Estamos de acuerdo en asumir la responsabilidad como ciudadanos y como miembros de la
                comunidad universitaria, por lo que nos obligamos a realizar las siguientes acciones:
            </p>

            <p class="parrafo">
                Adoptar las medidas de seguridad correspondientes de la actividad que desempeñemos en
                cualquier lugar, tales como uso adecuado de equipo de protección personal, higiene
                respiratoria, lavado de manos, etc. Así como, seguir los protocolos de prevención emitidos
                por la Universidad o institución donde esté realizando la actividad de visita de estudio,
                dentro o fuera del Estado de Morelos.
            </p>

            <p class="parrafo">
                Asimismo, manifestamos que la actividad descrita la realizamos bajo nuestra responsabilidad,
                por lo que deslindamos a la UTEZ y a su personal docente y administrativo de toda
                responsabilidad en caso de que se presente alguna consecuencia que resulte de la falta de
                acción, omisión o incumplimiento en la que hayamos incurrido con respecto a los puntos
                descritos anteriormente, así como del pago de daños y perjuicios y cualquier acción legal, en
                el entendido que mediante las acciones anteriores la Universidad está protegiendo nuestra
                integridad y la de los demás miembros de la comunidad universitaria.
            </p>

            <p class="parrafo">
                He leído este documento, entiendo completamente sus términos y por medio del mismo eximo y
                libero de toda responsabilidad a la UTEZ y a terceros, y me hago único y absoluto responsable
                de mi persona, en los términos del presente, mismo que suscribo libre y voluntariamente.
            </p>

            <%-- Un renglón por estudiante. El sistema no guarda la lista nominal,
                 pero sí cuántos van de cada grupo: el grado y grupo se imprime y
                 el estudiante solo escribe su nombre y firma. --%>
            <table class="firmantes">
                <thead>
                    <tr>
                        <th style="width:8%">No.</th>
                        <th style="width:47%">Nombre</th>
                        <th style="width:17%">Grado y Grupo</th>
                        <th style="width:28%">Firma</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty s.programas}">
                            <c:set var="n" value="0"/>
                            <c:forEach var="p" items="${s.programas}">
                                <c:forEach begin="1" end="${p.noEstudiantes}">
                                    <c:set var="n" value="${n + 1}"/>
                                    <tr>
                                        <td class="centro">${n}</td>
                                        <td></td>
                                        <td class="centro">${p.cuatrimestre}° <c:out value="${p.grupo}"/></td>
                                        <td></td>
                                    </tr>
                                </c:forEach>
                            </c:forEach>
                        </c:when>
                        <%-- Solicitudes viejas sin desglose: renglones numerados en blanco --%>
                        <c:otherwise>
                            <c:forEach var="n" begin="1" end="${s.totalEstudiantes > 0 ? s.totalEstudiantes : 30}">
                                <tr><td class="centro">${n}</td><td></td><td></td><td></td></tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>
</body>
</html>
