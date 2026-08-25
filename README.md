# SGVA — Sistema de Gestión de Visitas Académicas

Repositorio: <https://github.com/LeonardoAntonio2026/SistemaDeVisitasAcademicas>

## Equipo

Universidad Tecnológica Emiliano Zapata del Estado de Morelos (UTEZ) —
Ingeniería en Desarrollo y Gestión de Software.

| Matrícula | Integrante | Usuario de GitHub |
|---|---|---|
| 20253DS121 | Leonardo Antonio Arroyo Rodríguez | LeonardoAntonio2026 |
| 20253DS114 | Alan Esteban Zariñana Arizmendi | 20253ds114-design |
| 20253DS113 | Eder Gabriel García Vásquez | EdercitoFlow |
| 20253DS116 | Hugo Alberto Ramírez Martínez | Hugordito07 |
| 20253DS110 | Juan Manuel Calvo Vélez | JuanManuelVelez |

## Descripción del proyecto

Aplicación web para tramitar las visitas académicas de la UTEZ de principio a
fin, sustituyendo el trámite en papel y por correo.

El docente captura la solicitud en el sistema; la aplicación genera el formato
oficial (FO) en PDF con los datos capturados, el docente lo descarga, lo firma y
lo sube. El área de Estadías revisa la solicitud y la aprueba o la rechaza. Si
la rechaza, el docente corrige y la vuelve a enviar. Una vez realizada la
visita, el docente entrega su reporte —con evidencias fotográficas— y Estadías
lo evalúa.

Todo el expediente (solicitud, formato firmado, carta responsiva, reporte y
evidencias) queda guardado en la base de datos y consultable desde el histórico.

## Stack

| Pieza | Qué se usa |
|---|---|
| Lenguaje | Java 21 |
| Web | Jakarta Servlets 6.0 + JSP + JSTL 3.0 (sin framework) |
| Base de datos | Oracle Autonomous Database (nube), driver `ojdbc11` |
| Pool de conexiones | HikariCP |
| Empaquetado | Maven (`mvnw` incluido) → WAR |
| Servidor | Apache Tomcat 10.1+ |
| Front | Bootstrap 5 + Bootstrap Icons (servidos localmente, sin CDN) |

## Estructura del código

```
SistemaDeVisitasAcademicas/
├── src/main/java/com/example/demo/    Código Java
├── src/main/resources/                Wallet, credenciales y scripts SQL
├── src/main/webapp/                   Vistas JSP y archivos estáticos
├── extension-autollenado/             Extensión de Chrome (herramienta de apoyo)
├── docs/                              Documentación entregable
├── deploy.ps1                         Script de despliegue por SSH
└── pom.xml                            Dependencias y configuración de Maven
```

### Código Java — `src/main/java/com/example/demo/`

Está organizado en MVC de tres capas: **el servlet decide y valida, el DAO habla
con la base de datos y el JSP solo pinta**. Un JSP nunca abre una conexión ni
llama a un DAO directamente.

```
controller/          Servlets: uno por pantalla o acción
│                    (SolicitudServlet, ReporteServlet, LoginServlet,
│                     UsuarioServlet, DocumentoServlet...)
└── filters/         FiltroAutenticacion: exige sesión iniciada en todas
                     las rutas salvo login y recuperación de contraseña

model/               Entidades del dominio: Solicitud, Reporte, Usuario,
│                    Documento, ImagenReporte, ProgramaEducativo...
└── dao/             Acceso a datos con JDBC a mano, un DAO por entidad.
                     Todas las consultas usan PreparedStatement.

utils/               Clases de apoyo:
                     SQLConnector  — pool de conexiones HikariCP
                     SesionUtils   — lee el usuario y el rol de la sesión
                     Validador     — validaciones de servidor reutilizables
                     PasswordUtils — hash de contraseñas
                     EmailSender   — envío de correos por SMTP
                     EnlaceContrasena / FechaTexto — apoyos menores
```

Cada petición sigue siempre el mismo recorrido:

```
Navegador → FiltroAutenticacion → Servlet → DAO → Oracle
                                     ↓
                                  JSP (vista)
```

Después de un POST que modifica datos el servlet **redirige** al GET
correspondiente (patrón PRG), para que recargar la página no repita la
operación. Los errores viajan como parámetro en la URL del redirect
(`?error=sinfirmado`) y el JSP los traduce a un mensaje.

### Vistas — `src/main/webapp/`

```
*.jsp                Una vista por pantalla: solicitudes.jsp, detalle.jsp,
                     reportes.jsp, historico.jsp, login.jsp,
                     UserManagement.jsp, visor-documento.jsp...
layout/              Fragmentos incluidos por las vistas:
                     header.jsp (define las banderas de rol), sidebar.jsp,
                     footer.jsp, lista-solicitudes.jsp, stepper.jsp
css/ js/ img/ fonts/  Estáticos. Bootstrap y sus iconos van servidos desde
                      aquí, no desde un CDN.
error/               Páginas de error (404, 500...)
WEB-INF/web.xml      Mapeo de páginas de error y timeout de sesión
```

El JavaScript de `js/` valida formularios para mejorar la experiencia, pero la
validación que protege los datos es la de Java: un POST directo se salta el JS.

### Recursos — `src/main/resources/`

```
wallet/                  Wallet de la Autonomous Database (no se sube a Git)
credentials.properties   Credenciales de la base y del correo (no se sube a Git)
db/schema.sql            Tablas
db/seed.sql              Catálogos: roles, estados, tipos de documento
```

### Extensión de autollenado — `extension-autollenado/`

Extensión de Chrome que llena el formulario de solicitud con datos de prueba
para no capturarlo a mano en cada demostración. Es una herramienta de apoyo al
desarrollo, no forma parte del WAR.

## Requisitos

- **JDK 21** (el `pom.xml` fija `source`/`target` en 21).
- **Tomcat 10.1 o superior.** No sirve Tomcat 9: la app usa el espacio de
  nombres `jakarta.*`, no `javax.*`.
- Acceso a la base Oracle Autonomous y su *wallet*.

No hace falta instalar Maven: se usa el wrapper (`mvnw.cmd` en Windows, `mvnw`
en Linux/macOS).

## Configuración

### 1. Wallet de Oracle

Descomprime la wallet de la Autonomous Database en `src/main/resources/wallet/`.
`SQLConnector` la localiza sola desde el classpath y arma la URL JDBC con ella.

### 2. Credenciales

Se leen **primero de variables de entorno** y, si falta alguna, del archivo
`src/main/resources/credentials.properties`:

| Variable de entorno | Clave en el .properties | Para qué |
|---|---|---|
| `DB_USER` | `db.user` | Usuario de la base |
| `DB_PASS` | `db.pass` | Contraseña de la base |
| `DB_NAME` | `db.name` | Alias TNS (ej. `DBGESTIONVISITAS_high`) |
| — | `smtp.user` | Cuenta Gmail que envía los correos |
| — | `smtp.pass` | *App password* de esa cuenta, no la contraseña normal |

Ese archivo tiene contraseñas reales y **no se sube a Git**: toda la carpeta
`src/main/resources/` está en el `.gitignore`. En el servidor se usan las
variables de entorno.

### 3. Base de datos

Con la base creada, ejecuta **en este orden**:

```
src/main/resources/db/schema.sql   -- tablas
src/main/resources/db/seed.sql     -- catálogos
```

El `seed.sql` no es opcional: sin esos catálogos fallan por *foreign key* el
alta de usuarios y la creación de solicitudes.

## Compilar

```bash
./mvnw clean package
```

El WAR queda en `target/SGVA-1.0-SNAPSHOT.war`. Para probarlo en local, cópialo
a `webapps/` de tu Tomcat, o renómbralo a `ROOT.war` si lo quieres en la raíz.

## Desplegar

`deploy.ps1` compila y sube el WAR al servidor por SSH:

```powershell
.\deploy.ps1              # compila y despliega
.\deploy.ps1 -SkipBuild   # despliega el WAR que ya está en target\
```

Sube primero a `/tmp` y luego hace `mv` (operación atómica) para que Tomcat
nunca vea un WAR a medio escribir. El servidor, el usuario y la ruta de la llave
SSH son parámetros del script.

## Roles y flujo

Hay tres roles (tabla `ROL`): **Docente**, **Estadías** y **Administrador**.
Estadías y Administrador revisan lo mismo; el Administrador además entra a la
gestión de usuarios y puede levantar sus propias solicitudes como un docente.

Por eso el permiso sobre una solicitud concreta no se decide por el rol sino por
si es suya: con las propias hace lo del docente (firmar, subir, enviar,
corregir) y con las ajenas lo del revisor (aprobar o rechazar). El Administrador
también corrige las solicitudes y los reportes de los demás mientras el estado
lo permita (solicitud Pendiente o Rechazada, reporte Pendiente o Rechazado);
firmar, subir el firmado, enviar y borrar siguen siendo del dueño.

```
Docente crea solicitud            → Pendiente
  descarga el FO, lo firma y lo sube
  da ENVIAR                       → En revisión
Estadías aprueba o rechaza        → Aprobada / Rechazada
  si la rechaza, el docente la corrige → Pendiente
Docente sube la carta responsiva  → Completada  (se abre su reporte)
Docente llena y firma el reporte  → Completado
Estadías evalúa el reporte        → Aprobado / Rechazado
  si lo rechaza, el docente lo corrige  → Pendiente
```

Al guardar una corrección la solicitud regresa a **Pendiente**, se borra el FO
firmado que hubiera —el formato se regenera con los datos nuevos— y se limpia la
decisión anterior.

Cada rol ve cosas distintas en las listas:

- **Docente**: las rechazadas le siguen apareciendo en su bandeja, porque le
  toca corregirlas. En su histórico solo van las completadas.
- **Estadías / Administrador**: en la bandeja solo lo que les toca atender, así
  que lo rechazado queda en el histórico y en el detalle de la solicitud.
- **Administrador**: en su bandeja salen además las solicitudes que él mismo
  levantó, incluso pendientes, para poder firmarlas y enviarlas.

## Limitaciones conocidas

- Las contraseñas se guardan con SHA-256 sin sal (`PasswordUtils`).
- Los PDF se guardan en la base como Base64, no en disco; de ahí el límite de
  10 MB por archivo.
- Los DAO manejan los errores con `e.printStackTrace()` en lugar de un logger.
