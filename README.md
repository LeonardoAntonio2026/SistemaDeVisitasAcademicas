# SGVA — Sistema de Gestión de Visitas Académicas

Aplicación web de la UTEZ para tramitar visitas académicas de principio a fin:
el docente captura la solicitud, el área de Estadías la evalúa, y al terminar la
visita el docente entrega su reporte para que Estadías lo apruebe.

## Stack

| Pieza | Qué se usa |
|---|---|
| Lenguaje | Java 21 |
| Web | Jakarta Servlets 6.0 + JSP + JSTL 3.0 (sin framework) |
| Base de datos | Oracle Autonomous Database (nube), driver `ojdbc11` |
| Pool | HikariCP |
| Empaquetado | Maven (`mvnw` incluido) → WAR |
| Servidor | Apache Tomcat 10.1+ |
| Front | Bootstrap 5 + Bootstrap Icons (servidos localmente, sin CDN) |

## Requisitos

- **JDK 21** (el `pom.xml` fija `source`/`target` en 21).
- **Tomcat 10.1 o superior.** No sirve Tomcat 9: la app usa el espacio de
  nombres `jakarta.*`, no `javax.*`.
- Acceso a la base Oracle Autonomous y su *wallet*.

No hace falta instalar Maven: usa el wrapper (`mvnw.cmd` en Windows, `mvnw` en
Linux/macOS).

## Configuración

### 1. Wallet de Oracle

Descomprime la wallet de la Autonomous Database en:

```
src/main/resources/wallet/
```

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
| — | `smtp.pass` | *App password* de esa cuenta (no la contraseña normal) |

> **Este archivo tiene contraseñas reales y no se sube a Git** — toda la carpeta
> `src/main/resources/` está en el `.gitignore`. Cada quien mantiene su copia
> local. En el servidor, lo correcto es usar las variables de entorno.

### 3. Base de datos

Con la base creada, ejecuta **en este orden**:

```
src/main/resources/db/schema.sql   -- tablas
src/main/resources/db/seed.sql     -- catálogos (roles, estados, tipos de documento)
```

El `seed.sql` no es opcional: sin esos catálogos fallan por *foreign key* el
alta de usuarios y la creación de solicitudes.

## Compilar

```bash
./mvnw clean package
```

El WAR queda en `target/SGVA-1.0-SNAPSHOT.war`. Para probar en local, cópialo a
`webapps/` de tu Tomcat, o renómbralo a `ROOT.war` si lo quieres en la raíz.

## Desplegar

`deploy.ps1` compila y sube el WAR al servidor por SSH:

```powershell
.\deploy.ps1              # compila y despliega
.\deploy.ps1 -SkipBuild   # despliega el WAR que ya está en target\
```

Sube primero a `/tmp` y luego hace `mv` (operación atómica) para que Tomcat
nunca vea un WAR a medio escribir. El servidor, el usuario y la ruta de la
llave SSH son parámetros del script, por si cambian.

## Estructura

```
src/main/java/com/example/demo/
├── controller/          Servlets (una clase por pantalla o acción)
│   └── filters/         FiltroAutenticacion: exige sesión en todas las rutas
├── model/               Entidades (Solicitud, Reporte, Usuario...)
│   └── dao/             Acceso a datos, un DAO por entidad (JDBC a mano)
└── utils/               SQLConnector (pool), SesionUtils, Validador,
                         PasswordUtils, EmailSender, FechaTexto

src/main/webapp/
├── *.jsp                Vistas
├── layout/              Trozos reutilizables (header, sidebar, stepper...)
├── css/  js/  img/  fonts/
└── WEB-INF/web.xml      Páginas de error y timeout de sesión
```

**Regla de oro del MVC de aquí:** el servlet decide y valida, el DAO habla con
la base, el JSP solo pinta. Un JSP nunca abre una conexión.

## Roles y flujo

Hay tres roles (tabla `ROL`): **Docente**, **Estadias** y **Administrador**.
Estadías y Administrador ven lo mismo en el proceso de visitas; el
Administrador además entra a la gestión de usuarios.

```
Docente crea solicitud            → Pendiente
  descarga el FO, lo firma y lo sube
  da ENVIAR                       → En revisión
Estadías aprueba o rechaza        → Aprobada / Rechazada
  si la rechaza, el docente la corrige → Pendiente (vuelve a empezar el envío)
Docente sube la carta responsiva  → Completada  (se abre su reporte)
Docente llena y firma el reporte  → Completado
Estadías evalúa el reporte        → Aprobado / Rechazado
  si lo rechaza, el docente lo corrige  → Pendiente (vuelve a firmar y enviar)
```

Lo rechazado no está terminado: **la solicitud rechazada se corrige y se
reenvía**, igual que el reporte rechazado. Al guardar la corrección la solicitud
regresa a **Pendiente**, se borra el FO firmado que había (el formato se regenera
con los datos nuevos) y se limpia la decisión anterior.

Por eso cada rol ve cosas distintas:

- **Docente**: las **Rechazadas** le siguen apareciendo en su bandeja, porque le
  toca corregirlas. En su **Histórico** solo van las **Completadas**.
- **Estadías / Administrador**: en su bandeja solo lo que le toca atender, así
  que lo rechazado (solicitudes y reportes) no aparece ahí; queda en el
  **Histórico** y en el detalle de la solicitud.

## Notas para quien le siga

- **Sin pruebas todavía.** JUnit 5 ya está en el `pom.xml`, pero `src/test/`
  aún no existe.
- **Los DAO repiten mucho `try/catch`** con `e.printStackTrace()`. Está
  identificado como deuda técnica; la idea es cambiarlo por SLF4J (ya está
  entre las dependencias) y un helper que quite el boilerplate.
- **Las contraseñas se guardan con SHA-256 sin sal** (`PasswordUtils`). Sirve
  para el proyecto, pero antes de un uso real debería migrar a BCrypt o PBKDF2.
- **Los PDF se guardan en la base como Base64**, no en disco. Por eso el límite
  de 10 MB por archivo.
