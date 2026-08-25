<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    error.jsp — Página de error única para toda la app. El contenedor la usa
    para los códigos declarados en WEB-INF/web.xml (400, 403, 404, 500) y para
    cualquier excepción que no se haya atrapado.

    El código llega en un atributo del request que pone el propio servidor;
    si no viene (por ejemplo al abrir esta página a mano) se asume 500.

    Atributos que pone el contenedor y que esta página lee:
      jakarta.servlet.error.status_code   código HTTP (400, 403, 404, 500)
      jakarta.servlet.error.request_uri   ruta que falló
      jakarta.servlet.error.exception     excepción, si la hubo

    Va declarada con isErrorPage="true", que es lo que permite leer esos
    atributos. No usa layout/header.jsp porque el error puede ocurrir sin
    sesión iniciada y la cabecera da por hecho que hay usuario.

    @author Leonardo Antonio Arroyo Rodriguez
    @since 24/08/2026
--%>
<c:set var="codigo" value="${empty requestScope['jakarta.servlet.error.status_code']
                             ? 500 : requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="ruta" value="${requestScope['jakarta.servlet.error.request_uri']}"/>

<%-- El detalle técnico va a la consola del servidor, nunca a la pantalla del
     usuario: ahí solo se muestra un mensaje que pueda entender. --%>
<%
    Object codigoLog = request.getAttribute("jakarta.servlet.error.status_code");
    Throwable causa = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
    if (causa != null) {
        System.err.println("Error " + codigoLog + " en " + request.getAttribute("jakarta.servlet.error.request_uri"));
        causa.printStackTrace();
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>
        <c:choose>
            <c:when test="${codigo == 404}">Página no encontrada</c:when>
            <c:when test="${codigo == 403}">Acceso denegado</c:when>
            <c:when test="${codigo == 400}">Solicitud incorrecta</c:when>
            <c:otherwise>Error del sistema</c:otherwise>
        </c:choose>
        - Visitas Académicas
    </title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
</head>
<body class="error-body">

<div class="error-card">
    <img src="${pageContext.request.contextPath}/img/Logotipo-UTEZ.png" alt="Logo UTEZ" class="error-logo">

    <c:choose>
        <%-- ===================== 404 ===================== --%>
        <c:when test="${codigo == 404}">
            <div class="error-codigo">404</div>
            <h1 class="error-titulo">No encontramos esta página</h1>
            <p class="error-texto">
                El recurso o elemento que estás buscando no existe o se movió de lugar.
                Revisa la dirección o vuelve al inicio para seguir trabajando.
            </p>
        </c:when>

        <%-- ===================== 403 ===================== --%>
        <c:when test="${codigo == 403}">
            <div class="error-icono"><i class="bi bi-shield-lock"></i></div>
            <div class="error-codigo">403</div>
            <h1 class="error-titulo">No tienes permiso para ver esta página</h1>
            <p class="error-texto">
                Tu cuenta no tiene acceso a esta información. Si crees que sí deberías verla,
                pide al administrador que revise el rol que tienes asignado.
            </p>
        </c:when>

        <%-- ===================== 400 ===================== --%>
        <c:when test="${codigo == 400}">
            <div class="error-codigo">400</div>
            <h1 class="error-titulo">La solicitud no es válida</h1>
            <p class="error-texto">
                Los datos de la dirección están incompletos o mal escritos. Vuelve al inicio
                e intenta de nuevo desde el menú.
            </p>
        </c:when>

        <%-- ============ 500 y cualquier otro error ============ --%>
        <c:otherwise>
            <div class="error-icono"><i class="bi bi-exclamation-triangle"></i></div>
            <div class="error-codigo">500</div>
            <h1 class="error-titulo">Ocurrió un error en el sistema</h1>
            <p class="error-texto">
                No pudimos completar la operación. No se guardó ningún cambio incompleto.
                Intenta de nuevo en unos minutos y, si el problema sigue, ponte en contacto
                con soporte técnico.
            </p>
        </c:otherwise>
    </c:choose>

    <div class="error-acciones">
        <%-- Con sesión abierta se vuelve al inicio; sin ella, al login --%>
        <c:choose>
            <c:when test="${not empty sessionScope.usuario}">
                <a href="${pageContext.request.contextPath}/indexSv" class="btncrear text-decoration-none">
                    <i class="bi bi-house-door"></i> Volver al inicio
                </a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login.jsp" class="btncrear text-decoration-none">
                    <i class="bi bi-box-arrow-in-right"></i> Iniciar sesión
                </a>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${not empty ruta}">
        <span class="error-ruta">Dirección: <c:out value="${ruta}"/></span>
    </c:if>
</div>

</body>
</html>
