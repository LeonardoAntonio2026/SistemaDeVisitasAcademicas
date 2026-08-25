/**
 * @file loading.js — Feedback de carga (sin librerías).
 *
 * Hace dos cosas:
 *   1) Spinner en el botón al enviar un formulario.
 *   2) Barra fina de progreso al navegar entre páginas.
 *
 * Objetivo: que la app server-rendered no se sienta "congelada"
 * entre el click y la nueva página.
 *
 * Se carga desde layout/header.jsp con `defer`, así que corre en todas las
 * páginas con sesión. Todo va dentro de una IIFE: no expone nada al ámbito
 * global, se engancha solo a eventos de `document` y `window`.
 *
 * Depende del marcado: espera un elemento con id "barra-carga" (lo pinta
 * header.jsp) y usa la clase CSS "activa" para animarlo.
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 24/08/2026
 */
(function () {
    "use strict";

    /**
     * Temporizador que oculta la barra si la navegación nunca ocurrió.
     * @type {?number}
     */
    var timeoutSeguridad = null;

    /**
     * Muestra la barra de progreso y reinicia su animación.
     *
     * Si el elemento #barra-carga no existe, no hace nada: así una página que
     * no lo pinte no truena.
     *
     * @returns {void}
     */
    function mostrarBarra() {
        var barra = document.getElementById("barra-carga");
        if (barra) {
            barra.classList.remove("activa"); // reinicia por si venía a medias
            // fuerza reflow para reiniciar la animación
            void barra.offsetWidth;
            barra.classList.add("activa");
            // Red de seguridad: si por algo no hubo navegación (p. ej. una
            // descarga), oculta la barra para que no quede pegada.
            if (timeoutSeguridad) {
                clearTimeout(timeoutSeguridad);
            }
            timeoutSeguridad = setTimeout(ocultarBarra, 10000);
        }
    }

    /**
     * Oculta la barra de progreso.
     *
     * La llaman el temporizador de seguridad y el evento pageshow (al volver
     * con el botón "atrás" del navegador).
     *
     * @returns {void}
     */
    function ocultarBarra() {
        var barra = document.getElementById("barra-carga");
        if (barra) {
            barra.classList.remove("activa");
        }
    }

    // ---- 1) Botón en estado "cargando" al enviar un formulario ----
    // Escuchamos en 'document' (fase de burbuja): así corremos DESPUÉS de
    // los validadores del formulario y de los modales de confirmación
    // (js/modales.js escucha en captura), y podemos saber si el envío se
    // canceló (e.defaultPrevented).
    document.addEventListener("submit", function (e) {
        if (e.defaultPrevented) {
            return; // una validación o una confirmación detuvo el envío
        }
        var btn = e.submitter;
        if (!btn || btn.classList.contains("cargando")) {
            return;
        }
        // OJO: no usamos disabled porque quitaría name/value del POST
        // (p. ej. action=aprobar/rechazar). Bloqueamos con CSS pointer-events.
        btn.style.minWidth = btn.offsetWidth + "px";
        btn.dataset.htmlOriginal = btn.innerHTML;
        var texto = btn.getAttribute("data-cargando") || "Procesando…";
        btn.innerHTML =
            '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> ' +
            texto;
        btn.classList.add("cargando");
        mostrarBarra();
    }, false);

    // ---- 2) Barra de progreso al navegar por enlaces internos ----
    document.addEventListener("click", function (e) {
        if (e.defaultPrevented) {
            return; // el modal de confirmación del enlace detuvo la navegación
        }
        var a = e.target.closest("a[href]");
        if (!a) {
            return;
        }
        var href = a.getAttribute("href");
        if (!href || href.charAt(0) === "#" || href.indexOf("javascript:") === 0) {
            return;
        }
        // No mostrar en descargas ni en enlaces que abren pestaña nueva.
        // OJO: se decide por el atributo download y no por la clase
        // .btn-descargar, porque esa clase también viste enlaces que solo
        // navegan (Ver solicitud, Editar...) y se quedaban sin la barra.
        if (a.target === "_blank" || a.hasAttribute("download")) {
            return;
        }
        // Solo navegación del mismo sitio
        if (a.host && a.host !== window.location.host) {
            return;
        }
        mostrarBarra();
    });

    // ---- 3) Cada página abre desde arriba ----
    // El navegador restaura el scroll al navegar y se entraba a media página,
    // sin ver los avisos ni las instrucciones del principio
    if ("scrollRestoration" in history) {
        history.scrollRestoration = "manual";
    }

    // ---- Al volver con "atrás" (bfcache) revertimos todo ----
    window.addEventListener("pageshow", function () {
        ocultarBarra();
        window.scrollTo(0, 0);
        var pendientes = document.querySelectorAll(".cargando");
        for (var i = 0; i < pendientes.length; i++) {
            var b = pendientes[i];
            if (b.dataset.htmlOriginal) {
                b.innerHTML = b.dataset.htmlOriginal;
            }
            b.classList.remove("cargando");
            b.style.minWidth = "";
        }
    });
})();
