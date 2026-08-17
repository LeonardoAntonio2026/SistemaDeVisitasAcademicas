/**
 * Puente entre el popup y el formulario: el popup no puede tocar la página, así
 * que le pide por mensaje que se llene o se vacíe.
 */
(function () {
    "use strict";

    function perfilPorId(id) {
        return SVA_PRESETS.find(function (perfil) {
            return perfil.id === id;
        });
    }

    chrome.runtime.onMessage.addListener(function (mensaje, remitente, responder) {
        if (!SVAAutollenado.hayFormulario()) {
            return false; // Otra página del sistema: sin respuesta, el popup lo avisa
        }

        if (mensaje.tipo === "llenar") {
            SVAAutollenado.llenar(perfilPorId(mensaje.id) || SVA_PRESETS[0]).then(function () {
                responder({ ok: true });
            });
            return true; // El llenado espera al servidor: la respuesta llega después
        }

        if (mensaje.tipo === "limpiar") {
            SVAAutollenado.limpiar();
            responder({ ok: true });
        }

        return false;
    });
})();
