// Búsqueda y filtros del histórico (RF-09).
//
// El JSP ya pintó todas las solicitudes terminadas; aquí solo se esconden las
// filas que no coinciden. El filtrado es en pantalla, sin volver al servidor:
// el histórico son decenas de filas, no miles.
document.addEventListener("DOMContentLoaded", function () {
    var tbody = document.getElementById("filas-historico");
    if (!tbody) {
        return; // Histórico vacío: el JSP ni siquiera pintó la barra de filtros
    }

    var filas = Array.prototype.slice.call(tbody.rows);

    var inputTexto = document.getElementById("filtro-texto");
    var selEstado = document.getElementById("filtro-estado");
    var selDocente = document.getElementById("filtro-docente"); // solo Estadías/Admin
    var inputDesde = document.getElementById("filtro-desde");
    var inputHasta = document.getElementById("filtro-hasta");
    var btnLimpiar = document.getElementById("filtro-limpiar");

    var conteo = document.getElementById("filtro-conteo");
    var cardTabla = document.getElementById("card-historico");
    var sinResultados = document.getElementById("sin-resultados");
    var paginador = document.getElementById("paginacion");

    // Cuántas filas se ven a la vez: pintar el histórico completo de golpe es
    // lo que se siente lento, no el filtrado
    var POR_PAGINA = 10;
    var pagina = 1;
    // Botón al que hay que devolverle el foco después de repintar el paginador
    var focoPendiente = null;

    // "Reducción" y "reduccion" tienen que encontrarse igual: se comparan las
    // dos partes en minúsculas y sin acentos (NFD separa la letra del acento)
    function normalizar(texto) {
        return (texto || "").toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    }

    // El texto en el que se busca no cambia: se normaliza una sola vez al
    // entrar y no en cada tecla
    filas.forEach(function (fila) {
        fila.dataset.busqueda = normalizar(fila.dataset.busqueda);
    });

    // ===================== Opciones de los selects =====================

    // Valores distintos de un data-* entre las filas, en orden alfabético
    function valoresUnicos(clave) {
        var valores = [];
        filas.forEach(function (fila) {
            var valor = fila.dataset[clave];
            if (valor && valores.indexOf(valor) === -1) {
                valores.push(valor);
            }
        });
        return valores.sort(function (a, b) { return a.localeCompare(b, "es"); });
    }

    // Un select con una sola opción no filtra nada (todas las solicitudes la
    // cumplen), así que en ese caso se esconde el campo completo.
    function llenarSelect(select, valores) {
        if (!select) {
            return;
        }
        valores.forEach(function (valor) {
            var opcion = document.createElement("option");
            opcion.value = valor;
            opcion.textContent = valor;
            select.appendChild(opcion);
        });
        if (valores.length < 2) {
            select.closest(".filtro-campo").hidden = true;
        }
    }

    llenarSelect(selEstado, valoresUnicos("estado"));
    llenarSelect(selDocente, valoresUnicos("docente"));

    // ===================== Filtrado =====================

    // Filas que pasan los filtros, en el orden en que las mandó el servidor
    // (de la visita más reciente hacia atrás). De aquí sale la página que se ve.
    var coincidencias = filas;

    function aplicarFiltros() {
        var texto = normalizar(inputTexto.value.trim());
        var estado = selEstado.value;
        var docente = selDocente ? selDocente.value : "";
        var desde = inputDesde.value;
        var hasta = inputHasta.value;

        coincidencias = filas.filter(function (fila) {
            var datos = fila.dataset;
            // Las fechas son yyyy-MM-dd, igual que las del <input type="date">:
            // en ese formato compararlas como texto equivale a compararlas
            // como fechas
            return (!texto || datos.busqueda.indexOf(texto) !== -1)
                && (!estado || datos.estado === estado)
                && (!docente || datos.docente === docente)
                && (!desde || (datos.fecha && datos.fecha >= desde))
                && (!hasta || (datos.fecha && datos.fecha <= hasta));
        });

        // Cambiar un filtro cambia el conjunto entero: se vuelve a la página 1
        pagina = 1;
        pintarPagina();
    }

    // ===================== Paginación =====================

    function pintarPagina() {
        var totalPaginas = Math.max(1, Math.ceil(coincidencias.length / POR_PAGINA));
        if (pagina > totalPaginas) {
            pagina = totalPaginas;
        }
        var desde = (pagina - 1) * POR_PAGINA;
        var hasta = Math.min(desde + POR_PAGINA, coincidencias.length);

        filas.forEach(function (fila) { fila.hidden = true; });
        coincidencias.slice(desde, hasta).forEach(function (fila) { fila.hidden = false; });

        cardTabla.hidden = (coincidencias.length === 0);
        sinResultados.hidden = (coincidencias.length > 0);
        conteo.textContent = textoConteo(desde, hasta);
        pintarPaginador(totalPaginas);
    }

    function textoConteo(desde, hasta) {
        if (coincidencias.length === 0) {
            return "0 de " + filas.length + " solicitudes";
        }
        var texto = "Mostrando " + (desde + 1) + "–" + hasta
                + " de " + coincidencias.length
                + (coincidencias.length === 1 ? " solicitud" : " solicitudes");
        // Con filtros puestos se enseñan los dos totales: las que coinciden y
        // las que hay en el histórico completo
        if (coincidencias.length !== filas.length) {
            texto += " (de " + filas.length + " en el histórico)";
        }
        return texto;
    }

    function irAPagina(n, foco) {
        pagina = n;
        focoPendiente = foco;
        pintarPagina();
        // Se cambia de página desde el final de la tabla: sin esto las filas
        // nuevas quedan arriba, fuera de la pantalla
        cardTabla.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    function botonPagina(etiqueta, destino, deshabilitado, esActual, foco) {
        var boton = document.createElement("button");
        boton.type = "button";
        boton.className = "pagina-btn";
        boton.textContent = etiqueta;
        boton.disabled = deshabilitado;
        if (esActual) {
            boton.setAttribute("aria-current", "page");
        } else {
            boton.setAttribute("aria-label", "Ir a la página " + destino);
        }
        boton.dataset.foco = foco;
        boton.addEventListener("click", function () { irAPagina(destino, foco); });
        return boton;
    }

    // Con muchas páginas listarlas todas llenaría el renglón: se ven la actual
    // con su vecina de cada lado, más la primera y la última
    function numerosVisibles(totalPaginas) {
        var numeros = [];
        for (var i = 1; i <= totalPaginas; i++) {
            if (i === 1 || i === totalPaginas || Math.abs(i - pagina) <= 1) {
                numeros.push(i);
            }
        }
        return numeros;
    }

    function pintarPaginador(totalPaginas) {
        paginador.innerHTML = "";
        // Una sola página no se pagina: el paginador solo estorbaría
        paginador.hidden = (totalPaginas <= 1);
        if (paginador.hidden) {
            focoPendiente = null;
            return;
        }

        paginador.appendChild(botonPagina("‹ Anterior", pagina - 1, pagina === 1, false, "anterior"));

        var ultimoPintado = 0;
        numerosVisibles(totalPaginas).forEach(function (n) {
            if (n - ultimoPintado > 1) {
                var puntos = document.createElement("span");
                puntos.className = "paginacion-puntos";
                puntos.textContent = "…";
                paginador.appendChild(puntos);
            }
            paginador.appendChild(botonPagina(String(n), n, false, n === pagina, "n" + n));
            ultimoPintado = n;
        });

        paginador.appendChild(botonPagina("Siguiente ›", pagina + 1, pagina === totalPaginas, false, "siguiente"));

        // El botón que se pulsó desapareció al repintar y el foco se habría ido
        // al <body>: se le devuelve al equivalente de la página nueva
        if (focoPendiente) {
            var destino = paginador.querySelector('[data-foco="' + focoPendiente + '"]:not(:disabled)')
                    || paginador.querySelector('[aria-current="page"]');
            if (destino) {
                destino.focus();
            }
            focoPendiente = null;
        }
    }

    // Un rango al revés no devuelve nada: con min/max el navegador avisa antes
    function ajustarRango() {
        inputHasta.min = inputDesde.value;
        inputDesde.max = inputHasta.value;
    }

    [inputTexto, selEstado, selDocente, inputDesde, inputHasta].forEach(function (campo) {
        if (campo) {
            campo.addEventListener("input", function () {
                ajustarRango();
                aplicarFiltros();
            });
        }
    });

    btnLimpiar.addEventListener("click", function () {
        inputTexto.value = "";
        selEstado.value = "";
        if (selDocente) {
            selDocente.value = "";
        }
        inputDesde.value = "";
        inputHasta.value = "";
        ajustarRango();
        aplicarFiltros();
        inputTexto.focus();
    });

    // Estado inicial: sin filtros, solo para que se vea el total desde el principio
    aplicarFiltros();
});
