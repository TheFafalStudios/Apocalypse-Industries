[CmdletBinding()]
param(
    [string]$InstanceRoot = (Split-Path -Parent $PSScriptRoot),
    [Parameter(Mandatory)]
    [string]$PackwizPath
)
$ErrorActionPreference = 'Stop'
$stage = Join-Path $env:TEMP ("apocalypse-packwiz-stage-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $stage | Out-Null
try {
    foreach ($file in 'pack.toml', 'index.toml') {
        Copy-Item -LiteralPath (Join-Path $InstanceRoot $file) -Destination (Join-Path $stage $file)
    }
    foreach ($root in 'config', 'defaultconfigs', 'kubejs', 'datapacks', 'resourcepacks') {
        $sourceRoot = Join-Path $InstanceRoot $root
        Get-ChildItem -LiteralPath $sourceRoot -File -Recurse | ForEach-Object {
            $relative = [IO.Path]::GetRelativePath($InstanceRoot, $_.FullName).Replace('\', '/')
            $blocked =
                $relative -eq 'kubejs/config/web_server.json' -or
                $relative -like 'config/jei/world/*' -or
                $relative -like 'config/xaero/*' -or
                $relative -eq 'config/fancymenu/user_variables.db' -or
                $relative -match '\.(bak|old|log|db|sqlite|sqlite3)$'
            if ($blocked) { return }
            $destination = Join-Path $stage $relative
            New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destination) | Out-Null
            Copy-Item -LiteralPath $_.FullName -Destination $destination
        }
    }
    $stageMods = Join-Path $stage 'mods'
    New-Item -ItemType Directory -Force -Path $stageMods | Out-Null
    Get-ChildItem -LiteralPath (Join-Path $InstanceRoot 'mods') -File -Filter '*.pw.toml' | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $stageMods $_.Name)
    }
    foreach ($patchedJar in @(
        'moogs_paths-neoforge-1.21.1-1.0.2-hotfix-sable-chunksafety.jar',
        'weather2-neoforge-1.21.0-2.8.7-hotfix-chunksafety.jar',
        'weather2compat-1.6.0-hotfix-threadsafe.jar'
    )) {
        Copy-Item -LiteralPath (Join-Path $InstanceRoot "mods/$patchedJar") -Destination (Join-Path $stageMods $patchedJar)
    }
    Push-Location $stage
    try {
        & $PackwizPath refresh
        if ($LASTEXITCODE -ne 0) { throw "packwiz refresh failed with exit code $LASTEXITCODE" }
    } finally { Pop-Location }
    Copy-Item -LiteralPath (Join-Path $stage 'pack.toml') -Destination (Join-Path $InstanceRoot 'pack.toml') -Force
    Copy-Item -LiteralPath (Join-Path $stage 'index.toml') -Destination (Join-Path $InstanceRoot 'index.toml') -Force
} finally {
    if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
}
