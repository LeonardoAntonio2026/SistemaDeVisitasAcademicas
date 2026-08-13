/**
 * Popup de la barra de Chrome. No sabe llenar nada: le pide a la pestaña activa
 * (donde vive el content script) que lo haga.
 */
(function () {
    "use strict";

    var perfilesCaja = document.getElementById("perfiles");
    var ayuda = document.getElementById("ayuda");
    var pie = document.getElementById("pie");
    var estado = document.getElementById("estado");

    function mostrarEstado(texto, esAviso) {
        estado.textContent = texto;
        estado.classList.toggle("aviso", !!esAviso);
    }

    function pestanaActiva() {
        return new Promise(function (resolve) {
            chrome.tabs.query({ active: true, currentWindow: true }, function (pestanas) {
                resolve(pestanas[0] || null);
            });
        });
    }

    /** Devuelve null si en esa pestaña no hay formulario que llenar. */
    function preguntar(idPestana, mensaje) {
        return new Promise(function (resolve) {
            chrome.tabs.sendMessage(idPestana, mensaje, function (respuesta) {
                if (chrome.runtime.lastError) {
                    resolve(null);
                    return;
                }
                resolve(respuesta);
            });
        });
    }

    function sinFormulario() {
        ayuda.textContent = "Abre Nueva Solicitud en el sistema de visitas y vuelve a intentarlo.";
        perfilesCaja.innerHTML = "";
        pie.hidden = true;
    }

    function pintarPerfiles(idPestana, datos) {
        datos.perfiles.forEach(function (perfil) {
            var boton = document.createElement("button");
            boton.type = "button";
            boton.className = "perfil";

            var nombre = document.createElement("strong");
            nombre.textContent = perfil.nombre;
            boton.appendChild(nombre);

            var detalle = document.createElement("small");
            detalle.textContent = perfil.descripcion;
            if (perfil.id === datos.ultimo) {
                var marca = document.createElement("span");
                marca.className = "ultimo";
                marca.textContent = " · último usado";
                detalle.appendChild(marca);
            }
            boton.appendChild(detalle);

            boton.addEventListener("click", async function () {
                mostrarEstado("Llenando…", false);
                var resultado = await preguntar(idPestana, { tipo: "llenar", id: perfil.id });
                if (!resultado || !resultado.ok) {
                    mostrarEstado("No se pudo llenar el formulario.", true);
                    return;
                }
                if (resultado.avisos.length) {
                    mostrarEstado("Llenado con avisos: " + resultado.avisos.join(" "), true);
                    return;
                }
                mostrarEstado("Formulario llenado. Revisa y guarda.", false);
            });

            perfilesCaja.appendChild(boton);
        });

        pie.hidden = false;
        document.getElementById("btn-limpiar").addEventListener("click", async function () {
            await preguntar(idPestana, { tipo: "limpiar" });
            mostrarEstado("Formulario vacío.", false);
        });
    }

    (async function () {
        var pestana = await pestanaActiva();
        if (!pestana) {
            sinFormulario();
            return;
        }
        var datos = await preguntar(pestana.id, { tipo: "estado" });
        if (!datos || !datos.enFormulario) {
            sinFormulario();
            return;
        }
        pintarPerfiles(pestana.id, datos);
    })();
})();
