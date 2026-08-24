/* ============================================================
   Zonas de carga de archivos (detalle de la solicitud y del
   reporte). Dos cosas:

   1) Elegir un archivo NO es subirlo: al elegirlo se avisa que
      todavía falta dar click en el botón.
   2) Cuando el archivo ya está en el sistema la zona de carga
      nace oculta —si no, el recuadro punteado se lee como
      "todavía te falta subir algo"— y se abre con el botón
      "Reemplazar" que está junto al de descargar.
   3) Se revisa el peso y el tipo antes de enviar, para no hacer
      esperar al docente una subida de 30 MB que el servidor va a
      rechazar igual. Esto es solo comodidad: la validación que
      protege los datos es la de DocumentoServlet.
   ============================================================ */
(function () {
    "use strict";

    var MAX_BYTES = 10 * 1024 * 1024; // RN-07: mismo tope que MAX_PDF_BYTES

    // Los avisos salen en el modal del sistema (js/modales.js), como el resto
    // del sitio; nunca con los cuadros del navegador.
    function avisar(mensaje, titulo) {
        window.Modales.avisar({
            titulo: titulo,
            mensaje: mensaje,
            tipo: "aviso"
        });
    }

    // ---- 1) Aviso "seleccionado pero sin subir" (y revisión del archivo) ----
    document.querySelectorAll('.zona-carga input[type="file"]').forEach(function (input) {
        input.addEventListener("change", function () {
            var file = input.files[0];

            // Un archivo que no pasa se descarta aquí mismo: así el botón de
            // subir se queda sin nada que enviar (el input es required)
            if (file) {
                var nombre = file.name.toLowerCase();
                if (file.type !== "application/pdf" && nombre.slice(-4) !== ".pdf") {
                    input.value = "";
                    avisar("Solo se permiten archivos PDF.", "Formato no admitido");
                } else if (file.size > MAX_BYTES) {
                    input.value = "";
                    avisar("El archivo supera el tamaño máximo de 10 MB.", "El archivo pesa demasiado");
                }
            }

            var zona = input.closest(".zona-carga");
            var aviso = zona ? zona.querySelector(".aviso-seleccion") : null;
            if (aviso) {
                aviso.classList.toggle("visible", input.files.length > 0);
            }
        });
    });

    // ---- 2) "Reemplazar": revela la zona de carga escondida ----
    document.querySelectorAll("[data-abre-carga]").forEach(function (boton) {
        boton.addEventListener("click", function () {
            var zona = document.getElementById(boton.dataset.abreCarga);
            if (!zona) {
                return;
            }
            zona.hidden = false;
            boton.hidden = true; // Ya se abrió: el botón dejaría de tener sentido
            zona.scrollIntoView({ behavior: "smooth", block: "center" });
            var archivo = zona.querySelector('input[type="file"]');
            if (archivo) {
                archivo.focus({ preventScroll: true });
            }
        });
    });
})();
