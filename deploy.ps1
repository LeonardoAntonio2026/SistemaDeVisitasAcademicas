# deploy.ps1 - Compila el WAR y lo despliega en el servidor de Oracle Cloud.
#
#   .\deploy.ps1              compila y despliega
#   .\deploy.ps1 -SkipBuild   despliega el WAR que ya esta en target\

param(
    [string]$Server = "161.153.57.186",
    [string]$User   = "opc",
    [string]$Key    = "C:\Users\PC\Downloads\LlavaInstanciaSGVA.key",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$JavaHome  = "C:\Program Files\Java\jdk-21.0.11"
$TargetDir = Join-Path $PSScriptRoot "target"
$WarRemote = "/opt/tomcat11/webapps/ROOT.war"
$Staging   = "/tmp/deploy-staging.war"
$Url       = "http://$Server`:8080/"

function Paso($n, $texto) { Write-Host "`n[$n] $texto" -ForegroundColor Cyan }

# --- Comprobaciones previas -------------------------------------------------
if (-not (Test-Path $Key))      { throw "No encuentro la clave SSH en: $Key" }
if (-not (Test-Path $JavaHome)) { throw "No encuentro el JDK en: $JavaHome" }

# --- 1. Compilar ------------------------------------------------------------
if ($SkipBuild) {
    Paso 1 "Compilacion omitida (-SkipBuild)"
} else {
    Paso 1 "Compilando con Maven..."
    $env:JAVA_HOME = $JavaHome
    & (Join-Path $PSScriptRoot "mvnw.cmd") -q clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "La compilacion fallo. No se despliega nada." }
}

# Se busca el WAR en vez de asumir su nombre: depende del <artifactId> del
# pom.xml, y si ese cambia (ya paso una vez) un nombre fijo rompe el script.
$war = Get-ChildItem (Join-Path $TargetDir "*.war") -ErrorAction SilentlyContinue |
       Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $war) { throw "No hay ningun .war en $TargetDir" }
$WarLocal = $war.FullName
$tam = [math]::Round($war.Length / 1MB, 1)
Write-Host "    WAR listo: $($war.Name) ($tam MB)" -ForegroundColor Green

# --- 2. Subir a /tmp --------------------------------------------------------
# Se sube primero a /tmp y despues se mueve. Si se copiara directo sobre
# ROOT.war, Tomcat podria detectar el archivo a medio escribir e intentar
# desplegarlo incompleto. El 'mv' dentro del mismo disco es atomico: el
# archivo aparece entero o no aparece.
Paso 2 "Subiendo al servidor..."
& scp -i $Key -o StrictHostKeyChecking=accept-new $WarLocal "${User}@${Server}:$Staging"
if ($LASTEXITCODE -ne 0) { throw "Fallo el scp." }

# --- 3. Instalar ------------------------------------------------------------
Paso 3 "Instalando en webapps/ y esperando el autoDeploy..."
$remoto = "sudo mv $Staging $WarRemote && sudo chown tomcat:tomcat $WarRemote && echo instalado && sleep 25 && sudo tail -3 /opt/tomcat11/logs/catalina.out"
& ssh -i $Key -o StrictHostKeyChecking=accept-new "${User}@${Server}" $remoto
if ($LASTEXITCODE -ne 0) { throw "Fallo la instalacion en el servidor." }

# --- 4. Verificar -----------------------------------------------------------
Paso 4 "Verificando desde internet..."
try {
    $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 25
    if ($r.StatusCode -eq 200) {
        Write-Host "`n  OK - $Url responde HTTP 200" -ForegroundColor Green
    } else {
        Write-Host "`n  Responde HTTP $($r.StatusCode) - revisa catalina.out" -ForegroundColor Yellow
    }
} catch {
    Write-Host "`n  No respondio. Revisa el log:" -ForegroundColor Red
    Write-Host "  ssh -i `"$Key`" $User@$Server 'sudo tail -40 /opt/tomcat11/logs/catalina.out'"
    exit 1
}
