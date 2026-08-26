<%--
/**
 * Componente reutilizable para visualizar la línea de tiempo (stepper) del trámite de visita.
 * <p>
 * Muestra el progreso secuencial de 5 etapas del flujo (Llenado, Envío, Revisión, Carta responsiva y Reporte)
 * calculando los estados visuales (completado, actual o rechazado) a partir de los atributos
 * <code>stepperEstado</code> y <code>stepperEstadoReporte</code>.
 * </p>
 *
 * @author Eder Gabriel García Vázquez
 * @since 24/08/2026
 */
--%>
<%@ page pageEncoding="UTF-8" %>
<%-- Línea de tiempo del trámite completo. Componente reutilizable: se usa en la
     card de resumen de detalles y en las tarjetas de solicitud.

     Espera "stepperEstado" (estado de la solicitud) y, opcionalmente,
     "stepperEstadoReporte" (estado del reporte) para saber cómo va el último
     paso: la solicitud se cierra como "Completada" al subir la carta
     responsiva, pero el trámite no termina hasta que Estadías aprueba el
     reporte de la visita.

     Los nombres son las etapas del trámite, no las pantallas donde se hacen:
     antes decían "Llenar formulario" en el primer paso y "Llenar formulario de
     reporte" en el último, y no se distinguían. Un paso se completa cuando la
     acción ocurre (enviar, subir el archivo), no al abrir la pantalla. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pasoHecho" value="0"/>
<c:set var="pasoActual" value="0"/>
<c:set var="pasoRechazado" value="0"/>
<c:choose>
    <c:when test="${stepperEstado == 'Pendiente'}">
        <c:set var="pasoHecho" value="1"/><c:set var="pasoActual" value="2"/>
    </c:when>
    <c:when test="${stepperEstado == 'En revisión'}">
        <c:set var="pasoHecho" value="2"/><c:set var="pasoActual" value="3"/>
    </c:when>
    <c:when test="${stepperEstado == 'Aprobada'}">
        <c:set var="pasoHecho" value="3"/><c:set var="pasoActual" value="4"/>
    </c:when>
    <c:when test="${stepperEstado == 'Rechazada'}">
        <c:set var="pasoHecho" value="2"/><c:set var="pasoRechazado" value="3"/>
    </c:when>
    <c:when test="${stepperEstado == 'Completada'}">
        <%-- La solicitud ya está cerrada: lo que falta (o no) es el reporte --%>
        <c:choose>
            <c:when test="${stepperEstadoReporte == 'Aprobado'}">
                <c:set var="pasoHecho" value="5"/>
            </c:when>
            <c:when test="${stepperEstadoReporte == 'Rechazado'}">
                <c:set var="pasoHecho" value="4"/><c:set var="pasoRechazado" value="5"/>
            </c:when>
            <c:otherwise>
                <c:set var="pasoHecho" value="4"/><c:set var="pasoActual" value="5"/>
            </c:otherwise>
        </c:choose>
    </c:when>
</c:choose>

<div class="stepper">
    <c:forTokens items="Llenado de la solicitud,Envío a Estadías,Revisión de Estadías,Carta responsiva,Reporte de la visita"
                 delims="," var="pasoNombre" varStatus="st">
        <div class="step ${st.count <= pasoHecho ? 'done' : ''} ${st.count == pasoActual ? 'current' : ''} ${st.count == pasoRechazado ? 'danger' : ''}">
            <div class="step-dot"></div>
            <div class="step-label">${pasoNombre}</div>
        </div>
    </c:forTokens>
</div>