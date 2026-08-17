// Panel de gestión de usuarios (RF-12).
//
// El JSP pinta la lista una sola vez, al entrar. De ahí en adelante los
// formularios se envían con fetch al servlet, que responde JSON
// {ok, mensaje, usuario, baja}, y con eso se actualiza la tabla sin recargar.
// La validación de verdad la hace el servlet.
document.addEventListener("DOMContentLoaded", function () {
    var ERROR_RED = "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.";

    var avisos = document.getElementById("avisos");
    var tbody = document.getElementById("lista-usuarios");
    var plantilla = document.getElementById("tpl-fila-usuario");
    var cardUsuarios = document.getElementById("card-usuarios");
    var sinUsuarios = document.getElementById("sin-usuarios");

    var formUsuario = document.getElementById("form-usuario");
    var formEditar = document.getElementById("form-editar");
    var formEliminar = document.getElementById("form-eliminar");
    var avisoEditar = document.getElementById("aviso-modal-editar");

    var modalEditar = new bootstrap.Modal(document.getElementById("modal-editar"));
    var modalEliminar = new bootstrap.Modal(document.getElementById("modal-eliminar"));

    // OJO: aquí NO se puede usar formUsuario.action. Los formularios exponen sus
    // campos como propiedades por nombre, y esos ganan sobre las propias del
    // formulario: como hay un <input name="action">, formUsuario.action devuelve
    // ese input y no la URL. Con getAttribute se lee el atributo de verdad.
    var URL_USUARIOS = formUsuario.getAttribute("action");

    // ===================== Avisos =====================

    // Mini card de aviso: verde si salió bien, roja si no
    function miniCard(mensaje, exito) {
        var caja = document.createElement("div");
        caja.className = "instruccion " + (exito ? "instruccion-exito" : "instruccion-rechazo");
        caja.style.marginTop = "1rem";

        var icono = document.createElement("i");
        icono.className = "bi " + (exito ? "bi-check-circle" : "bi-exclamation-triangle");

        var texto = document.createElement("div");
        texto.textContent = mensaje;

        caja.appendChild(icono);
        caja.appendChild(texto);
        return caja;
    }

    // Solo se ve el último resultado, para que no se acumulen avisos viejos
    function avisar(mensaje, exito) {
        avisos.innerHTML = "";
        if (mensaje) {
            avisos.appendChild(miniCard(mensaje, exito));
        }
    }

    // Los errores de la edición se quedan dentro del modal: cerrarlo perdería
    // lo que ya se había escrito
    function avisarEnModal(mensaje) {
        avisoEditar.innerHTML = "";
        avisoEditar.appendChild(miniCard(mensaje, false));
    }

    // ===================== La tabla =====================

    // La tabla y el estado vacío se turnan según queden filas o no
    function ajustarEstadoVacio() {
        var hayFilas = tbody.querySelector("tr") !== null;
        cardUsuarios.hidden = !hayFilas;
        sinUsuarios.hidden = hayFilas;
    }

    function filaDe(id) {
        return tbody.querySelector('tr[data-id="' + id + '"]');
    }

    // Escribe los datos del usuario en una fila, nueva o ya existente
    function pintarFila(fila, usuario) {
        fila.dataset.id = usuario.id;
        fila.querySelector(".usuario-nombre").textContent = usuario.nombre;
        fila.querySelector(".celda-correo").textContent = usuario.correo;

        var rol = fila.querySelector(".celda-rol .badge-estado");
        rol.textContent = usuario.rol;
        rol.className = "badge-estado rol-" + usuario.rol.toLowerCase();
    }

    function agregarFila(usuario) {
        var fila = plantilla.content.firstElementChild.cloneNode(true);
        pintarFila(fila, usuario);
        tbody.appendChild(fila);
        ajustarEstadoVacio();
    }

    // ===================== Peticiones al servlet =====================

    var recargando = false;

    // Si la sesión caducó, FiltroAutenticacion manda al login y lo que llega ya
    // no es JSON: solo en ese caso se recarga, los demás fallos se avisan
    function leerJson(respuesta) {
        if (respuesta.redirected) {
            recargando = true;
            window.location.reload();
        }
        return respuesta.json();
    }

    // Manda el formulario como si fuera un POST normal, pero sin salir de la
    // página. El servlet lee los mismos parámetros de siempre.
    function enviarFormulario(form) {
        return fetch(URL_USUARIOS, {
            method: "POST",
            body: new URLSearchParams(new FormData(form))
        }).then(leerJson);
    }

    // Lo que piden los modales antes de abrirse: action=editar trae los datos
    // del usuario y action=confirmar trae además el conteo de la baja
    function pedirDatos(action, id) {
        return fetch(URL_USUARIOS + "?action=" + action + "&id=" + id).then(leerJson);
    }

    // Cuando la petición ni siquiera llegó (sin internet, servidor caído)
    function avisarFallo(enModal) {
        if (recargando) {
            return; // la sesión caducó y la página ya se está recargando
        }
        if (enModal) {
            avisarEnModal(ERROR_RED);
        } else {
            avisar(ERROR_RED, false);
        }
    }

    // Mientras el servidor contesta, el botón se bloquea para no mandar dos veces
    function bloquear(form, bloqueado) {
        var boton = form.querySelector('button[type="submit"]');
        if (boton) {
            boton.disabled = bloqueado;
        }
    }

    // ===================== Crear =====================

    formUsuario.addEventListener("submit", function (e) {
        e.preventDefault();
        bloquear(formUsuario, true);

        enviarFormulario(formUsuario).then(function (r) {
            if (r.ok) {
                agregarFila(r.usuario);
                formUsuario.reset();
                // reset() devuelve los valores, pero no borra el "las contraseñas
                // no coinciden" que dejó validacion-cuenta.js
                formUsuario.querySelectorAll("[data-igual-a]").forEach(function (campo) {
                    campo.setCustomValidity("");
                });
                document.getElementById("nombre").focus();
            }
            avisar(r.mensaje, r.ok);
        }).catch(function () {
            avisarFallo(false);
        }).finally(function () {
            bloquear(formUsuario, false);
        });
    });

    // ===================== Editar =====================

    function abrirEditar(usuario) {
        avisoEditar.innerHTML = "";
        formEditar.querySelector('input[name="id"]').value = usuario.id;
        document.getElementById("editar-nombre").value = usuario.nombre;
        document.getElementById("editar-correo").value = usuario.correo;
        document.getElementById("editar-rol").value = usuario.rol;
        modalEditar.show();
    }

    formEditar.addEventListener("submit", function (e) {
        e.preventDefault();
        bloquear(formEditar, true);

        var id = formEditar.querySelector('input[name="id"]').value;

        enviarFormulario(formEditar).then(function (r) {
            // Un correo repetido no cierra el modal: se corrige encima
            if (!r.ok) {
                avisarEnModal(r.mensaje);
                return;
            }
            var fila = filaDe(id);
            if (fila) {
                pintarFila(fila, r.usuario);
            }
            modalEditar.hide();
            avisar(r.mensaje, true);
        }).catch(function () {
            avisarFallo(true);
        }).finally(function () {
            bloquear(formEditar, false);
        });
    });

    // ===================== Eliminar =====================

    // El borrado es irreversible, así que el modal enseña primero qué se pierde
    function abrirEliminar(usuario, baja) {
        formEliminar.querySelector('input[name="id"]').value = usuario.id;
        document.getElementById("baja-nombre").textContent = usuario.nombre;
        document.getElementById("baja-correo").textContent = usuario.correo;

        var solicitudes = baja.solicitudes;
        var reportes = baja.reportes;

        // Lo que se borra para siempre
        var lista = document.getElementById("baja-lista");
        lista.innerHTML = "";
        if (solicitudes > 0) {
            agregarPunto(lista, solicitudes + " solicitud" + (solicitudes === 1 ? "" : "es")
                + " de visita, con su desglose de grupos y sus documentos");
        }
        if (reportes > 0) {
            agregarPunto(lista, reportes + " reporte" + (reportes === 1 ? "" : "s")
                + " de visita, con sus evidencias fotográficas");
        }
        document.getElementById("baja-destruye").hidden = (solicitudes + reportes === 0);
        document.getElementById("baja-limpia").hidden = (solicitudes + reportes > 0);

        // El trabajo de otros docentes no se borra: al usuario solo se le desliga
        var desliga = document.getElementById("baja-desliga");
        var partes = [];
        if (baja.autorizaciones > 0) {
            partes.push("en " + baja.autorizaciones + " que evaluó solo se quitará su firma como quien autorizó");
        }
        if (baja.acompanamientos > 0) {
            partes.push("en " + baja.acompanamientos + " solo se le quitará de la lista de docentes acompañantes");
        }
        desliga.textContent = "No se borrarán las solicitudes de otros docentes: " + partes.join(", y ") + ".";
        desliga.hidden = (partes.length === 0);

        modalEliminar.show();
    }

    function agregarPunto(lista, texto) {
        var li = document.createElement("li");
        li.textContent = texto;
        lista.appendChild(li);
    }

    formEliminar.addEventListener("submit", function (e) {
        e.preventDefault();
        bloquear(formEliminar, true);

        var id = formEliminar.querySelector('input[name="id"]').value;

        enviarFormulario(formEliminar).then(function (r) {
            if (r.ok) {
                var fila = filaDe(id);
                if (fila) {
                    fila.remove();
                    ajustarEstadoVacio();
                }
            }
            modalEliminar.hide();
            avisar(r.mensaje, r.ok);
        }).catch(function () {
            modalEliminar.hide();
            avisarFallo(false);
        }).finally(function () {
            bloquear(formEliminar, false);
        });
    });

    // ===================== Botones de cada fila =====================

    // El escucha va en el tbody y no en cada botón: así también funciona en las
    // filas que se agregan después de dar de alta un usuario
    tbody.addEventListener("click", function (e) {
        var boton = e.target.closest("button[data-accion]");
        if (!boton) {
            return;
        }
        var accion = boton.dataset.accion;
        var id = boton.closest("tr").dataset.id;

        pedirDatos(accion === "editar" ? "editar" : "confirmar", id).then(function (r) {
            if (!r.ok) {
                avisar(r.mensaje, false);
                return;
            }
            if (accion === "editar") {
                abrirEditar(r.usuario);
            } else {
                abrirEliminar(r.usuario, r.baja);
            }
        }).catch(function () {
            avisarFallo(false);
        });
    });
});
