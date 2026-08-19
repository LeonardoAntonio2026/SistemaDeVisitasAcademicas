<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Visor de un documento subido. Se abre en una pestaña aparte desde el
     botón "Ver" de la card de Archivos: sirve para comprobar que el archivo
     que se cargó es el correcto sin tener que descargarlo y abrirlo a mano.

     La misma barra azul que documento-impreso.jsp, para que ver un formato
     generado y ver un archivo subido se sientan como la misma cosa. --%>
<c:set var="d" value="${documento}"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- Un documento cuelga de una solicitud O de un reporte: el botón de volver
     regresa justo a la página de donde se abrió el visor --%>
<c:choose>
    <c:when test="${not empty d.idSolicitud}">
        <c:set var="volverUrl" value="${ctx}/detalle?id=${d.idSolicitud}"/>
        <c:set var="volverTexto" value="Volver a la solicitud"/>
    </c:when>
    <c:otherwise>
        <c:set var="volverUrl" value="${ctx}/reporte?id=${d.idReporte}"/>
        <c:set var="volverTexto" value="Volver al reporte"/>
    </c:otherwise>
</c:choose>
<c:set var="archivoUrl" value="${ctx}/documento?id=${d.idDocumento}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${fn:toUpperCase(d.nombreTipo)} - Ver documento</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap-icons.min.css">
    <style>
        /* La página ocupa la ventana completa y el PDF se lleva todo lo que
           sobra: un visor con scroll propio dentro de otro scroll es de lo
           más incómodo de usar. */
        html, body {
            height: 100%;
            margin: 0;
        }
        body {
            font-family: Arial, Helvetica, sans-serif;
            background: #E8E8ED;
            color: #000;
            display: flex;
            flex-direction: column;
        }
        .toolbar {
            background: #183052;
            color: #fff;
            padding: 12px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
            flex-shrink: 0;
        }
        .toolbar a {
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
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        /* Qué archivo se está viendo: sin esto el visor es un PDF suelto sin
           contexto y no se sabe cuál de los documentos se abrió */
        .archivo-info {
            display: flex;
            flex-direction: column;
            min-width: 0;
            text-align: center;
            flex: 1;
        }
        .archivo-info strong {
            font-size: 14px;
            overflow-wrap: anywhere;
        }
        .archivo-info span {
            font-size: 12.5px;
            opacity: 0.85;
        }
        .visor {
            flex: 1;
            min-height: 0;
            padding: 16px;
            box-sizing: border-box;
        }
        .visor object,
        .visor iframe {
            width: 100%;
            height: 100%;
            border: none;
            border-radius: 8px;
            background: #fff;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
        }
        /* Respaldo para los navegadores (sobre todo de celular) que no saben
           dibujar un PDF dentro de la página: ahí se ofrece descargarlo. */
        .sin-visor {
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
            padding: 32px 24px;
            text-align: center;
            color: #3C3C43;
        }
        .sin-visor a {
            display: inline-block;
            margin-top: 12px;
            background: #183052;
            color: #fff;
            border-radius: 8px;
            padding: 10px 20px;
            font-weight: 600;
            text-decoration: none;
        }

        @media (max-width: 575.98px) {
            .toolbar { padding: 10px 14px; }
            .archivo-info { text-align: left; flex-basis: 100%; order: -1; }
            .visor { padding: 10px; }
        }
    </style>
</head>
<body>
<div class="toolbar">
    <a href="${volverUrl}"><i class="bi bi-arrow-left"></i> ${volverTexto}</a>
    <span class="archivo-info">
        <strong>${fn:toUpperCase(d.nombreTipo)}</strong>
        <span>${d.tamanoLegible} · subido el ${d.fechaCarga}</span>
    </span>
    <a href="${archivoUrl}" download><i class="bi bi-download"></i> Descargar</a>
</div>

<div class="visor">
    <object data="${archivoUrl}&inline=1" type="application/pdf">
        <div class="sin-visor">
            <p>Tu navegador no puede mostrar el PDF dentro de esta página.</p>
            <a href="${archivoUrl}" download><i class="bi bi-download"></i> Descargar el archivo</a>
        </div>
    </object>
</div>
</body>
</html>
