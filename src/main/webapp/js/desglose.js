/**
 * @file desglose.js — Desglose académico de una solicitud, por mensajes JSON.
 *
 * Administra las tres entidades que cuelgan de la solicitud: docentes
 * acompañantes, grupos que participan y asignaturas a reforzar.
 *
 * La pantalla es la misma que la sección "Datos de los participantes" del
 * formulario de la solicitud, así que se maneja igual: las filas del desglose
 * se editan en su lugar y los chips se agregan escribiendo. La diferencia está
 * abajo: allá todo espera al botón de guardar, aquí cada cambio sale solo como
 * un mensaje contra /desglose.
 *
 *     { "accion": "listar|crear|actualizar|eliminar",
 *       "entidad": "grupo|asignatura|docente",
 *       "idSolicitud": 7,
 *       "datos": { ... } }
 *
 * y la respuesta siempre trae el desglose COMPLETO ya con el cambio aplicado:
 *
 *     { "ok": true, "mensaje": "...", "foInvalidado": false,
 *       "desglose": { "grupos": [...], "asignaturas": [...], "docentes": [...],
 *                     "totalEstudiantes": 80, "divisiones": [...] } }
 *
 * Por eso aquí no se lleva ningún estado propio: cada respuesta repinta las
 * tres listas desde cero. Lo que se ve es siempre lo que hay en la base, nunca
 * una suposición de lo que debió pasar. La primera carga usa el mismo camino,
 * con accion "listar".
 *
 * La validación de verdad la hace DesgloseServlet, con las mismas reglas que el
 * formulario completo. Aquí solo se decide CUÁNDO vale la pena mandar el
 * mensaje: una fila a medio capturar no se manda.
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 25/08/2026
 */
document.addEventListener("DOMContentLoaded", function () {
    "use strict";

    var ERROR_RED = "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.";
    var ERROR_PERMISO = "Ya no puedes cambiar el desglose de esta solicitud. "
        + "Recarga la página para ver cómo quedó.";

    var config = document.querySelector("script[data-solicitud]").dataset;
    var ID_SOLICITUD = Number(config.solicitud);
    var URL_DESGLOSE = config.url;
    var URL_DOCENTES = config.docentes;

    // ===================== Estado de la pantalla =====================

    // Un solo lugar dice qué está pasando: el renglón bajo el título. El JSP lo
    // deja ya en "Cargando el desglose…", así que no hay un momento en blanco
    // entre que aparece la página y arranca este script.
    var contenido = document.getElementById("desglose");
    var estadoCaja = document.getElementById("estado-desglose");
    var estadoIcono = document.getElementById("estado-icono");
    var estadoTexto = document.getElementById("estado-texto");

    var ICONOS = {
        cargando: "bi-arrow-repeat girando",
        exito: "bi-check-circle",
        error: "bi-exclamation-triangle"
    };

    /**
     * Escribe el estado bajo el título.
     *
     * @param {?string} tipo cargando, exito, error, o null para dejarlo en blanco
     * @param {string} [texto] qué decir
     * @returns {void}
     */
    function marcarEstado(tipo, texto) {
        estadoTexto.textContent = tipo ? texto : "";
        estadoIcono.className = tipo ? "bi " + ICONOS[tipo] : "";
        estadoIcono.hidden = !tipo;
        estadoCaja.className = "desglose-estado"
            + (tipo === "exito" ? " desglose-estado--exito" : "")
            + (tipo === "error" ? " desglose-estado--error" : "");
    }

    /**
     * Atenúa el contenido y lo deja de aceptar clics mientras el servidor
     * contesta. Se ve que algo está pasando y, de paso, no se puede tocar otra
     * fila con un cambio a medio guardar.
     *
     * @param {boolean} activo true mientras haya una petición en curso
     * @returns {void}
     */
    function ocupado(activo) {
        contenido.classList.toggle("ocupado", activo);
    }

    // ===================== Mensajes al servlet =====================

    var recargando = false;

    /**
     * Convierte la respuesta HTTP en el mensaje JSON del servlet.
     *
     * Dos casos no son mensajes y se traducen a uno: si la sesión caducó,
     * FiltroAutenticacion redirige al login y lo que llega es HTML; si la
     * solicitud dejó de ser editable (se envió desde otra pestaña), el servlet
     * responde 403 y tampoco hay JSON que leer.
     *
     * @param {Response} respuesta respuesta cruda de fetch
     * @returns {Object|Promise<Object>} el mensaje del servlet
     */
    function leerMensaje(respuesta) {
        if (respuesta.redirected) {
            recargando = true;
            window.location.reload();
        }
        if (respuesta.status === 403) {
            return { ok: false, mensaje: ERROR_PERMISO };
        }
        return respuesta.json();
    }

    /**
     * Manda un mensaje al servlet y repinta con lo que conteste.
     *
     * Todas las operaciones pasan por aquí, así que el estado de guardado y el
     * repintado se resuelven en un solo lugar. Quien llama solo se ocupa de lo
     * suyo: dónde enseñar el error si algo se rechaza.
     *
     * @param {string} accion listar, crear, actualizar o eliminar
     * @param {?string} entidad grupo, asignatura o docente
     * @param {?Object} datos contenido que pide esa operación
     * @param {?Object} caja sección donde mostrar el error, o null para el pie
     * @returns {Promise<boolean>} true si la operación se aplicó
     */
    function enviar(accion, entidad, datos, caja) {
        ocupado(true);
        // "listar" es la carga de la pantalla, no un cambio: decir "Guardando…"
        // ahí haría creer que se tocó algo nada más entrar
        marcarEstado("cargando", accion === "listar" ? "Cargando el desglose…" : "Guardando…");

        return fetch(URL_DESGLOSE, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                accion: accion,
                entidad: entidad || null,
                idSolicitud: ID_SOLICITUD,
                datos: datos || {}
            })
        }).then(leerMensaje).then(function (r) {
            ocupado(false);

            if (!r.ok) {
                // El motivo va pegado a la sección que lo provocó, que es donde
                // está mirando quien lo tiene que corregir; arriba solo se dice
                // que no se guardó, para que el indicador no mienta
                avisar(caja, r.mensaje);
                marcarEstado("error", caja ? "No se guardó el cambio." : r.mensaje);
                return false;
            }

            avisar(caja, "");
            if (r.desglose) {
                pintarDesglose(r.desglose);
            }
            if (r.foInvalidado) {
                avisarFormatoDadoDeBaja();
            }
            marcarEstado(r.mensaje ? "exito" : null, r.mensaje);
            return true;
        }).catch(function () {
            ocupado(false);
            if (recargando) {
                return false; // la sesión caducó y la página ya se está recargando
            }
            avisar(caja, ERROR_RED);
            marcarEstado("error", caja ? "No se guardó el cambio." : ERROR_RED);
            return false;
        });
    }

    // ===================== Avisos =====================

    /**
     * Pasa a tiempo pasado el aviso de arriba, la primera vez que un cambio da
     * de baja el formato firmado.
     *
     * No se quita después aunque se hagan más cambios: mientras no suba el
     * formato nuevo, sigue siendo cierto.
     *
     * @returns {void}
     */
    function avisarFormatoDadoDeBaja() {
        document.getElementById("aviso-formato-hecho").hidden = false;
    }

    /**
     * Muestra u oculta el mensaje de error de una sección.
     *
     * Va pegado a la sección que lo provocó, como los avisos de validación del
     * formulario: un error del desglose de grupos no tiene por qué salir hasta
     * arriba de la página, lejos de la fila que hay que corregir.
     *
     * @param {?Object} caja par {caja, texto} de la sección, o null para el pie
     * @param {string} mensaje texto del servidor; vacío oculta el aviso
     * @returns {void}
     */
    function avisar(caja, mensaje) {
        if (!caja) {
            return; // la carga inicial no tiene sección propia: solo el estado de arriba
        }
        caja.texto.textContent = mensaje;
        caja.caja.style.display = mensaje ? "" : "none";
    }

    /**
     * Arma el par de nodos de un aviso de sección.
     *
     * @param {string} id identificador base, sin el sufijo -msg
     * @returns {Object} el par {caja, texto}
     */
    function avisoDe(id) {
        return {
            caja: document.getElementById(id + "-msg"),
            texto: document.getElementById(id + "-msg-text")
        };
    }

    var avisoGrupos = avisoDe("programas");
    var avisoAsignaturas = avisoDe("asignaturas");
    var avisoDocentes = avisoDe("acompanantes");

    // ===================== Grupos que participan =====================

    var programasContainer = document.getElementById("programas-container");
    var tplFila = document.getElementById("tpl-programa-row");
    var btnAgregarGrupo = document.getElementById("btn-agregar-grupo");
    var resumenDivisiones = document.getElementById("division-resumen");
    var sinGrupos = document.getElementById("sin-grupos");

    // Catálogo que dejó el JSP: [{division: "DATID", programa: "..."}, ...]
    var programasCatalogo = [];
    document.getElementById("catalogo-programas").querySelectorAll("span").forEach(function (item) {
        programasCatalogo.push({ division: item.dataset.division, programa: item.dataset.programa });
    });

    // La fila que se está capturando y todavía no existe en la base. Se guarda
    // aparte porque cada respuesta del servlet vacía el contenedor: sin esto,
    // guardar un cambio en otra fila borraría lo que se llevaba escrito aquí.
    var borrador = null;

    function campos(fila) {
        return {
            division: fila.querySelector(".campo-division"),
            programa: fila.querySelector(".campo-programa"),
            cuatrimestre: fila.querySelector(".campo-cuatrimestre"),
            grupo: fila.querySelector(".campo-grupo"),
            estudiantes: fila.querySelector(".campo-estudiantes")
        };
    }

    /**
     * Llena el select de programas con los de la división elegida.
     *
     * @param {HTMLElement} fila fila del desglose
     * @param {?string} programaElegido programa a dejar seleccionado
     * @returns {void}
     */
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

    /**
     * Lo capturado en una fila, listo para el mensaje.
     *
     * @param {HTMLElement} fila fila del desglose
     * @returns {Object} datos del grupo
     */
    function datosDeFila(fila) {
        var c = campos(fila);
        return {
            programa: c.programa.value,
            cuatrimestre: Number(c.cuatrimestre.value) || null,
            grupo: c.grupo.value,
            noEstudiantes: Number(c.estudiantes.value) || null
        };
    }

    /**
     * Indica si la fila ya tiene con qué mandarse.
     *
     * Una fila a medio capturar no se manda: el servidor la rechazaría con un
     * "elige el programa educativo" cada vez que se toca un select, y el aviso
     * saldría mientras la persona todavía está llenándola.
     *
     * @param {Object} datos lo devuelto por datosDeFila
     * @returns {boolean} true si están los cuatro campos
     */
    function filaCompleta(datos) {
        return !!(datos.programa && datos.cuatrimestre && datos.grupo && datos.noEstudiantes);
    }

    /** Huella de lo capturado, para no mandar un cambio que no cambió nada. */
    function firmaDe(datos) {
        return [datos.programa, datos.cuatrimestre, datos.grupo, datos.noEstudiantes].join("|");
    }

    /**
     * Crea una fila del desglose.
     *
     * @param {?Object} grupo grupo que viene del servlet, o null para una fila nueva
     * @returns {HTMLElement} la fila lista para insertarse
     */
    function crearFila(grupo) {
        var fila = tplFila.content.firstElementChild.cloneNode(true);
        if (!grupo) {
            return fila;
        }
        var c = campos(fila);
        fila.dataset.id = grupo.id;
        // La división no se guarda en la base: sale del catálogo a partir del
        // programa, y el servlet ya la mandó resuelta en el mensaje
        c.division.value = grupo.division;
        pintarProgramas(fila, grupo.programa);
        c.cuatrimestre.value = grupo.cuatrimestre;
        c.grupo.value = grupo.grupo || "";
        c.estudiantes.value = grupo.noEstudiantes;
        fila.dataset.firma = firmaDe(datosDeFila(fila));
        return fila;
    }

    /**
     * Vuelve a pintar las filas con los grupos guardados, conservando la fila
     * que se esté capturando.
     *
     * @param {Array<Object>} grupos grupos del mensaje
     * @returns {void}
     */
    function pintarGrupos(grupos) {
        programasContainer.innerHTML = "";
        grupos.forEach(function (grupo) {
            programasContainer.appendChild(crearFila(grupo));
        });
        if (borrador) {
            programasContainer.appendChild(borrador);
        }
        // Una sola fila nueva a la vez: dos borradores a medias no se sabría
        // cuál se está capturando, y ninguno se guarda hasta estar completo
        btnAgregarGrupo.disabled = !!borrador;
        // Sin esto, "no hay ningún grupo" y "todavía no carga" se ven igual
        sinGrupos.hidden = grupos.length > 0 || !!borrador;
    }

    /**
     * Pinta el resumen por división con lo que calculó el servidor.
     *
     * @param {Array<Object>} divisiones pares sigla/estudiantes del mensaje
     * @param {number} total suma de todos los grupos
     * @returns {void}
     */
    function pintarResumen(divisiones, total) {
        var porDivision = {};
        divisiones.forEach(function (division) {
            porDivision[division.sigla] = division.estudiantes;
        });
        resumenDivisiones.querySelectorAll("[data-division]").forEach(function (celda) {
            celda.textContent = porDivision[celda.dataset.division] || 0;
        });
        resumenDivisiones.querySelector("[data-division-total]").textContent = total;
    }

    btnAgregarGrupo.addEventListener("click", function () {
        borrador = crearFila(null);
        programasContainer.appendChild(borrador);
        btnAgregarGrupo.disabled = true;
        sinGrupos.hidden = true;
        borrador.querySelector(".campo-division").focus();
    });

    // Los escuchas van en el contenedor y no en cada fila: las filas se vuelven
    // a crear con cada respuesta del servlet, así que engancharlos una por una
    // los dejaría muertos desde el primer cambio.
    programasContainer.addEventListener("change", function (e) {
        var fila = e.target.closest(".programa-row");
        if (!fila) {
            return;
        }
        // Cambiar la división deja sin sentido el programa que estaba elegido
        if (e.target.classList.contains("campo-division")) {
            pintarProgramas(fila, null);
        }
        guardarFila(fila);
    });

    /**
     * Manda la fila si ya está completa: alta si es nueva, cambio si ya existe.
     *
     * @param {HTMLElement} fila fila que se acaba de tocar
     * @returns {void}
     */
    function guardarFila(fila) {
        var datos = datosDeFila(fila);
        if (!filaCompleta(datos)) {
            return;
        }
        if (fila.dataset.id) {
            if (fila.dataset.firma === firmaDe(datos)) {
                return; // se tocó un select pero quedó en el mismo valor
            }
            datos.id = Number(fila.dataset.id);
            enviar("actualizar", "grupo", datos, avisoGrupos);
            return;
        }
        // Es el borrador: si el alta pasa, la respuesta lo trae ya con su id y
        // el repintado lo sustituye por una fila de verdad
        borrador = null;
        enviar("crear", "grupo", datos, avisoGrupos).then(function (aplicado) {
            if (!aplicado) {
                borrador = fila; // se rechazó: se queda para corregirla encima
                btnAgregarGrupo.disabled = true;
            }
        });
    }

    programasContainer.addEventListener("click", function (e) {
        var boton = e.target.closest(".btn-delete-row");
        if (!boton) {
            return;
        }
        var fila = boton.closest(".programa-row");

        // El borrador no está en la base: se quita y ya, no hay nada que borrar
        if (!fila.dataset.id) {
            borrador = null;
            fila.remove();
            btnAgregarGrupo.disabled = false;
            sinGrupos.hidden = programasContainer.querySelector(".programa-row") !== null;
            avisar(avisoGrupos, "");
            marcarEstado(null);
            return;
        }

        var c = campos(fila);
        window.Modales.confirmar({
            titulo: "Quitar este grupo",
            mensaje: "Se quitará el " + c.cuatrimestre.value + "° " + c.grupo.value
                + " de " + c.programa.value + ".",
            detalle: "Sus " + c.estudiantes.value
                + " estudiantes dejarán de contar en el total de la visita.",
            tipo: "peligro",
            aceptar: "Sí, quitar"
        }, function () {
            enviar("eliminar", "grupo", { id: Number(fila.dataset.id) }, avisoGrupos);
        });
    });

    // ===================== Chips: asignaturas y acompañantes =====================

    var tplChip = document.getElementById("tpl-chip");

    /**
     * Arma un chip con su texto y sus dos acciones.
     *
     * @param {number} id llave del renglón en la base
     * @param {string} etiqueta texto visible
     * @returns {HTMLElement} el chip listo para insertarse
     */
    function crearChip(id, etiqueta) {
        var chip = tplChip.content.firstElementChild.cloneNode(true);
        chip.dataset.id = id;
        var texto = chip.querySelector(".chip-texto");
        texto.textContent = etiqueta;
        texto.title = "Clic para corregir";
        return chip;
    }

    /**
     * Vuelve a pintar los chips de una lista, dejando su campo de captura al final.
     *
     * @param {HTMLElement} contenedor el .tags-input-wrapper
     * @param {HTMLElement} entrada el .tags-input que vive dentro
     * @param {Array<Object>} items renglones del mensaje
     * @param {Function} etiquetaDe cómo se ve cada renglón
     * @returns {void}
     */
    function pintarChips(contenedor, entrada, items, etiquetaDe) {
        contenedor.querySelectorAll(".tag-chip").forEach(function (chip) {
            chip.remove();
        });
        items.forEach(function (item) {
            contenedor.insertBefore(crearChip(item.id, etiquetaDe(item)), entrada);
        });
    }

    /**
     * Hace que el texto de un chip responda al teclado como el botón que dice
     * ser: es un <span role="button">, así que Enter y Espacio no lo activan
     * solos como sí harían con un <button>.
     *
     * @param {HTMLElement} contenedor el .tags-input-wrapper
     * @returns {void}
     */
    function activarChipsConTeclado(contenedor) {
        contenedor.addEventListener("keydown", function (e) {
            if (e.key !== "Enter" && e.key !== " ") {
                return;
            }
            var texto = e.target.closest(".chip-texto");
            if (!texto) {
                return; // se está escribiendo en el campo, no sobre un chip
            }
            e.preventDefault();
            texto.click(); // lo atiende el mismo escucha delegado que el clic
        });
    }

    var wrapperAsignaturas = document.getElementById("tags-wrapper");
    var entradaAsignaturas = document.getElementById("tags-input");
    var wrapperDocentes = document.getElementById("acompanantes-wrapper");
    var entradaDocentes = document.getElementById("acompanantes-input");
    var ayudaDocentes = document.getElementById("acompanantes-ayuda");
    var AYUDA_DOCENTES = ayudaDocentes.textContent;

    activarChipsConTeclado(wrapperAsignaturas);
    activarChipsConTeclado(wrapperDocentes);

    // ===================== Asignaturas =====================

    entradaAsignaturas.addEventListener("keydown", function (e) {
        if (e.key !== "Enter") {
            return;
        }
        e.preventDefault(); // Enter dentro de un campo no debe enviar nada más
        var nombre = entradaAsignaturas.value.trim();
        if (!nombre) {
            return;
        }
        entradaAsignaturas.value = "";
        enviar("crear", "asignatura", { nombre: nombre }, avisoAsignaturas)
            .then(function (aplicado) {
                if (!aplicado) {
                    entradaAsignaturas.value = nombre; // se rechazó: se devuelve para corregir
                }
            });
    });

    wrapperAsignaturas.addEventListener("click", function (e) {
        var chip = e.target.closest(".tag-chip");
        if (!chip) {
            entradaAsignaturas.focus(); // clic en el hueco: se escribe ahí
            return;
        }
        if (e.target.closest(".tag-remove")) {
            // Sin confirmar: una asignatura se vuelve a escribir en dos segundos,
            // y el servlet no deja quedarse sin ninguna
            enviar("eliminar", "asignatura", { id: Number(chip.dataset.id) }, avisoAsignaturas);
            return;
        }
        if (e.target.closest(".chip-texto")) {
            corregirAsignatura(chip);
        }
    });

    /**
     * Convierte el chip en su propio campo de captura para corregir el nombre.
     *
     * @param {HTMLElement} chip chip de la asignatura
     * @returns {void}
     */
    function corregirAsignatura(chip) {
        var boton = chip.querySelector(".chip-texto");
        if (!boton) {
            return; // ya se está corrigiendo
        }
        var original = boton.textContent;

        var campo = document.createElement("input");
        campo.type = "text";
        campo.className = "chip-input";
        campo.maxLength = 100;
        campo.value = original;
        campo.size = Math.max(original.length, 6);

        chip.replaceChild(campo, boton);
        campo.focus();
        campo.select();

        // Idempotente a propósito: Escape cancela y además provoca el blur, que
        // vuelve a llamar aquí; sin la guarda, el segundo replaceChild truena
        // porque el campo ya no es hijo del chip
        function cancelar() {
            if (campo.parentNode === chip) {
                chip.replaceChild(boton, campo);
            }
        }

        campo.addEventListener("keydown", function (e) {
            if (e.key === "Escape") {
                cancelar();
                return;
            }
            if (e.key !== "Enter") {
                return;
            }
            e.preventDefault();
            var nombre = campo.value.trim();
            if (!nombre || nombre === original) {
                cancelar();
                return;
            }
            enviar("actualizar", "asignatura",
                { id: Number(chip.dataset.id), nombre: nombre }, avisoAsignaturas)
                .then(function (aplicado) {
                    if (!aplicado) {
                        cancelar(); // el repintado no ocurrió: se deja como estaba
                    }
                });
        });

        // Salirse sin aceptar es cancelar: si se quisiera guardar, se presiona Enter
        campo.addEventListener("blur", cancelar);
    }

    // ===================== Docentes acompañantes =====================

    // Cuando se está sustituyendo a alguien, aquí vive su id: el mismo campo de
    // búsqueda sirve para agregar y para cambiar, y esto decide cuál de las dos
    var cambiandoDocente = null;

    /**
     * Entra o sale del modo "cambiar acompañante".
     *
     * @param {?number} id docente al que se va a sustituir, o null para salir
     * @param {string} [nombre] su nombre, para decirlo en el campo
     * @returns {void}
     */
    function modoCambio(id, nombre) {
        cambiandoDocente = id;
        entradaDocentes.value = "";
        cerrarSugerencias();
        if (id) {
            entradaDocentes.placeholder = "Elige quién reemplaza a " + nombre;
            ayudaDocentes.textContent = "Elige al docente que va a sustituirlo, o presiona Escape para dejarlo como está.";
            entradaDocentes.focus();
        } else {
            entradaDocentes.placeholder = "Escribe el nombre del docente y elígelo de la lista";
            ayudaDocentes.textContent = AYUDA_DOCENTES;
        }
    }

    wrapperDocentes.addEventListener("click", function (e) {
        var chip = e.target.closest(".tag-chip");
        if (!chip) {
            entradaDocentes.focus();
            return;
        }
        if (e.target.closest(".tag-remove")) {
            enviar("eliminar", "docente", { idUsuario: Number(chip.dataset.id) }, avisoDocentes);
            return;
        }
        if (e.target.closest(".chip-texto")) {
            modoCambio(Number(chip.dataset.id), chip.querySelector(".chip-texto").textContent);
        }
    });

    entradaDocentes.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && cambiandoDocente) {
            modoCambio(null);
        }
    });

    /**
     * Aplica el docente elegido en el autocompletado: lo agrega, o sustituye al
     * que se estaba cambiando.
     *
     * @param {Object} docente el que se eligió de las sugerencias
     * @returns {void}
     */
    function elegirDocente(docente) {
        var anterior = cambiandoDocente;
        modoCambio(null);

        if (anterior) {
            enviar("actualizar", "docente",
                { id: anterior, idUsuario: docente.id }, avisoDocentes);
            return;
        }
        enviar("crear", "docente", { idUsuario: docente.id }, avisoDocentes);
    }

    // ===================== Autocompletado de docentes =====================

    var sugerencias = document.getElementById("acompanantes-sugerencias");
    var temporizador = null;

    function cerrarSugerencias() {
        sugerencias.innerHTML = "";
        sugerencias.classList.remove("visible");
    }

    /**
     * Pinta las coincidencias que devolvió /docentes.
     *
     * @param {Array<Object>} docentes lista de {id, nombre, correo}
     * @returns {void}
     */
    function pintarSugerencias(docentes) {
        sugerencias.innerHTML = "";
        if (docentes.length === 0) {
            cerrarSugerencias();
            return;
        }
        docentes.forEach(function (docente) {
            var item = document.createElement("div");
            item.className = "autocomplete-item";

            var nombre = document.createElement("span");
            nombre.className = "autocomplete-nombre";
            nombre.textContent = docente.nombre;
            item.appendChild(nombre);

            var correo = document.createElement("small");
            correo.textContent = docente.correo;
            item.appendChild(correo);

            // mousedown y no click: al hacer clic, el blur del campo cierra la
            // lista antes de que el click alcance a dispararse
            item.addEventListener("mousedown", function (e) {
                e.preventDefault();
                elegirDocente(docente);
            });
            sugerencias.appendChild(item);
        });
        sugerencias.classList.add("visible");
    }

    entradaDocentes.addEventListener("input", function () {
        window.clearTimeout(temporizador);
        var texto = entradaDocentes.value.trim();
        if (texto.length < 2) {
            cerrarSugerencias();
            return;
        }
        // Se espera a que deje de teclear: una petición por letra satura al
        // servidor y las respuestas llegan desordenadas
        temporizador = window.setTimeout(function () {
            fetch(URL_DOCENTES + "?q=" + encodeURIComponent(texto))
                .then(function (respuesta) {
                    return respuesta.json();
                })
                .then(pintarSugerencias)
                .catch(cerrarSugerencias);
        }, 250);
    });

    entradaDocentes.addEventListener("blur", function () {
        // Con retraso: si el blur viene de elegir una sugerencia, hay que dejar
        // que ese mousedown termine antes de vaciar la caja
        window.setTimeout(cerrarSugerencias, 150);
    });

    // ===================== Repintado completo =====================

    /**
     * Vuelve a pintar las tres listas y el resumen con lo que respondió el
     * servlet.
     *
     * @param {Object} desglose bloque "desglose" del mensaje
     * @returns {void}
     */
    function pintarDesglose(desglose) {
        pintarChips(wrapperDocentes, entradaDocentes, desglose.docentes, function (docente) {
            return docente.nombre;
        });
        pintarGrupos(desglose.grupos);
        pintarResumen(desglose.divisiones, desglose.totalEstudiantes);
        pintarChips(wrapperAsignaturas, entradaAsignaturas, desglose.asignaturas, function (asignatura) {
            return asignatura.nombre;
        });
    }

    // ===================== Primera carga =====================

    // La página llegó sin datos: las tres listas se piden por el mismo camino
    // que usan las demás operaciones
    enviar("listar", null, null, null);
});
