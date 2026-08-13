/**
 * Perfiles de datos precargados para el formulario de Nueva Solicitud.
 *
 * Para agregar el tuyo: copia un bloque y cámbialo. Las reglas que valida el
 * servidor (SolicitudServlet.validar) son las mismas que se respetan aquí:
 *
 *   nombreEmpresa       obligatorio, máx. 150
 *   direccionLugar      obligatorio, máx. 200
 *   telefonoContacto    exactamente 10 dígitos
 *   correoContacto      correo válido, máx. 100
 *   objetivoVisita      obligatorio, máx. 500
 *   areaSolicitante     obligatoria, máx. 100
 *   celularResponsable  exactamente 10 dígitos
 *   cuatrimestre        entre 1 y 11
 *   estudiantes         entre 1 y 999 por grupo
 *   fecha de inicio     nunca anterior a hoy (por eso se usa diasDesdeHoy)
 *
 * El nombre del programa educativo tiene que ser IDÉNTICO al del catálogo
 * (CatalogoAcademico.java), acentos incluidos, o el <select> no lo encuentra.
 *
 * acompanantes: no son nombres fijos, es el texto que se escribe en el buscador
 * de docentes. El sistema busca por nombre o por correo, así que "@utez.edu.mx"
 * sirve para tomar al primer docente registrado, sea cual sea. Si la búsqueda no
 * devuelve nada, el perfil se llena igual y el panel lo avisa.
 */
var SVA_PRESETS = [
    {
        id: "continental",
        nombre: "Continental · visita industrial",
        descripcion: "DATID · 3 grupos · 45 estudiantes",
        diasDesdeHoy: 35,
        campos: {
            nombreEmpresa: "Continental Automotive Systems México, S. de R.L. de C.V.",
            direccionLugar: "Carretera Federal México-Cuautla Km 4.5, Parque Industrial Cuautla, 62747 Cuautla, Morelos",
            telefonoContacto: "7351234567",
            correoContacto: "vinculacion@continental-cuautla.com",
            objetivoVisita: "Conocer la infraestructura de red industrial y los sistemas de control de la planta, "
                + "para que los estudiantes relacionen los temas de redes, seguridad de la información y "
                + "automatización vistos en clase con su aplicación real en un entorno de manufactura. "
                + "Incluye recorrido por el área de producción, sala de telecomunicaciones y una sesión de "
                + "preguntas con el personal de TI.",
            areaSolicitante: "DATID",
            docenteResponsable: "Mtra. Claudia Ramírez Salgado",
            celularResponsable: "7771234567"
        },
        grupos: [
            {
                division: "DATID",
                programa: "Ingeniería en Tecnologías de la Información",
                cuatrimestre: 7,
                grupo: "A",
                estudiantes: 28
            },
            {
                division: "DATEFI",
                programa: "Licenciatura en Terapia Física",
                cuatrimestre: 5,
                grupo: "B",
                estudiantes: 12
            },
            {
                division: "DAMI",
                programa: "Ingeniería en Mecatrónica",
                cuatrimestre: 9,
                grupo: "A",
                estudiantes: 5
            }
        ],
        asignaturas: [
            "Redes de Área Local",
            "Seguridad de la Información",
            "Gestión de Proyectos de TI"
        ],
        acompanantes: ["@utez.edu.mx", "@utez.edu.mx"]
    },
    {
        id: "bepensa",
        nombre: "Bepensa Bebidas · planta embotelladora",
        descripcion: "DACEA · 2 grupos · 52 estudiantes",
        diasDesdeHoy: 21,
        campos: {
            nombreEmpresa: "Bepensa Bebidas, S.A. de C.V. — Planta Cuernavaca",
            direccionLugar: "Av. Emiliano Zapata 1050, Col. Tlaltenango, 62170 Cuernavaca, Morelos",
            telefonoContacto: "7773456789",
            correoContacto: "visitas.planta@bepensa.com",
            objetivoVisita: "Observar el proceso completo de producción y distribución de bebidas para relacionar "
                + "los temas de cadena de suministro, control de inventarios y estrategia comercial con la "
                + "operación real de la planta. Los estudiantes documentarán el recorrido y presentarán un "
                + "análisis del proceso logístico como evidencia de la asignatura.",
            areaSolicitante: "DACEA",
            docenteResponsable: "Lic. Fernando Estrada Peralta",
            celularResponsable: "7778901234"
        },
        grupos: [
            {
                division: "DACEA",
                programa: "Licenciatura en Innovación de Negocios y Mercadotecnia",
                cuatrimestre: 6,
                grupo: "A",
                estudiantes: 34
            },
            {
                division: "DACEA",
                programa: "TSU en Desarrollo de Negocios área Mercadotecnia",
                cuatrimestre: 3,
                grupo: "B",
                estudiantes: 18
            }
        ],
        asignaturas: [
            "Cadena de Suministro",
            "Mercadotecnia Estratégica",
            "Comportamiento del Consumidor"
        ],
        acompanantes: ["@utez.edu.mx"]
    },
    {
        id: "minima",
        nombre: "Mínima · un solo grupo",
        descripcion: "DATEFI · 1 grupo · 22 estudiantes",
        diasDesdeHoy: 10,
        campos: {
            nombreEmpresa: "Hospital General de Cuernavaca Dr. José G. Parres",
            direccionLugar: "Av. Domingo Diez S/N, Col. Lomas de la Selva, 62270 Cuernavaca, Morelos",
            telefonoContacto: "7773112233",
            correoContacto: "ensenanza@hospitalparres.gob.mx",
            objetivoVisita: "Recorrer el área de rehabilitación física del hospital para que los estudiantes "
                + "observen la aplicación clínica de las técnicas de terapia revisadas en clase y conozcan el "
                + "equipamiento con el que trabajarán durante su estadía.",
            areaSolicitante: "DATEFI",
            docenteResponsable: "Mtro. Aarón Villalobos Cruz",
            celularResponsable: "7775566778"
        },
        grupos: [
            {
                division: "DATEFI",
                programa: "Licenciatura en Terapia Física",
                cuatrimestre: 8,
                grupo: "A",
                estudiantes: 22
            }
        ],
        asignaturas: ["Rehabilitación Neurológica"],
        acompanantes: []
    }
];
