/**
 * Solo atiende el atajo de teclado (Alt+Shift+L): le pide a la pestaña activa
 * que se llene con el último perfil usado.
 */
chrome.commands.onCommand.addListener(function (comando) {
    if (comando !== "autollenar") {
        return;
    }
    chrome.tabs.query({ active: true, currentWindow: true }, function (pestanas) {
        var pestana = pestanas[0];
        if (!pestana) {
            return;
        }
        // Si la pestaña no es el formulario no hay quien escuche: se ignora el error
        chrome.tabs.sendMessage(pestana.id, { tipo: "llenar" }, function () {
            void chrome.runtime.lastError;
        });
    });
});
