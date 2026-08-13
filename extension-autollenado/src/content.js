/**
 * Panel flotante dentro del formulario de Nueva Solicitud y puente con el popup
 * y el atajo de teclado de la extensión.
 */
(function () {
    "use strict";

    var CLAVE_ULTIMO = "ultimoPerfil";

    if (!SVAAutollenado.hayFormulario()) {
        return; // Cualquier otra página del sistema: la extensión no se muestra
    }

    function perfilPorId(id) {
        return SVA_PRESETS.find(function (perfil) {
            return perfil.id === id;
        });
    }

    function guardarUltimo(id) {
        chrome.storage.local.set({ ultimoPerfil: id });
    }

    function leerUltimo() {
        return new Promise(function (resolve) {
            chrome.storage.local.get(CLAVE_ULTIMO, function (datos) {
                resolve(datos[CLAVE_ULTIMO] || null);
            });
        });
    }

    // ===================== Panel =====================

    var panel = document.createElement("div");
    panel.className = "sva-af";
    panel.innerHTML =
        '<button type="button" class="sva-af__toggle" title="Datos de prueba">'
        + '<span class="sva-af__rayo">⚡</span> Autollenar</button>'
        + '<div class="sva-af__caja" hidden>'
        + '  <div class="sva-af__titulo">Datos de prueba</div>'
        + '  <select class="sva-af__select"></select>'
        + '  <p class="sva-af__desc"></p>'
        + '  <div class="sva-af__acciones">'
        + '    <button type="button" class="sva-af__btn sva-af__btn--primario" data-accion="llenar">Llenar</button>'
        + '    <button type="button" class="sva-af__btn" data-accion="limpiar">Limpiar</button>'
        + '  </div>'
        + '  <p class="sva-af__estado"></p>'
        + '</div>';
    document.body.appendChild(panel);

    var toggle = panel.querySelector(".sva-af__toggle");
    var caja = panel.querySelector(".sva-af__caja");
    var select = panel.querySelector(".sva-af__select");
    var descripcion = panel.querySelector(".sva-af__desc");
    var estado = panel.querySelector(".sva-af__estado");

    SVA_PRESETS.forEach(function (perfil) {
        var opcion = document.createElement("option");
        opcion.value = perfil.id;
        opcion.textContent = perfil.nombre;
        select.appendChild(opcion);
    });

    function pintarDescripcion() {
        var perfil = perfilPorId(select.value);
        descripcion.textContent = perfil ? perfil.descripcion : "";
    }

    function mostrarEstado(texto, esAviso) {
        estado.textContent = texto;
        estado.classList.toggle("sva-af__estado--aviso", !!esAviso);
    }

    /** Traduce el resultado del llenado a una línea de estado. */
    function resumen(resultado) {
        if (!resultado.ok) {
            return { texto: resultado.avisos.join(" "), aviso: true };
        }
        if (resultado.avisos.length) {
            return { texto: "Llenado con avisos: " + resultado.avisos.join(" "), aviso: true };
        }
        return { texto: "Formulario llenado. Revisa y guarda.", aviso: false };
    }

    async function llenar(id) {
        var perfil = perfilPorId(id) || SVA_PRESETS[0];
        mostrarEstado("Llenando…", false);
        var resultado = await SVAAutollenado.llenar(perfil);
        var linea = resumen(resultado);
        mostrarEstado(linea.texto, linea.aviso);
        guardarUltimo(perfil.id);
        return resultado;
    }

    toggle.addEventListener("click", function () {
        caja.hidden = !caja.hidden;
        panel.classList.toggle("sva-af--abierto", !caja.hidden);
    });

    select.addEventListener("change", function () {
        pintarDescripcion();
        mostrarEstado("", false);
    });

    panel.addEventListener("click", async function (e) {
        var boton = e.target.closest("[data-accion]");
        if (!boton) {
            return;
        }
        if (boton.dataset.accion === "llenar") {
            await llenar(select.value);
        } else {
            SVAAutollenado.limpiar();
            mostrarEstado("Formulario vacío.", false);
        }
    });

    leerUltimo().then(function (id) {
        if (id && perfilPorId(id)) {
            select.value = id;
        }
        pintarDescripcion();
    });

    // ===================== Mensajes del popup y del atajo =====================

    chrome.runtime.onMessage.addListener(function (mensaje, remitente, responder) {
        if (mensaje.tipo === "estado") {
            leerUltimo().then(function (ultimo) {
                responder({
                    enFormulario: true,
                    ultimo: ultimo,
                    perfiles: SVA_PRESETS.map(function (perfil) {
                        return {
                            id: perfil.id,
                            nombre: perfil.nombre,
                            descripcion: perfil.descripcion
                        };
                    })
                });
            });
            return true;
        }

        if (mensaje.tipo === "llenar") {
            leerUltimo().then(function (ultimo) {
                var id = mensaje.id || ultimo || SVA_PRESETS[0].id;
                if (caja.hidden) {
                    toggle.click(); // Que se vean los avisos del llenado
                }
                select.value = perfilPorId(id) ? id : SVA_PRESETS[0].id;
                pintarDescripcion();
                llenar(select.value).then(responder);
            });
            return true;
        }

        if (mensaje.tipo === "limpiar") {
            responder(SVAAutollenado.limpiar());
            mostrarEstado("Formulario vacío.", false);
            return false;
        }

        return false;
    });
})();
