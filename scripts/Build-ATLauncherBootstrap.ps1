[CmdletBinding()]
param([string]$InstanceRoot = (Split-Path -Parent $PSScriptRoot))
$ErrorActionPreference = 'Stop'
$bootstrap = Join-Path $InstanceRoot 'bootstrap/packwiz-installer-bootstrap.jar'
if (-not (Test-Path -LiteralPath $bootstrap)) { throw "Missing $bootstrap" }
$stage = Join-Path $env:TEMP ("apocalypse-atlauncher-" + [guid]::NewGuid().ToString('N'))
$minecraftDir = Join-Path $stage '.minecraft'
New-Item -ItemType Directory -Path $minecraftDir -Force | Out-Null
try {
    Copy-Item -LiteralPath $bootstrap -Destination (Join-Path $minecraftDir 'packwiz-installer-bootstrap.jar')
    $instanceCfg = @'
InstanceType=OneSix
name=Apocalypse Industries
OverrideCommands=true
PreLaunchCommand="$INST_JAVA" -jar packwiz-installer-bootstrap.jar https://raw.githubusercontent.com/TheFafalStudios/Apocalypse-Industries/main/pack.toml
'@
    [IO.File]::WriteAllText((Join-Path $stage 'instance.cfg'), $instanceCfg, [Text.UTF8Encoding]::new($false))
    $mmcPack = @'
{
  "components": [
    {"cachedName":"Minecraft","cachedVersion":"1.21.1","cachedVolatile":true,"dependencyOnly":false,"uid":"net.minecraft","version":"1.21.1"},
    {"cachedName":"NeoForge","cachedVersion":"21.1.248","cachedVolatile":true,"dependencyOnly":false,"uid":"net.neoforged","version":"21.1.248"}
  ],
  "formatVersion": 1
}
'@
    [IO.File]::WriteAllText((Join-Path $stage 'mmc-pack.json'), $mmcPack, [Text.UTF8Encoding]::new($false))
    $dist = Join-Path $InstanceRoot 'dist'
    New-Item -ItemType Directory -Path $dist -Force | Out-Null
    $output = Join-Path $dist 'Apocalypse-Industries-ATLauncher.zip'
    if (Test-Path -LiteralPath $output) { Remove-Item -LiteralPath $output -Force }
    Compress-Archive -Path (Join-Path $stage '*') -DestinationPath $output -CompressionLevel Optimal
    Write-Output $output
} finally {
    if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
}
