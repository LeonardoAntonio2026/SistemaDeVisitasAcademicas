/**
 * Lógica de llenado del formulario de Nueva Solicitud (SolicitudDocente.jsp).
 *
 * No toca la lógica de la página: escribe en los campos y dispara los mismos
 * eventos que dispararía una persona (input, change, Enter, mousedown sobre la
 * sugerencia), para que solicitud-form.js recalcule el resumen por división,
 * arme los chips y descarte los grupos repetidos igual que siempre.
 */
(function () {
    "use strict";

    /** Cuánto se espera a que el servidor conteste el autocompletado de docentes. */
    var ESPERA_SUGERENCIAS_MS = 4000;

    /** Campos de texto simples del formulario, en el orden en que aparecen. */
    var CAMPOS_TEXTO = [
        "nombreEmpresa",
        "direccionLugar",
        "telefonoContacto",
        "correoContacto",
        "objetivoVisita",
        "areaSolicitante",
        "docenteResponsable",
        "celularResponsable"
    ];

    /** El responsable ya viene con el docente en sesión: no se pisa si trae algo. */
    var SOLO_SI_ESTA_VACIO = ["docenteResponsable"];

    function formulario() {
        return document.getElementById("form-solicitud");
    }

    function hayFormulario() {
        return !!formulario();
    }

    function disparar(elemento, tipo) {
        elemento.dispatchEvent(new Event(tipo, { bubbles: true }));
    }

    /** Escribe un valor y avisa a la página como si se hubiera tecleado. */
    function escribir(elemento, valor) {
        if (!elemento) {
            return false;
        }
        elemento.value = valor;
        disparar(elemento, "input");
        disparar(elemento, "change");
        return true;
    }

    function campo(nombre) {
        var form = formulario();
        return form ? form.querySelector('[name="' + nombre + '"]') : null;
    }

    /** Fecha de hoy + días, en el formato yyyy-MM-dd que espera <input type="date">. */
    function fechaEnDias(dias) {
        var fecha = new Date();
        fecha.setDate(fecha.getDate() + (dias || 0));
        var mes = String(fecha.getMonth() + 1).padStart(2, "0");
        var dia = String(fecha.getDate()).padStart(2, "0");
        return fecha.getFullYear() + "-" + mes + "-" + dia;
    }

    /**
     * Fecha del perfil. Si trae una fecha fija y ya pasó, se ignora: el campo
     * tiene min="hoy" y el servidor rechaza las visitas en el pasado.
     */
    function fechaDelPerfil(perfil) {
        if (perfil.fechaInicio && perfil.fechaInicio >= fechaEnDias(0)) {
            return perfil.fechaInicio;
        }
        return fechaEnDias(perfil.diasDesdeHoy || 30);
    }

    // ===================== Grupos que participan =====================

    function filas() {
        var contenedor = document.getElementById("programas-container");
        return contenedor
            ? Array.prototype.slice.call(contenedor.querySelectorAll(".programa-row"))
            : [];
    }

    function agregarFila() {
        var boton = document.getElementById("btn-agregar-grupo");
        if (!boton) {
            return null;
        }
        boton.click();
        var lista = filas();
        return lista[lista.length - 1] || null;
    }

    function opcionDe(select, valor) {
        return Array.prototype.find.call(select.options, function (opcion) {
            return opcion.value === valor;
        });
    }

    /**
     * Llena una fila en el mismo orden que lo haría el docente: la división
     * primero, porque de ella dependen los programas educativos que se ofrecen.
     */
    function llenarFila(fila, grupo) {
        var division = fila.querySelector(".campo-division");
        var programa = fila.querySelector(".campo-programa");
        var cuatrimestre = fila.querySelector(".campo-cuatrimestre");
        var letra = fila.querySelector(".campo-grupo");
        var estudiantes = fila.querySelector(".campo-estudiantes");

        if (!opcionDe(division, grupo.division)) {
            return;
        }
        escribir(division, grupo.division);

        if (!opcionDe(programa, grupo.programa)) {
            return;
        }
        escribir(programa, grupo.programa);
        escribir(cuatrimestre, String(grupo.cuatrimestre));

        var opcionGrupo = opcionDe(letra, grupo.grupo);
        if (opcionGrupo && !opcionGrupo.disabled) {
            escribir(letra, grupo.grupo);
        }
        escribir(estudiantes, String(grupo.estudiantes));
    }

    function quitarFila(fila) {
        var boton = fila.querySelector(".btn-delete-row");
        if (boton) {
            boton.click();
        }
    }

    function vaciarFila(fila) {
        escribir(fila.querySelector(".campo-division"), "");
        escribir(fila.querySelector(".campo-cuatrimestre"), "");
        escribir(fila.querySelector(".campo-grupo"), "");
        escribir(fila.querySelector(".campo-estudiantes"), "");
    }

    // ===================== Chips (asignaturas y acompañantes) =====================

    function quitarChips(idWrapper) {
        var wrapper = document.getElementById(idWrapper);
        if (!wrapper) {
            return;
        }
        wrapper.querySelectorAll(".tag-chip .tag-remove").forEach(function (boton) {
            boton.click();
        });
    }

    /** El chip lo arma la página al recibir Enter, igual que si se escribiera. */
    function agregarAsignatura(texto) {
        var entrada = document.getElementById("tags-input");
        if (!entrada) {
            return;
        }
        entrada.value = texto;
        entrada.dispatchEvent(new KeyboardEvent("keydown", {
            key: "Enter",
            bubbles: true,
            cancelable: true
        }));
    }

    /** Espera a que el servidor conteste el autocompletado de docentes. */
    function esperarSugerencia(lista) {
        return new Promise(function (resolve) {
            var inicio = Date.now();
            (function revisar() {
                var item = lista.querySelector(".autocomplete-item");
                if (item) {
                    resolve(item);
                    return;
                }
                if (lista.querySelector(".autocomplete-vacio")) {
                    resolve(null);
                    return;
                }
                if (Date.now() - inicio > ESPERA_SUGERENCIAS_MS) {
                    resolve(null);
                    return;
                }
                setTimeout(revisar, 120);
            })();
        });
    }

    /**
     * Busca un docente y toma la primera sugerencia. Los ya elegidos no vuelven
     * a salir en la lista, así que repetir el mismo texto agrega a otro docente.
     */
    async function agregarAcompanante(texto) {
        var entrada = document.getElementById("acompanantes-input");
        var lista = document.getElementById("acompanantes-sugerencias");
        if (!entrada || !lista) {
            return;
        }

        // Se limpia la lista anterior para no quedarnos con una sugerencia vieja
        lista.innerHTML = "";
        lista.classList.remove("visible");

        entrada.focus();
        entrada.value = texto;
        disparar(entrada, "input");

        var item = await esperarSugerencia(lista);
        if (!item) {
            entrada.value = "";
            entrada.blur();
            return;
        }
        item.dispatchEvent(new MouseEvent("mousedown", { bubbles: true, cancelable: true }));
    }

    // ===================== Acciones públicas =====================

    /** Deja el formulario como recién abierto: sin chips y con una sola fila vacía. */
    function limpiar() {
        CAMPOS_TEXTO.forEach(function (nombre) {
            escribir(campo(nombre), "");
        });
        escribir(campo("fechaInicio"), "");

        quitarChips("tags-wrapper");
        quitarChips("acompanantes-wrapper");

        var lista = filas();
        lista.forEach(function (fila, indice) {
            if (indice === 0) {
                vaciarFila(fila);
            } else {
                quitarFila(fila);
            }
        });
    }

    /** Llena todo el formulario con un perfil de presets.js. */
    async function llenar(perfil) {
        CAMPOS_TEXTO.forEach(function (nombre) {
            var valor = perfil.campos[nombre];
            if (valor === undefined || valor === null) {
                return;
            }
            var elemento = campo(nombre);
            if (!elemento) {
                return;
            }
            if (SOLO_SI_ESTA_VACIO.indexOf(nombre) !== -1 && elemento.value.trim()) {
                return;
            }
            escribir(elemento, valor);
        });

        escribir(campo("fechaInicio"), fechaDelPerfil(perfil));

        // Grupos: se reutilizan las filas que ya estaban y se quitan las sobrantes
        var existentes = filas();
        (perfil.grupos || []).forEach(function (grupo, indice) {
            var fila = existentes[indice] || agregarFila();
            if (fila) {
                llenarFila(fila, grupo);
            }
        });
        filas().slice((perfil.grupos || []).length).forEach(quitarFila);

        quitarChips("tags-wrapper");
        (perfil.asignaturas || []).forEach(agregarAsignatura);

        quitarChips("acompanantes-wrapper");
        for (var i = 0; i < (perfil.acompanantes || []).length; i++) {
            await agregarAcompanante(perfil.acompanantes[i]);
        }
    }

    globalThis.SVAAutollenado = {
        hayFormulario: hayFormulario,
        llenar: llenar,
        limpiar: limpiar
    };
})();
