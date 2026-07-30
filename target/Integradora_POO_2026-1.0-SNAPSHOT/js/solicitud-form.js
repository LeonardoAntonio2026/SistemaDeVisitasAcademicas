document.addEventListener("DOMContentLoaded", function () {
    var btnAgregarGrupo = document.getElementById("btn-agregar-grupo");
    var programasContainer = document.getElementById("programas-container");
    var tagsWrapper = document.getElementById("tags-wrapper");
    var tagsInput = document.getElementById("tags-input");
    var divisionInputs = document.querySelector(".division-inputs");
    var mismatchMsg = document.getElementById("division-mismatch-msg");
    var mismatchText = document.getElementById("division-mismatch-text");
    var form = document.querySelector("form[action='solicitud']");
    var acompWrapper = document.getElementById("acompanantes-wrapper");
    var acompInput = document.getElementById("acompanantes-input");
    var acompSugerencias = document.getElementById("acompanantes-sugerencias");

    function getTrashSvg() {
        return '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16"><path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/><path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1 0-2h3.171a1 1 0 0 1 .707.293L7.5 3h1l.621-.707A1 1 0 0 1 9.829 2H13a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3h11a.5.5 0 0 0 0-1h-11a.5.5 0 0 0 0 1z"/></svg>';
    }

    // Cada chip lleva un input hidden name="asignaturas" para que el valor llegue al servlet
    function addTag(text) {
        if (!tagsWrapper || !tagsInput || !text) {
            return;
        }
        var chip = document.createElement("span");
        chip.className = "tag-chip";
        chip.innerHTML = text + ' <button type="button" class="tag-remove" aria-label="Quitar">&times;</button>';

        var hidden = document.createElement("input");
        hidden.type = "hidden";
        hidden.name = "asignaturas";
        hidden.value = text;
        chip.appendChild(hidden);

        tagsWrapper.insertBefore(chip, tagsInput);
    }

    // Suma el número de estudiantes capturado por división académica (DACEA, DATEFI, DATID, DAMI)
    function getDivisionTotal() {
        if (!divisionInputs) {
            return 0;
        }
        var total = 0;
        divisionInputs.querySelectorAll('input[type="number"]:not(.division-total)').forEach(function (i) {
            total += parseInt(i.value, 10) || 0;
        });
        return total;
    }

    // Suma el número de estudiantes capturado en el desglose por programa educativo
    function getProgramasTotal() {
        if (!programasContainer) {
            return 0;
        }
        var total = 0;
        programasContainer.querySelectorAll('input[name="numEstudiantesGrupo"]').forEach(function (i) {
            total += parseInt(i.value, 10) || 0;
        });
        return total;
    }

    // Verifica que el total por división académica coincida con el total del desglose por programa
    function validarTotales() {
        if (!mismatchMsg || !mismatchText) {
            return true;
        }
        var totalDivision = getDivisionTotal();
        var totalProgramas = getProgramasTotal();
        var coinciden = totalDivision === totalProgramas;

        if (coinciden) {
            mismatchMsg.style.display = "none";
        } else {
            mismatchText.textContent = "El total de estudiantes por división académica (" + totalDivision +
                ") no coincide con el total del desglose por programa educativo (" + totalProgramas + ").";
            mismatchMsg.style.display = "flex";
        }
        return coinciden;
    }

    if (btnAgregarGrupo && programasContainer) {
        btnAgregarGrupo.addEventListener("click", function () {
            var row = document.createElement("div");
            row.className = "programa-row";
            row.innerHTML =
                '<input type="text" name="programaEducativo" class="form-control" placeholder="Ejemplo">' +
                '<input type="number" name="cuatrimestre" class="form-control" placeholder="5" min="1" max="11">' +
                '<input type="text" name="grupo" class="form-control" placeholder="A">' +
                '<input type="number" name="numEstudiantesGrupo" class="form-control" placeholder="4" min="0">' +
                '<button type="button" class="btn-delete-row" title="Eliminar fila">' + getTrashSvg() + "</button>";
            programasContainer.appendChild(row);
            validarTotales();
        });

        programasContainer.addEventListener("click", function (e) {
            var btn = e.target.closest(".btn-delete-row");
            if (btn) {
                var row = btn.closest(".programa-row");
                if (row) {
                    row.remove();
                    validarTotales();
                }
            }
        });

        programasContainer.addEventListener("input", validarTotales);
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

    // ===================== Docentes acompañantes =====================
    // Se escribe el nombre, el servidor sugiere docentes registrados y al dar
    // Enter (o click) se agrega un chip con el id del docente en un hidden.
    if (acompWrapper && acompInput && acompSugerencias) {
        var script = document.querySelector('script[src$="solicitud-form.js"]');
        var contextPath = script ? script.getAttribute("src").replace("/js/solicitud-form.js", "") : "";
        var sugerencias = [];
        var resaltado = -1;
        var peticion = null;

        function idsSeleccionados() {
            var ids = [];
            acompWrapper.querySelectorAll('input[name="docentesAcompanantes"]').forEach(function (i) {
                ids.push(i.value);
            });
            return ids;
        }

        function agregarAcompanante(docente) {
            if (idsSeleccionados().indexOf(String(docente.id)) !== -1) {
                return; // Ya estaba en la lista
            }
            var chip = document.createElement("span");
            chip.className = "tag-chip";
            chip.appendChild(document.createTextNode(docente.nombre + " "));

            var quitar = document.createElement("button");
            quitar.type = "button";
            quitar.className = "tag-remove";
            quitar.setAttribute("aria-label", "Quitar");
            quitar.innerHTML = "&times;";
            chip.appendChild(quitar);

            var hidden = document.createElement("input");
            hidden.type = "hidden";
            hidden.name = "docentesAcompanantes";
            hidden.value = docente.id;
            chip.appendChild(hidden);

            acompWrapper.insertBefore(chip, acompInput);
        }

        function cerrarSugerencias() {
            acompSugerencias.innerHTML = "";
            acompSugerencias.classList.remove("visible");
            sugerencias = [];
            resaltado = -1;
        }

        function pintarSugerencias(lista) {
            acompSugerencias.innerHTML = "";
            sugerencias = lista;
            resaltado = -1;

            if (!lista.length) {
                var vacio = document.createElement("div");
                vacio.className = "autocomplete-vacio";
                vacio.textContent = "No se encontraron docentes con ese nombre";
                acompSugerencias.appendChild(vacio);
                acompSugerencias.classList.add("visible");
                return;
            }

            lista.forEach(function (docente, indice) {
                var item = document.createElement("div");
                item.className = "autocomplete-item";
                item.dataset.indice = indice;

                var nombre = document.createElement("span");
                nombre.className = "autocomplete-nombre";
                nombre.textContent = docente.nombre;
                item.appendChild(nombre);

                var correo = document.createElement("small");
                correo.textContent = docente.correo;
                item.appendChild(correo);

                item.addEventListener("mousedown", function (e) {
                    e.preventDefault(); // Evita que el input pierda el foco antes del click
                    agregarAcompanante(docente);
                    acompInput.value = "";
                    cerrarSugerencias();
                });
                acompSugerencias.appendChild(item);
            });
            acompSugerencias.classList.add("visible");
        }

        function marcarResaltado() {
            acompSugerencias.querySelectorAll(".autocomplete-item").forEach(function (item, indice) {
                item.classList.toggle("resaltado", indice === resaltado);
            });
        }

        function buscarDocentes(texto) {
            if (peticion) {
                clearTimeout(peticion);
            }
            if (texto.length < 2) {
                cerrarSugerencias();
                return;
            }
            // Pequeño retraso para no pegarle al servidor en cada tecla
            peticion = setTimeout(function () {
                fetch(contextPath + "/docentes?q=" + encodeURIComponent(texto))
                    .then(function (r) { return r.ok ? r.json() : []; })
                    .then(function (lista) {
                        var ids = idsSeleccionados();
                        pintarSugerencias(lista.filter(function (d) {
                            return ids.indexOf(String(d.id)) === -1;
                        }));
                    })
                    .catch(function () { cerrarSugerencias(); });
            }, 250);
        }

        acompWrapper.addEventListener("click", function (e) {
            if (e.target.classList.contains("tag-remove")) {
                var chip = e.target.closest(".tag-chip");
                if (chip) {
                    chip.remove();
                }
                return;
            }
            acompInput.focus();
        });

        acompInput.addEventListener("input", function () {
            buscarDocentes(this.value.trim());
        });

        acompInput.addEventListener("keydown", function (e) {
            if (e.key === "ArrowDown" && sugerencias.length) {
                e.preventDefault();
                resaltado = (resaltado + 1) % sugerencias.length;
                marcarResaltado();
            } else if (e.key === "ArrowUp" && sugerencias.length) {
                e.preventDefault();
                resaltado = (resaltado <= 0 ? sugerencias.length : resaltado) - 1;
                marcarResaltado();
            } else if (e.key === "Enter") {
                // Enter nunca envía el formulario desde aquí: agrega el docente
                e.preventDefault();
                var elegido = sugerencias[resaltado >= 0 ? resaltado : 0];
                if (elegido) {
                    agregarAcompanante(elegido);
                    this.value = "";
                    cerrarSugerencias();
                }
            } else if (e.key === "Escape") {
                cerrarSugerencias();
            } else if (e.key === "Backspace" && !this.value) {
                var chips = acompWrapper.querySelectorAll(".tag-chip");
                if (chips.length) {
                    chips[chips.length - 1].remove();
                }
            }
        });

        acompInput.addEventListener("blur", function () {
            setTimeout(cerrarSugerencias, 120);
        });
    }

    // Si el usuario escribió una asignatura y envió sin presionar Enter, la convertimos en chip
    if (form && tagsInput) {
        form.addEventListener("submit", function () {
            if (tagsInput.value.trim()) {
                addTag(tagsInput.value.trim().replace(/,$/, ""));
                tagsInput.value = "";
            }
        });
    }

    function pintarDivisionTotal() {
        if (!divisionInputs) {
            return;
        }
        var totalInput = divisionInputs.querySelector(".division-total");
        if (totalInput) {
            totalInput.value = getDivisionTotal();
        }
    }

    if (divisionInputs) {
        divisionInputs.addEventListener("input", function () {
            pintarDivisionTotal();
            validarTotales();
        });
        // En edición los inputs ya vienen con valores: el total se pinta al cargar
        pintarDivisionTotal();
    }

    // No permite enviar el formulario si los dos totales de estudiantes no coinciden
    if (form) {
        form.addEventListener("submit", function (e) {
            if (!validarTotales()) {
                e.preventDefault();
                mismatchMsg.scrollIntoView({ behavior: "smooth", block: "center" });
            }
        });
    }

    validarTotales();
});
