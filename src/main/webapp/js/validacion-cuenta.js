// Validación compartida de las pantallas de cuenta (restablecer contraseña y
// alta de usuarios).
//
// Cada campo de confirmación declara a quién debe parecerse con
// data-igual-a="#id-del-original". Se usa la validación nativa del navegador
// (setCustomValidity) para que el error salga antes de enviar y no después de
// un viaje al servidor. El servlet vuelve a compararlos de todos modos.
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-igual-a]").forEach(function (confirmacion) {
        var original = document.querySelector(confirmacion.getAttribute("data-igual-a"));
        if (!original) {
            return;
        }
        var mensaje = confirmacion.getAttribute("data-mensaje") || "Los datos no coinciden.";

        function revisar() {
            confirmacion.setCustomValidity(confirmacion.value === original.value ? "" : mensaje);
        }

        confirmacion.addEventListener("input", revisar);
        original.addEventListener("input", revisar);
    });
});
