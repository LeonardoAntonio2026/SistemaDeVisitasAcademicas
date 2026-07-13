@echo off
rem Arranca la app con Jetty 12 (ee10) para desarrollo
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.11"
cd /d "%~dp0.."
call "%~dp0..\mvnw.cmd" org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.16:run -Djetty.http.port=8090
