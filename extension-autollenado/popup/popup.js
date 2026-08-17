/**
 * Popup de la barra de Chrome. No sabe llenar nada: le manda el perfil elegido
 * a la pestaña activa (donde vive el content script) y se cierra.
 */
(function () {
    "use strict";

    var aviso = document.getElementById("aviso");

    function enviar(mensaje) {
        chrome.tabs.query({ active: true, currentWindow: true }, function (pestanas) {
            var pestana = pestanas[0];
            if (!pestana) {
                return;
            }
            chrome.tabs.sendMessage(pestana.id, mensaje, function (respuesta) {
                // Sin respuesta = esa pestaña no es el formulario de Nueva Solicitud
                if (chrome.runtime.lastError || !respuesta) {
                    aviso.hidden = false;
                    return;
                }
                window.close();
            });
        });
    }

    var perfiles = document.getElementById("perfiles");
    SVA_PRESETS.forEach(function (perfil) {
        var boton = document.createElement("button");
        boton.type = "button";
        boton.className = "perfil";
        boton.textContent = perfil.nombre;
        boton.addEventListener("click", function () {
            enviar({ tipo: "llenar", id: perfil.id });
        });
        perfiles.appendChild(boton);
    });

    document.getElementById("btn-limpiar").addEventListener("click", function () {
        enviar({ tipo: "limpiar" });
    });
})();
