$ErrorActionPreference = "Stop"

# JavaFX is the default SAFAD UI. This helper compiles all sources and launches it.
# The legacy Swing UI can still be run from LegacySwingMain if needed.
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$outDir = Join-Path $projectRoot "out\production\javafx-run"
$mysqlJar = Join-Path $projectRoot "lib\mysql-connector-j-9.7.0.jar"

if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

$sources = Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter *.java |
        Where-Object { $_.FullName -notlike "*\src\out\*" } |
        ForEach-Object { $_.FullName }

if ($env:JAVAFX_HOME -and (Test-Path (Join-Path $env:JAVAFX_HOME "lib"))) {
    $fxLib = Join-Path $env:JAVAFX_HOME "lib"
    javac --module-path $fxLib --add-modules javafx.controls -cp $mysqlJar -d $outDir $sources
    java --module-path $fxLib --add-modules javafx.controls -cp "$outDir;$mysqlJar" ui.fx.FxMainApp
    exit
}

$legacyFxJar = "C:\Program Files\Java\jre1.8.0_441\lib\ext\jfxrt.jar"
if (Test-Path $legacyFxJar) {
    javac -cp "$mysqlJar;$legacyFxJar" -d $outDir $sources
    java -cp "$outDir;$mysqlJar;$legacyFxJar" ui.fx.FxMainApp
    exit
}

Write-Host "JavaFX SDK was not found."
Write-Host "Install OpenJFX for your JDK, then set JAVAFX_HOME to the JavaFX SDK folder."
Write-Host "Example: `$env:JAVAFX_HOME='C:\javafx-sdk-23.0.2'"
