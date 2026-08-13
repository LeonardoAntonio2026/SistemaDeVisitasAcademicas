// Lógica del detalle del reporte de visita (reporte-detalle.jsp).
// Los casos del JSP son mutuamente excluyentes, así que cada bloque
// se activa solo si sus elementos existen en la página.
document.addEventListener("DOMContentLoaded", function () {

    // ============================================================
    // Formulario de captura: galería con exactamente 3 imágenes.
    // Las ya guardadas vienen renderizadas por el servidor (tienen
    // data-id-imagen); quitarlas agrega un hidden eliminarImagen=ID.
    // Las nuevas se acumulan en memoria y se sincronizan al enviar.
    // ============================================================
    var form = document.getElementById("form-reporte");
    var galeria = document.getElementById("galeria-previa");
    var btnAgregar = document.getElementById("btn-agregar-imagen");
    var inputOculto = document.getElementById("input-imagen-oculto");

    if (form && galeria && btnAgregar && inputOculto) {
        var IMAGENES_REQUERIDAS = 3;
        var MAX_BYTES = 5 * 1024 * 1024;
        var archivosNuevos = []; // File objects seleccionados

        function totalImagenes() {
            var existentes = galeria.querySelectorAll(".galeria-item[data-id-imagen]").length;
            return existentes + archivosNuevos.length;
        }

        // Los avisos de la galería salen en el modal del sistema (js/modales.js),
        // igual que las confirmaciones del resto del sitio.
        function avisar(mensaje, titulo) {
            window.Modales.avisar({
                titulo: titulo || "No se puede agregar la imagen",
                mensaje: mensaje,
                tipo: "aviso"
            });
        }

        btnAgregar.addEventListener("click", function () {
            if (totalImagenes() >= IMAGENES_REQUERIDAS) {
                avisar("El reporte lleva exactamente " + IMAGENES_REQUERIDAS + " imágenes. Quita una para poder agregar otra.");
                return;
            }
            inputOculto.click();
        });

        inputOculto.addEventListener("change", function () {
            var file = inputOculto.files[0];
            inputOculto.value = "";
            if (!file) {
                return;
            }
            if (["image/jpeg", "image/png"].indexOf(file.type) === -1) {
                avisar("Solo se permiten imágenes JPG o PNG.", "Formato no admitido");
                return;
            }
            if (file.size > MAX_BYTES) {
                avisar("Cada imagen debe pesar máximo 5 MB.", "La imagen pesa demasiado");
                return;
            }
            if (totalImagenes() >= IMAGENES_REQUERIDAS) {
                avisar("El reporte lleva exactamente " + IMAGENES_REQUERIDAS + " imágenes.");
                return;
            }
            archivosNuevos.push(file);
            renderNuevas();
        });

        // Quitar una imagen ya guardada: se oculta y se marca para borrar
        galeria.addEventListener("click", function (e) {
            var btn = e.target.closest(".quitar-imagen");
            if (!btn) {
                return;
            }
            var item = btn.closest(".galeria-item");
            var idImagen = item.getAttribute("data-id-imagen");
            if (idImagen) {
                var hidden = document.createElement("input");
                hidden.type = "hidden";
                hidden.name = "eliminarImagen";
                hidden.value = idImagen;
                form.appendChild(hidden);
                item.remove();
            }
            actualizarBoton();
        });

        function renderNuevas() {
            // Solo se re-renderizan las nuevas; las existentes las pintó el servidor
            galeria.querySelectorAll(".galeria-item:not([data-id-imagen])").forEach(function (n) {
                n.remove();
            });
            archivosNuevos.forEach(function (file, idx) {
                var item = document.createElement("div");
                item.className = "galeria-item";

                var img = document.createElement("img");
                img.src = URL.createObjectURL(file);
                item.appendChild(img);

                var btnQuitar = document.createElement("button");
                btnQuitar.type = "button";
                btnQuitar.className = "quitar-imagen";
                btnQuitar.innerHTML = "&times;";
                btnQuitar.addEventListener("click", function () {
                    archivosNuevos.splice(idx, 1);
                    renderNuevas();
                });
                item.appendChild(btnQuitar);

                galeria.appendChild(item);
            });
            actualizarBoton();
        }

        function actualizarBoton() {
            btnAgregar.disabled = totalImagenes() >= IMAGENES_REQUERIDAS;
        }

        form.addEventListener("submit", function (e) {
            if (totalImagenes() !== IMAGENES_REQUERIDAS) {
                e.preventDefault();
                window.Modales.avisar({
                    titulo: "Faltan fotografías",
                    mensaje: "El reporte debe llevar exactamente " + IMAGENES_REQUERIDAS +
                             " imágenes de la visita.",
                    tipo: "aviso"
                }, function () {
                    btnAgregar.focus();
                });
                return;
            }
            // Sincronizamos las nuevas a un input real name="imagenes"
            var dt = new DataTransfer();
            archivosNuevos.forEach(function (file) {
                dt.items.add(file);
            });
            var inputFinal = document.getElementById("input-imagenes-final");
            if (!inputFinal) {
                inputFinal = document.createElement("input");
                inputFinal.type = "file";
                inputFinal.id = "input-imagenes-final";
                inputFinal.name = "imagenes";
                inputFinal.multiple = true;
                inputFinal.style.display = "none";
                form.appendChild(inputFinal);
            }
            inputFinal.files = dt.files;
        });

        actualizarBoton();
    }

    // Las confirmaciones de evaluar y de enviar el reporte ya no viven aquí:
    // van como data-confirmar en reporte-detalle.jsp y las atiende
    // js/modales.js, el mismo que usan la solicitud y su detalle.

    // Al elegir archivo se avisa que falta dar click en Subir (elegir != subir)
    document.querySelectorAll(".zona-carga input[type='file']").forEach(function (input) {
        input.addEventListener("change", function () {
            var aviso = input.closest(".zona-carga").querySelector(".aviso-seleccion");
            if (aviso) {
                aviso.classList.toggle("visible", input.files.length > 0);
            }
        });
    });
});
