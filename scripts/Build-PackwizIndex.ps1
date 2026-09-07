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

    $allowedRoots = 'config', 'defaultconfigs', 'kubejs', 'datapacks'
    foreach ($root in $allowedRoots) {
        $sourceRoot = Join-Path $InstanceRoot $root
        Get-ChildItem -LiteralPath $sourceRoot -File -Recurse | ForEach-Object {
            $relative = [IO.Path]::GetRelativePath($InstanceRoot, $_.FullName).Replace('\', '/')
            $blocked =
                $relative -eq 'kubejs/config/web_server.json' -or
                $relative -like 'config/jei/world/*' -or
                $relative -like 'config/xaero/*' -or
                $relative -eq 'config/fancymenu/user_variables.db' -or
                $relative -match '\.(bak|old|log|db|sqlite|sqlite3)$' -or
                $relative -eq 'datapacks/OWZA_Lite_LostCities_1.20.1_v0.1.0.zip' -or
                $relative -eq 'datapacks/village-nullifier-1.18.2-1.0.0.zip'
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

    Push-Location $stage
    try {
        & $PackwizPath refresh
        if ($LASTEXITCODE -ne 0) { throw "packwiz refresh failed with exit code $LASTEXITCODE" }
    }
    finally {
        Pop-Location
    }

    Copy-Item -LiteralPath (Join-Path $stage 'pack.toml') -Destination (Join-Path $InstanceRoot 'pack.toml') -Force
    Copy-Item -LiteralPath (Join-Path $stage 'index.toml') -Destination (Join-Path $InstanceRoot 'index.toml') -Force
}
finally {
    if (Test-Path -LiteralPath $stage) {
        Remove-Item -LiteralPath $stage -Recurse -Force
    }
}
