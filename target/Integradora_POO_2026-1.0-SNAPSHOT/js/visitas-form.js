document.addEventListener("DOMContentLoaded", function () {
    var btnAgregarGrupo = document.getElementById("btn-agregar-grupo");
    var programasContainer = document.getElementById("programas-container");
    var tagsWrapper = document.getElementById("tags-wrapper");
    var tagsInput = document.getElementById("tags-input");
    var divisionInputs = document.querySelector(".division-inputs");

    function getTrashSvg() {
        return '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16"><path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/><path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/></svg>';
    }

    function addTag(text) {
        if (!tagsWrapper || !tagsInput || !text) {
            return;
        }
        var chip = document.createElement("span");
        chip.className = "tag-chip";
        chip.innerHTML = text + ' <button type="button" class="tag-remove" aria-label="Quitar">&times;</button>';
        tagsWrapper.insertBefore(chip, tagsInput);
    }

    if (btnAgregarGrupo && programasContainer) {
        btnAgregarGrupo.addEventListener("click", function () {
            var row = document.createElement("div");
            row.className = "programa-row";
            row.innerHTML =
                '<input type="text" class="form-control" placeholder="Ejemplo">' +
                '<input type="text" class="form-control" placeholder="5">' +
                '<input type="text" class="form-control" placeholder="A">' +
                '<input type="number" class="form-control" placeholder="4" min="0">' +
                '<button type="button" class="btn-delete-row" title="Eliminar fila">' + getTrashSvg() + "</button>";
            programasContainer.appendChild(row);
        });

        programasContainer.addEventListener("click", function (e) {
            var btn = e.target.closest(".btn-delete-row");
            if (btn) {
                var row = btn.closest(".programa-row");
                if (row) {
                    row.remove();
                }
            }
        });
    }

    if (tagsWrapper && tagsInput) {
        tagsWrapper.addEventListener("click", function () {
            tagsInput.focus();
        });

        tagsInput.addEventListener("keydown", function (e) {
            if ((e.key === "Enter" || e.key === ",") && this.value.trim()) {
                e.preventDefault();
                addTag(this.value.trim().replace(/,$/, ""));
                this.value = "";
            }
            if (e.key === "Backspace" && !this.value) {
                var chips = tagsWrapper.querySelectorAll(".tag-chip");
                if (chips.length) {
                    chips[chips.length - 1].remove();
                }
            }
        });

        tagsWrapper.addEventListener("click", function (e) {
            if (e.target.classList.contains("tag-remove")) {
                var chip = e.target.closest(".tag-chip");
                if (chip) {
                    chip.remove();
                }
            }
        });
    }

    if (divisionInputs) {
        divisionInputs.addEventListener("input", function () {
            var inputs = this.querySelectorAll('input[type="number"]:not(.division-total)');
            var total = 0;
            inputs.forEach(function (i) {
                total += parseInt(i.value, 10) || 0;
            });
            var totalInput = this.querySelector(".division-total");
            if (totalInput) {
                totalInput.value = total;
            }
        });
    }
});
