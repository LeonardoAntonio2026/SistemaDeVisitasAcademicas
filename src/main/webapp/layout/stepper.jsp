<%@ page pageEncoding="UTF-8" %>
<%-- Stepper de progreso de la solicitud (4 pasos). Componente reutilizable:
     se usa en la card de resumen de detalles y en las tarjetas de solicitud.
     Espera la variable "stepperEstado" con el nombre del estado.
     Nota: "Adjuntar formato" y "Adjuntar carta responsiva" NO son pasos;
     un paso se completa hasta dar click en ENVIAR / subir y confirmar. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pasoHecho" value="0"/>
<c:set var="pasoActual" value="0"/>
<c:set var="pasoRechazado" value="false"/>
<c:choose>
    <c:when test="${stepperEstado == 'Pendiente'}">
        <c:set var="pasoHecho" value="1"/><c:set var="pasoActual" value="2"/>
    </c:when>
    <c:when test="${stepperEstado == 'En revisión'}">
        <c:set var="pasoHecho" value="2"/><c:set var="pasoActual" value="3"/>
    </c:when>
    <c:when test="${stepperEstado == 'Aprobada'}">
        <c:set var="pasoHecho" value="3"/>
    </c:when>
    <c:when test="${stepperEstado == 'Completada'}">
        <c:set var="pasoHecho" value="3"/><c:set var="pasoActual" value="4"/>
    </c:when>
    <c:when test="${stepperEstado == 'Rechazada'}">
        <c:set var="pasoHecho" value="2"/><c:set var="pasoRechazado" value="true"/>
    </c:when>
</c:choose>

<div class="stepper">
    <c:forTokens items="Llenar formulario,Enviar solicitud,Revisión de estadías,Llenar formulario de reporte"
                 delims="," var="pasoNombre" varStatus="st">
        <div class="step ${st.count <= pasoHecho ? 'done' : ''} ${st.count == pasoActual ? 'current' : ''} ${pasoRechazado && st.count == 3 ? 'danger' : ''}">
            <div class="step-dot"></div>
            <div class="step-label">${pasoNombre}</div>
        </div>
    </c:forTokens>
</div>
