@echo off
rem Arranca la app con Jetty 12 (ee10) para desarrollo
set "JAVA_HOME=C:\Program Files\Java\jdk-24"
rem tmpdir corto: evita "Unable to establish loopback connection" del JDK
rem (los sockets unix-domain del Pipe fallan con el TEMP de perfil en esta maquina)
if not exist "C:\jtmp" mkdir "C:\jtmp"
set "MAVEN_OPTS=-Djava.io.tmpdir=C:\jtmp"
cd /d "%~dp0.."
call "%~dp0..\mvnw.cmd" org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.16:run -Djetty.http.port=8090
