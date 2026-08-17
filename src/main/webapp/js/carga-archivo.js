/* ============================================================
   Zonas de carga de archivos (detalle de la solicitud y del
   reporte). Dos cosas:

   1) Elegir un archivo NO es subirlo: al elegirlo se avisa que
      todavía falta dar click en el botón.
   2) Cuando el archivo ya está en el sistema la zona de carga
      nace oculta —si no, el recuadro punteado se lee como
      "todavía te falta subir algo"— y se abre con el botón
      "Reemplazar" que está junto al de descargar.
   ============================================================ */
(function () {
    "use strict";

    // ---- 1) Aviso "seleccionado pero sin subir" ----
    document.querySelectorAll('.zona-carga input[type="file"]').forEach(function (input) {
        input.addEventListener("change", function () {
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
