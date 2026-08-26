/**
 * Módulo de validación de coincidencia de campos para las pantallas de cuenta.
 *
 * Gestiona la validación del lado del cliente para la confirmación de contraseñas y otros datos,
 * asociando dinámicamente campos mediante el atributo 'data-igual-a' y aplicando
 * mensajes de error personalizados a través de la API nativa de validación del navegador.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
document.addEventListener("DOMContentLoaded", function () {
    // Validación compartida de las pantallas de cuenta (restablecer contraseña y
    // alta de usuarios).
    //
    // Cada campo de confirmación declara a quién debe parecerse con
    // data-igual-a="#id-del-original". Se usa la validación nativa del navegador
    // (setCustomValidity) para que el error salga antes de enviar y no después de
    // un viaje al servidor. El servlet vuelve a compararlos de todos modos.
    document.querySelectorAll("[data-igual-a]").forEach(function (confirmacion) {
        var original = document.querySelector(confirmacion.getAttribute("data-igual-a"));
        if (!original) {
            return;
        }
        var mensaje = confirmacion.getAttribute("data-mensaje") || "Los datos no coinciden.";

        /**
         * Evalúa la igualdad entre el campo original y el campo de confirmación.
         * Actualiza el estado de validez del campo utilizando setCustomValidity.
         *
         * @author Hugo Alberto Ramirez Martinez
         * @since 25/08/2026
         */
        function revisar() {
            confirmacion.setCustomValidity(confirmacion.value === original.value ? "" : mensaje);
        }

        confirmacion.addEventListener("input", revisar);
        original.addEventListener("input", revisar);
    });
});