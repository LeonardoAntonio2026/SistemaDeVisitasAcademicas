document.addEventListener("DOMContentLoaded", function () {
    var btnAgregarGrupo = document.getElementById("btn-agregar-grupo");
    var programasContainer = document.getElementById("programas-container");
    var plantillaFila = document.getElementById("tpl-programa-row");
    var catalogo = document.getElementById("catalogo-programas");
    var iniciales = document.getElementById("programas-iniciales");
    var resumenDivisiones = document.getElementById("division-resumen");
    var tagsWrapper = document.getElementById("tags-wrapper");
    var tagsInput = document.getElementById("tags-input");
    var programasMsg = document.getElementById("programas-msg");
    var programasText = document.getElementById("programas-msg-text");
    var asignaturasMsg = document.getElementById("asignaturas-msg");
    var asignaturasText = document.getElementById("asignaturas-msg-text");
    var form = document.querySelector("form[action='solicitud']");
    var acompWrapper = document.getElementById("acompanantes-wrapper");
    var acompInput = document.getElementById("acompanantes-input");
    var acompSugerencias = document.getElementById("acompanantes-sugerencias");

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

    // Cuántas asignaturas (chips) lleva capturadas el formulario
    function getAsignaturas() {
        return document.querySelectorAll('input[name="asignaturas"]').length;
    }

    // Pinta u oculta uno de los mensajes de error de abajo del campo
    function mostrarMensaje(caja, texto, mensaje) {
        if (!caja || !texto) {
            return;
        }
        if (mensaje) {
            texto.textContent = mensaje;
            caja.style.display = "flex";
        } else {
            caja.style.display = "none";
        }
    }

    // ===================== Desglose por programa educativo =====================
    // Cada fila es un grupo: división -> programa educativo -> cuatrimestre ->
    // grupo -> estudiantes. Todo sale de listas, y las opciones que ya se usaron
    // se descartan para que no se capture dos veces el mismo grupo.

    // Catálogo que dejó el JSP: [{division: "DATID", programa: "..."}, ...]
    var programasCatalogo = [];
    if (catalogo) {
        catalogo.querySelectorAll("span").forEach(function (item) {
            programasCatalogo.push({
                division: item.dataset.division,
                programa: item.dataset.programa
            });
        });
    }

    function filas() {
        return programasContainer
            ? Array.prototype.slice.call(programasContainer.querySelectorAll(".programa-row"))
            : [];
    }

    function campos(fila) {
        return {
            division: fila.querySelector(".campo-division"),
            programa: fila.querySelector(".campo-programa"),
            cuatrimestre: fila.querySelector(".campo-cuatrimestre"),
            grupo: fila.querySelector(".campo-grupo"),
            estudiantes: fila.querySelector(".campo-estudiantes")
        };
    }

    // Llena el select de programas con los de la división elegida
    function pintarProgramas(fila, programaElegido) {
        var c = campos(fila);
        var division = c.division.value;
        c.programa.innerHTML = "";

        var vacio = document.createElement("option");
        vacio.value = "";
        vacio.textContent = division ? "Elige el programa educativo…" : "Elige primero la división";
        c.programa.appendChild(vacio);

        programasCatalogo.forEach(function (item) {
            if (item.division !== division) {
                return;
            }
            var opcion = document.createElement("option");
            opcion.value = item.programa;
            opcion.textContent = item.programa;
            c.programa.appendChild(opcion);
        });

        if (programaElegido) {
            c.programa.value = programaElegido;
        }
    }

    // Descarta los grupos ya capturados para el mismo programa y cuatrimestre,
    // para no agregar dos veces el 5° A de la misma carrera
    function descartarGruposUsados() {
        // Quién tiene tomado cada grupo. Si dos filas chocan, la que pierde el
        // grupo es la que llegó después
        var tomados = {};
        filas().forEach(function (fila) {
            var c = campos(fila);
            if (!c.programa.value || !c.cuatrimestre.value || !c.grupo.value) {
                return;
            }
            var llave = c.programa.value + "|" + c.cuatrimestre.value + "|" + c.grupo.value;
            if (tomados[llave]) {
                c.grupo.value = "";
            } else {
                tomados[llave] = true;
            }
        });

        filas().forEach(function (fila) {
            var c = campos(fila);
            var prefijo = c.programa.value + "|" + c.cuatrimestre.value + "|";
            Array.prototype.forEach.call(c.grupo.options, function (opcion) {
                opcion.disabled = !!opcion.value
                    && opcion.value !== c.grupo.value
                    && !!tomados[prefijo + opcion.value];
            });
        });
    }

    // Suma los estudiantes de cada división a partir de los grupos capturados
    function pintarResumenDivisiones() {
        if (!resumenDivisiones) {
            return;
        }
        var porDivision = {};
        var total = 0;

        filas().forEach(function (fila) {
            var c = campos(fila);
            var cantidad = parseInt(c.estudiantes.value, 10) || 0;
            var division = c.division.value;
            if (!division || cantidad <= 0) {
                return;
            }
            porDivision[division] = (porDivision[division] || 0) + cantidad;
            total += cantidad;
        });

        resumenDivisiones.querySelectorAll("[data-division]").forEach(function (celda) {
            celda.textContent = porDivision[celda.dataset.division] || 0;
        });

        var celdaTotal = resumenDivisiones.querySelector("[data-division-total]");
        if (celdaTotal) {
            celdaTotal.textContent = total;
        }
    }

    // Todo lo que cambia cuando se toca una fila
    function refrescar() {
        descartarGruposUsados();
        pintarResumenDivisiones();
        validarProgramas();
    }

    function agregarFila(datos) {
        if (!programasContainer || !plantillaFila) {
            return null;
        }
        var fila = plantillaFila.content.firstElementChild.cloneNode(true);
        programasContainer.appendChild(fila);

        var c = campos(fila);
        if (datos && datos.programa) {
            // Fila que ya existía: la división se deduce del programa guardado
            var enCatalogo = programasCatalogo.find(function (item) {
                return item.programa === datos.programa;
            });
            if (enCatalogo) {
                c.division.value = enCatalogo.division;
                pintarProgramas(fila, datos.programa);
            } else {
                // Programa de una solicitud vieja capturada con texto libre:
                // se ofrece como opción para no perder el dato al editar
                var suelta = document.createElement("option");
                suelta.value = datos.programa;
                suelta.textContent = datos.programa + " (fuera del catálogo)";
                c.programa.appendChild(suelta);
                c.programa.value = datos.programa;
            }
            c.cuatrimestre.value = datos.cuatrimestre || "";
            c.grupo.value = datos.grupo || "";
            c.estudiantes.value = datos.estudiantes || "";
        }
        return fila;
    }

    // Debe haber al menos un grupo, y ninguno con un programa fuera del catálogo
    // (pasa al editar solicitudes viejas, capturadas cuando era texto libre)
    function validarProgramas() {
        var mensaje = "";
        var fueraDeCatalogo = filas().filter(function (fila) {
            var c = campos(fila);
            return c.programa.value && !c.division.value;
        }).length;

        if (filas().length === 0) {
            mensaje = "Agrega al menos un grupo en el desglose por programa educativo.";
        } else if (fueraDeCatalogo > 0) {
            mensaje = fueraDeCatalogo === 1
                ? "Un grupo tiene un programa educativo que ya no está en el catálogo. Vuelve a seleccionar su división académica y su programa."
                : "Hay " + fueraDeCatalogo + " grupos con programas educativos que ya no están en el catálogo. Vuelve a seleccionar su división académica y su programa.";
        }
        mostrarMensaje(programasMsg, programasText, mensaje);
        filas().forEach(function (fila) {
            var c = campos(fila);
            fila.classList.toggle("programa-row--revisar", !!c.programa.value && !c.division.value);
        });
        return !mensaje;
    }

    // Los chips de asignaturas no son un input required: se validan aparte
    function validarAsignaturas() {
        var mensaje = getAsignaturas() === 0
            ? "Agrega al menos una asignatura que se reforzará con la visita."
            : "";
        mostrarMensaje(asignaturasMsg, asignaturasText, mensaje);
        return !mensaje;
    }

    // Todo lo que el navegador no puede validar solo con required/pattern
    function validarFormulario() {
        // Se evalúan las dos para que se pinten todos los mensajes de una vez
        var programasOk = validarProgramas();
        var asignaturasOk = validarAsignaturas();
        return programasOk && asignaturasOk;
    }

    if (programasContainer && plantillaFila) {
        // Filas que ya existían (edición o vuelta con errores); si no hay, una vacía
        var hayIniciales = false;
        if (iniciales) {
            iniciales.querySelectorAll("span").forEach(function (item) {
                agregarFila({
                    programa: item.dataset.programa,
                    cuatrimestre: item.dataset.cuatrimestre,
                    grupo: item.dataset.grupo,
                    estudiantes: item.dataset.estudiantes
                });
                hayIniciales = true;
            });
        }
        if (!hayIniciales) {
            agregarFila(null);
        }
        refrescar();

        if (btnAgregarGrupo) {
            btnAgregarGrupo.addEventListener("click", function () {
                var fila = agregarFila(null);
                refrescar();
                if (fila) {
                    fila.querySelector(".campo-division").focus();
                }
            });
        }

        programasContainer.addEventListener("click", function (e) {
            var btn = e.target.closest(".btn-delete-row");
            if (!btn) {
                return;
            }
            var fila = btn.closest(".programa-row");
            if (fila) {
                fila.remove();
                refrescar();
            }
        });

        programasContainer.addEventListener("change", function (e) {
            var fila = e.target.closest(".programa-row");
            if (!fila) {
                return;
            }
            if (e.target.classList.contains("campo-division")) {
                pintarProgramas(fila, null);
            }
            refrescar();
        });

        // El número de estudiantes se escribe: el resumen se actualiza al teclear
        programasContainer.addEventListener("input", function (e) {
            if (e.target.classList.contains("campo-estudiantes")) {
                pintarResumenDivisiones();
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
            validarAsignaturas();
        });

        tagsWrapper.addEventListener("click", function (e) {
            if (e.target.classList.contains("tag-remove")) {
                var chip = e.target.closest(".tag-chip");
                if (chip) {
                    chip.remove();
                }
                validarAsignaturas();
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

    // No permite enviar mientras falte algo que el navegador no valida solo:
    // los grupos del desglose y las asignaturas
    if (form) {
        form.addEventListener("submit", function (e) {
            // Si escribió una asignatura y envió sin presionar Enter, la convertimos en chip
            if (tagsInput && tagsInput.value.trim()) {
                addTag(tagsInput.value.trim().replace(/,$/, ""));
                tagsInput.value = "";
            }

            if (!validarFormulario()) {
                e.preventDefault();
                var primero = [programasMsg, asignaturasMsg].find(function (caja) {
                    return caja && caja.style.display !== "none";
                });
                if (primero) {
                    primero.scrollIntoView({ behavior: "smooth", block: "center" });
                }
            }
        });
    }
});

// La rueda del ratón sobre un campo numérico enfocado le cambia el valor al
// bajar por el formulario: se cancela y se quita el foco. Va delegado al
// documento porque las filas de grupos se agregan dinámicamente.
document.addEventListener("wheel", function (e) {
    var campo = document.activeElement;
    if (campo && campo.type === "number" && campo === e.target) {
        e.preventDefault();
        campo.blur();
    }
}, { passive: false });
