[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$Version,

    [Parameter(Mandatory)]
    [string]$Commit,

    [Parameter(Mandatory)]
    [string]$ChangelogPath,

    [string]$TwinRoot = 'Q:\Other Games\ATLauncher\servers\Chunkserve - Apocalypse Industries',

    [string]$VersionsRoot = 'Q:\Other Games\ATLauncher\servers\Apocalypse Industries versions'
)

$ErrorActionPreference = 'Stop'

function Convert-ToRelativePath {
    param([string]$Root, [string]$FullName)
    return [IO.Path]::GetRelativePath($Root, $FullName).Replace('\', '/')
}

function Test-ExcludedServerPath {
    param([string]$RelativePath, [string]$LevelName)

    $segments = $RelativePath -split '/'
    $first = $segments[0]
    $excludedRoots = @(
        $LevelName,
        'world',
        'world_nether',
        'world_the_end',
        'logs',
        'crash-reports',
        'backups'
    ) | Where-Object { $_ }

    if ($excludedRoots -contains $first) { return $true }
    if ($first -eq 'local') { return $true }
    $protectedRootFiles = @(
        'SERVER_PACK_COMPATIBILITY.toml',
        'server.properties',
        'eula.txt',
        'ops.json',
        'whitelist.json',
        'banned-ips.json',
        'banned-players.json',
        'usercache.json',
        'usernamecache.json',
        'start.sh'
    )
    if ($protectedRootFiles -contains $RelativePath) { return $true }
    if ($RelativePath -match '(?i)(^|/)(distant[ _.-]?horizons?|dh[ _.-]?(server[ _.-]?data|lod|lods)|lod[ _.-]?cache|lods)(/|$)') { return $true }
    if ($RelativePath -match '(?i)(^|/)\.git(/|$)') { return $true }
    return $false
}

if (-not (Test-Path -LiteralPath $TwinRoot -PathType Container)) {
    throw "Twin root does not exist: $TwinRoot"
}
if (-not (Test-Path -LiteralPath $ChangelogPath -PathType Leaf)) {
    throw "Changelog source does not exist: $ChangelogPath"
}
if ($Version -notmatch '^[0-9A-Za-z][0-9A-Za-z._-]*$') {
    throw 'Version contains characters that are unsafe for a folder or artifact name.'
}
if ($Commit -notmatch '^[0-9a-fA-F]{7,40}$') {
    throw 'Commit must be a 7-40 character Git commit hash.'
}

$levelName = 'world'
$propertiesPath = Join-Path $TwinRoot 'server.properties'
if (Test-Path -LiteralPath $propertiesPath) {
    $levelLine = Get-Content -LiteralPath $propertiesPath | Where-Object { $_ -match '^level-name=' } | Select-Object -First 1
    if ($levelLine) {
        $configuredLevelName = ($levelLine -split '=', 2)[1].Trim()
        if ($configuredLevelName) { $levelName = $configuredLevelName }
    }
}

New-Item -ItemType Directory -Path $VersionsRoot -Force | Out-Null
$versionRoot = Join-Path $VersionsRoot $Version
if (Test-Path -LiteralPath $versionRoot) {
    throw "Version artifacts already exist and are immutable: $versionRoot"
}

$previousVersion = ''
$compatibilityPath = Join-Path $TwinRoot 'SERVER_PACK_COMPATIBILITY.toml'
if (Test-Path -LiteralPath $compatibilityPath) {
    $compatibilityText = Get-Content -LiteralPath $compatibilityPath -Raw
    if ($compatibilityText -match '(?m)^compatible_pack_version\s*=\s*"([^"]*)"') {
        $previousVersion = $matches[1]
    }
}

$previousEntries = @{}
if ($previousVersion) {
    $previousManifestPath = Join-Path (Join-Path $VersionsRoot $previousVersion) 'SERVER_FILE_MANIFEST.json'
    if (-not (Test-Path -LiteralPath $previousManifestPath)) {
        throw "Previous compatibility version '$previousVersion' has no manifest at $previousManifestPath"
    }
    $previousManifest = Get-Content -LiteralPath $previousManifestPath -Raw | ConvertFrom-Json
    foreach ($entry in $previousManifest.files) { $previousEntries[[string]$entry.path] = $entry }
}

$currentEntries = @{}
Get-ChildItem -LiteralPath $TwinRoot -File -Recurse -Force | ForEach-Object {
    $relative = Convert-ToRelativePath -Root $TwinRoot -FullName $_.FullName
    if (-not (Test-ExcludedServerPath -RelativePath $relative -LevelName $levelName)) {
        $currentEntries[$relative] = [pscustomobject]@{
            path = $relative
            size = $_.Length
            sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        }
    }
}

$changed = @($currentEntries.Keys | Where-Object {
    -not $previousEntries.ContainsKey($_) -or $previousEntries[$_].sha256 -ne $currentEntries[$_].sha256
} | Sort-Object)
$deleted = @($previousEntries.Keys | Where-Object { -not $currentEntries.ContainsKey($_) } | Sort-Object)

$stage = Join-Path $env:TEMP ('apocalypse-server-update-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $stage -Force | Out-Null
try {
    foreach ($relative in $changed) {
        $source = Join-Path $TwinRoot ($relative.Replace('/', '\'))
        $destination = Join-Path $stage ($relative.Replace('/', '\'))
        New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
        Copy-Item -LiteralPath $source -Destination $destination -Force
    }

    $builtAt = [DateTime]::UtcNow.ToString('o')
    $baseVersionValue = if ($previousVersion) { $previousVersion } else { 'none (initial full packet)' }
    $marker = @"
status = "compatible"
compatible_pack_version = "$Version"
compatible_pack_commit = "$($Commit.ToLowerInvariant())"
base_server_version = "$baseVersionValue"
built_at_utc = "$builtAt"
world_data_included = false
distant_horizons_lod_included = false
"@
    [IO.File]::WriteAllText((Join-Path $stage 'SERVER_PACK_COMPATIBILITY.toml'), $marker, [Text.UTF8Encoding]::new($false))

    New-Item -ItemType Directory -Path $versionRoot -Force | Out-Null
    $packetName = "Apocalypse-Industries-Server-Update-$Version.zip"
    $packetPath = Join-Path $versionRoot $packetName
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [IO.Compression.ZipFile]::CreateFromDirectory($stage, $packetPath, [IO.Compression.CompressionLevel]::Optimal, $false)

    $manifest = [pscustomobject]@{
        schema = 1
        pack_version = $Version
        pack_commit = $Commit.ToLowerInvariant()
        base_server_version = $previousVersion
        generated_at_utc = $builtAt
        level_name_excluded = $levelName
        worlds_included = $false
        distant_horizons_lod_included = $false
        files = @($currentEntries.Values | Sort-Object path)
    }
    $manifest | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $versionRoot 'SERVER_FILE_MANIFEST.json') -Encoding utf8

    $deleteText = if ($deleted.Count) { ($deleted -join "`n") + "`n" } else { "# No files need deletion for this update.`n" }
    [IO.File]::WriteAllText((Join-Path $versionRoot 'DELETE_THESE_FILES.txt'), $deleteText, [Text.UTF8Encoding]::new($false))

    $notes = Get-Content -LiteralPath $ChangelogPath -Raw
    $changelog = @"
# Apocalypse Industries server update $Version

- Pack commit: `$Commit`
- Previous server version: `$baseVersionValue`
- Changed/new payload files: $($changed.Count)
- Removed files: $($deleted.Count)
- World/save data included: no
- Distant Horizons LOD data included: no

## Server changes

$notes

## Operator actions

1. Stop Chunkserve and make a provider-side backup.
2. Upload and extract `$packetName` at the server root.
3. Delete every path listed in `DELETE_THESE_FILES.txt`.
4. Start the server and inspect its console for mod/config migration errors.
5. Compare the deployed `SERVER_PACK_COMPATIBILITY.toml` with the local twin marker.
"@
    [IO.File]::WriteAllText((Join-Path $versionRoot 'SERVER_CHANGELOG.md'), $changelog, [Text.UTF8Encoding]::new($false))

    $deployment = @"
# Deploy server update $Version

1. Stop the Chunkserve server.
2. Create a provider-side backup of the live server.
3. Upload `$packetName` to the server root and extract it there, overwriting matching files.
4. Apply `DELETE_THESE_FILES.txt` relative to the server root. Lines beginning with `#` are comments.
5. Do not replace or delete the live world/save directories or Distant Horizons LOD data.
6. Start the server and confirm a clean startup.
7. Verify that the remote and local `SERVER_PACK_COMPATIBILITY.toml` files both report `$Version` and commit `$($Commit.ToLowerInvariant())`.

This packet was generated from the local theoretical twin. It does not prove that the remote upload was completed.
"@
    [IO.File]::WriteAllText((Join-Path $versionRoot 'DEPLOYMENT.md'), $deployment, [Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText((Join-Path $versionRoot 'SERVER_PACK_COMPATIBILITY.toml'), $marker, [Text.UTF8Encoding]::new($false))

    $checksumFiles = @($packetName, 'SERVER_FILE_MANIFEST.json', 'DELETE_THESE_FILES.txt', 'SERVER_CHANGELOG.md', 'DEPLOYMENT.md', 'SERVER_PACK_COMPATIBILITY.toml')
    $checksumLines = foreach ($name in $checksumFiles) {
        $hash = (Get-FileHash -LiteralPath (Join-Path $versionRoot $name) -Algorithm SHA256).Hash.ToLowerInvariant()
        "$hash  $name"
    }
    [IO.File]::WriteAllText((Join-Path $versionRoot 'SHA256SUMS.txt'), (($checksumLines -join "`n") + "`n"), [Text.UTF8Encoding]::new($false))

    [IO.File]::WriteAllText($compatibilityPath, $marker, [Text.UTF8Encoding]::new($false))

    [pscustomobject]@{
        VersionFolder = $versionRoot
        Packet = $packetPath
        BaseVersion = $previousVersion
        ChangedOrNewFiles = $changed.Count
        DeletedFiles = $deleted.Count
        ExpectedServerFiles = $currentEntries.Count
        ExcludedLevelName = $levelName
    }
}
finally {
    if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
}