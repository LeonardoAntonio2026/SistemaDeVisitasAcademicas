/* ============================================================
   modales.js — Diálogos del sistema en vez de alert() y confirm().
   Los cuadros del navegador se veían ajenos a la app, no se podían
   redactar con el lenguaje del sistema y en el celular salen con el
   dominio del servidor encima. Aquí todos los avisos y confirmaciones
   pasan por un mismo modal de Bootstrap, con el mismo aspecto que las
   cards y las mini cards de aviso del resto del sitio.

   Dos formas de usarlo:

   1) Declarativa (la de siempre en el marcado). En el <form>, en el
      botón que envía o en el enlace:

        data-confirmar="Texto de la pregunta"          (obligatorio)
        data-confirmar-titulo="Enviar solicitud"
        data-confirmar-detalle="Línea fina de apoyo"
        data-confirmar-ok="Sí, enviar"
        data-confirmar-cancelar="Cancelar"
        data-confirmar-tipo="info|aviso|exito|peligro"
        data-confirmar-requiere="#motivo"              (campo obligatorio)
        data-confirmar-requiere-mensaje="Escribe el motivo."

      Si el <form> y el botón traen data-confirmar, gana el del botón:
      así "Aprobar" y "Rechazar" preguntan distinto en un mismo form.

   2) Desde JavaScript:

        Modales.avisar("No se permiten archivos de más de 5 MB.");
        Modales.confirmar({ mensaje: "...", tipo: "peligro" }, function () { ... });

   Los escuchas van en fase de captura, o sea ANTES que los de
   loading.js: al cancelar, el envío queda con defaultPrevented y el
   botón nunca entra en "Procesando…". Al aceptar se reenvía el mismo
   formulario con su submitter (para no perder name/value, p. ej.
   action=aprobar) y ahí sí corre loading.js con normalidad.
   Se carga desde layout/header.jsp con `defer`, así que está disponible
   en todas las páginas con sesión. Expone un único objeto global,
   window.Modales, con los métodos avisar() y confirmar().

   Si Bootstrap no cargó (el servidor de la universidad no siempre tiene
   salida a internet), cada método cae a alert()/confirm() del navegador
   en vez de dejar al usuario sin respuesta.

   @author Leonardo Antonio Arroyo Rodriguez
   @since 24/08/2026
   ============================================================ */
(function (window, document) {
    "use strict";

    // Cada tipo amarra icono, color de la mini card y color del botón
    // principal. Son los mismos cuatro tonos de .instruccion.
    var TIPOS = {
        info:    { icono: "bi-question-circle",      mensaje: "modal-mensaje--info",    boton: "btn-primario" },
        aviso:   { icono: "bi-exclamation-triangle", mensaje: "modal-mensaje--aviso",   boton: "btn-primario" },
        exito:   { icono: "bi-check-circle",         mensaje: "modal-mensaje--exito",   boton: "btn-acento" },
        peligro: { icono: "bi-exclamation-triangle", mensaje: "modal-mensaje--peligro", boton: "btn-peligro" }
    };

    var nodo = null;        // el modal, se arma una sola vez y se reusa
    var modalBs = null;     // instancia de bootstrap.Modal
    var partes = {};        // referencias a los huecos que se rellenan
    var alAceptar = null;   // qué hacer si el usuario acepta
    var acepto = false;

    /**
     * Indica si Bootstrap está cargado y se puede usar su modal.
     *
     * @returns {boolean} true si existe bootstrap.Modal
     */
    function hayBootstrap() {
        return !!(window.bootstrap && window.bootstrap.Modal);
    }

    /**
     * Arma el modal la primera vez que se necesita y lo deja en el body.
     *
     * Se construye una sola vez y se reutiliza en todos los avisos de la
     * página; las llamadas siguientes devuelven el nodo ya creado.
     *
     * @returns {HTMLElement} el nodo del modal, listo para rellenar
     */
    function obtenerNodo() {
        if (nodo) {
            return nodo;
        }
        nodo = document.createElement("div");
        nodo.className = "modal fade";
        nodo.id = "modal-sistema";
        nodo.tabIndex = -1;
        nodo.setAttribute("aria-labelledby", "modal-sistema-titulo");
        nodo.setAttribute("aria-hidden", "true");
        nodo.innerHTML =
            '<div class="modal-dialog modal-dialog-centered">' +
              '<div class="modal-content">' +
                '<div class="modal-header">' +
                  '<h6 class="modal-title" id="modal-sistema-titulo"></h6>' +
                  '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>' +
                '</div>' +
                '<div class="modal-body">' +
                  '<div class="modal-mensaje">' +
                    '<i class="bi" aria-hidden="true"></i>' +
                    '<div>' +
                      '<p class="modal-texto"></p>' +
                      '<p class="modal-detalle" hidden></p>' +
                    '</div>' +
                  '</div>' +
                '</div>' +
                '<div class="modal-footer">' +
                  '<button type="button" class="btn-base btn-secundario" data-bs-dismiss="modal"></button>' +
                  '<button type="button" class="btn-base"></button>' +
                '</div>' +
              '</div>' +
            '</div>';
        document.body.appendChild(nodo);

        partes = {
            titulo: nodo.querySelector(".modal-title"),
            caja: nodo.querySelector(".modal-mensaje"),
            icono: nodo.querySelector(".modal-mensaje i"),
            texto: nodo.querySelector(".modal-texto"),
            detalle: nodo.querySelector(".modal-detalle"),
            cancelar: nodo.querySelector(".modal-footer .btn-secundario"),
            aceptar: nodo.querySelector(".modal-footer .btn-base:last-child")
        };

        // Aceptar solo marca la intención: la acción se dispara cuando el
        // modal ya se cerró, para que el fondo oscuro no se quede encima
        // mientras el navegador se lleva la página.
        partes.aceptar.addEventListener("click", function () {
            acepto = true;
            modalBs.hide();
        });

        nodo.addEventListener("shown.bs.modal", function () {
            partes.aceptar.focus();
        });

        // Cerrar con Escape, con la X o con el fondo cuenta como cancelar
        nodo.addEventListener("hidden.bs.modal", function () {
            var accion = alAceptar;
            var seAcepto = acepto;
            alAceptar = null;
            acepto = false;
            if (seAcepto && typeof accion === "function") {
                accion();
            }
        });

        modalBs = new window.bootstrap.Modal(nodo);
        return nodo;
    }

    /**
     * Acepta tanto una cadena suelta como el objeto completo de opciones.
     *
     * Así quien llama puede escribir Modales.avisar("texto") sin armar el
     * objeto cuando solo necesita el mensaje.
     *
     * @param {string|Object} opciones mensaje suelto, o el objeto de opciones
     * @returns {Object} siempre un objeto de opciones (vacío si no llegó nada)
     */
    function normalizar(opciones) {
        return typeof opciones === "string" ? { mensaje: opciones } : (opciones || {});
    }

    /**
     * Rellena el modal con las opciones dadas y lo muestra.
     *
     * @param {Object} op opciones ya normalizadas
     * @param {string} [op.mensaje] texto principal
     * @param {string} [op.titulo] encabezado del modal
     * @param {string} [op.detalle] línea fina de apoyo bajo el mensaje
     * @param {string} [op.tipo] info, aviso, exito o peligro
     * @param {string} [op.aceptar] texto del botón principal
     * @param {string} [op.cancelar] texto del botón de cancelar
     * @param {boolean} confirmable true para pregunta (dos botones),
     *        false para aviso (solo "Entendido")
     * @param {?Function} accion qué ejecutar si el usuario acepta
     * @returns {void}
     */
    function abrir(op, confirmable, accion) {
        obtenerNodo();
        var tipo = TIPOS[op.tipo] || (confirmable ? TIPOS.info : TIPOS.aviso);

        partes.titulo.textContent = op.titulo || (confirmable ? "Confirma la acción" : "Aviso");
        partes.texto.textContent = op.mensaje || "";

        partes.detalle.textContent = op.detalle || "";
        partes.detalle.hidden = !op.detalle;

        partes.caja.className = "modal-mensaje " + tipo.mensaje;
        partes.icono.className = "bi " + tipo.icono;

        partes.aceptar.className = "btn-base " + tipo.boton;
        partes.aceptar.textContent = op.aceptar || (confirmable ? "Sí, continuar" : "Entendido");

        // Un aviso no tiene nada que cancelar: se queda solo con "Entendido"
        partes.cancelar.textContent = op.cancelar || "Cancelar";
        partes.cancelar.hidden = !confirmable;

        alAceptar = accion || null;
        acepto = false;
        modalBs.show();
    }

    var Modales = {
        /**
         * Sustituye a alert(): informa y no continúa nada.
         *
         * @param {string|Object} opciones mensaje suelto, o el objeto de opciones
         * @param {Function} [alCerrar] se ejecuta cuando el usuario cierra el aviso
         * @returns {void}
         */
        avisar: function (opciones, alCerrar) {
            var op = normalizar(opciones);
            if (!hayBootstrap()) {
                window.alert(op.mensaje);
                if (typeof alCerrar === "function") {
                    alCerrar();
                }
                return;
            }
            // En un aviso, cerrar con la X y aceptar son lo mismo: el
            // seguimiento (p. ej. enfocar el campo que falta) corre siempre,
            // por eso va en 'hidden' y no colgado del botón.
            abrir(op, false, null);
            nodo.addEventListener("hidden.bs.modal", function unaVez() {
                nodo.removeEventListener("hidden.bs.modal", unaVez);
                if (typeof alCerrar === "function") {
                    alCerrar();
                }
            });
        },

        /**
         * Sustituye a confirm(): la acción corre solo si el usuario acepta.
         *
         * @param {string|Object} opciones mensaje suelto, o el objeto de opciones
         * @param {Function} accion qué ejecutar si el usuario acepta
         * @returns {void}
         */
        confirmar: function (opciones, accion) {
            var op = normalizar(opciones);
            if (!hayBootstrap()) {
                if (window.confirm(op.mensaje) && typeof accion === "function") {
                    accion();
                }
                return;
            }
            abrir(op, true, accion);
        }
    };

    // ============================================================
    // Capa declarativa: data-confirmar en formularios, botones y enlaces
    // ============================================================

    /**
     * Lee la configuración del elemento; null si no pide confirmación.
     *
     * Los datos salen de los atributos data-confirmar-* del marcado.
     *
     * @param {?HTMLElement} el formulario, botón o enlace a inspeccionar
     * @returns {?Object} las opciones del modal, o null si el elemento no
     *          trae data-confirmar
     */
    function configDe(el) {
        if (!el || !el.dataset || !el.dataset.confirmar) {
            return null;
        }
        var d = el.dataset;
        return {
            mensaje: d.confirmar,
            titulo: d.confirmarTitulo || "",
            detalle: d.confirmarDetalle || "",
            aceptar: d.confirmarOk || "",
            cancelar: d.confirmarCancelar || "",
            tipo: d.confirmarTipo || "",
            requiere: d.confirmarRequiere || "",
            requiereMensaje: d.confirmarRequiereMensaje || "Completa este campo para continuar.",
            requiereTitulo: d.confirmarRequiereTitulo || "Falta un dato"
        };
    }

    /**
     * Comprueba el campo obligatorio antes de dejar continuar.
     *
     * Campo que no puede ir vacío (el motivo de un rechazo). Antes era un
     * alert() suelto; ahora avisa en el mismo modal y deja el cursor en el
     * campo. Devuelve true si falta capturarlo.
     *
     * @param {Object} op opciones del modal
     * @param {string} [op.requiere] selector CSS del campo obligatorio
     * @param {string} [op.requiereTitulo] título del aviso si está vacío
     * @param {string} [op.requiereMensaje] mensaje del aviso si está vacío
     * @returns {boolean} true si el campo está vacío (y ya se avisó al
     *          usuario), false si se puede continuar
     */
    function faltaRequerido(op) {
        if (!op.requiere) {
            return false;
        }
        var campo = document.querySelector(op.requiere);
        if (!campo || campo.value.trim() !== "") {
            return false;
        }
        Modales.avisar({
            titulo: op.requiereTitulo,
            mensaje: op.requiereMensaje,
            tipo: "aviso"
        }, function () {
            campo.focus();
        });
        return true;
    }

    /**
     * Reenvía el formulario conservando el botón que lo envió.
     *
     * Conservar el submitter importa: es lo que lleva el name/value que
     * distingue "aprobar" de "rechazar" en un mismo formulario.
     *
     * @param {HTMLFormElement} form formulario a reenviar
     * @param {?HTMLElement} submitter botón que originó el envío
     * @returns {void}
     */
    function reenviar(form, submitter) {
        if (typeof form.requestSubmit === "function") {
            form.requestSubmit(submitter || null);
        } else if (submitter) {
            submitter.click(); // conserva name/value en navegadores viejos
        } else {
            form.submit();
        }
    }

    /**
     * Consume la marca de "esto ya se confirmó".
     *
     * Marca de "esto ya se confirmó". El segundo envío tiene que pasar de
     * largo o la confirmación se repetiría en bucle.
     *
     * La marca se borra al leerla: es de un solo uso, así que un envío
     * posterior del mismo formulario vuelve a preguntar.
     *
     * @param {HTMLElement} el formulario, botón o enlace a revisar
     * @returns {boolean} true si ya venía confirmado (y se limpia la marca)
     */
    function yaConfirmado(el) {
        if (el.dataset.confirmado === "1") {
            delete el.dataset.confirmado;
            return true;
        }
        return false;
    }

    document.addEventListener("submit", function (e) {
        var form = e.target;
        if (!form || !form.dataset || yaConfirmado(form)) {
            return;
        }
        // Manda lo que diga el botón; si no dice nada, lo del formulario
        var submitter = e.submitter;
        var op = configDe(submitter) || configDe(form);
        if (!op) {
            return;
        }
        e.preventDefault();
        if (faltaRequerido(op)) {
            return;
        }
        Modales.confirmar(op, function () {
            form.dataset.confirmado = "1";
            reenviar(form, submitter);
        });
    }, true);

    document.addEventListener("click", function (e) {
        if (!e.target || !e.target.closest) {
            return;
        }
        var enlace = e.target.closest("a[data-confirmar]");
        if (!enlace || yaConfirmado(enlace)) {
            return;
        }
        var op = configDe(enlace);
        if (!op) {
            return;
        }
        e.preventDefault();
        Modales.confirmar(op, function () {
            // Se re-dispara el mismo click: así navega igual que siempre y
            // loading.js alcanza a mostrar la barra de progreso.
            enlace.dataset.confirmado = "1";
            enlace.click();
        });
    }, true);

    window.Modales = Modales;
})(window, document);
