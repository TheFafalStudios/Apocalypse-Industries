[CmdletBinding()]
param(
    [string]$InstanceRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
$instancePath = Join-Path $InstanceRoot 'instance.json'
$modsPath = Join-Path $InstanceRoot 'mods'
$instance = Get-Content -LiteralPath $instancePath -Raw | ConvertFrom-Json
$activeFiles = @{}
Get-ChildItem -LiteralPath $modsPath -File -Filter '*.jar' | ForEach-Object {
    $activeFiles[$_.Name.ToLowerInvariant()] = $_
}

$created = 0
$unmatched = [System.Collections.Generic.List[string]]::new()
$handled = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)

foreach ($mod in $instance.launcher.mods) {
    if ($mod.disabled -or -not $mod.file) { continue }
    $key = ([string]$mod.file).ToLowerInvariant()
    if (-not $activeFiles.ContainsKey($key)) { continue }

    $jar = $activeFiles[$key]
    $safeBase = [IO.Path]::GetFileNameWithoutExtension($jar.Name) -replace '[^A-Za-z0-9._-]', '-'
    $metadataPath = Join-Path $modsPath ($safeBase + '.pw.toml')
    $name = ([string]$mod.name).Replace('"', '\"')
    $filename = $jar.Name.Replace('"', '\"')

    if ($mod.curseForgeProjectId -and $mod.curseForgeFileId) {
        $hash = (Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA1).Hash.ToLowerInvariant()
        $content = @"
name = "$name"
filename = "$filename"
side = "both"

[download]
hash-format = "sha1"
hash = "$hash"
mode = "metadata:curseforge"

[update]
[update.curseforge]
file-id = $($mod.curseForgeFileId)
project-id = $($mod.curseForgeProjectId)
"@
        Set-Content -LiteralPath $metadataPath -Value $content -Encoding utf8NoBOM
        $created++
        $null = $handled.Add($jar.Name)
        continue
    }

    if ($mod.modrinthVersion.files) {
        $remoteFile = $mod.modrinthVersion.files | Where-Object { $_.filename -eq $jar.Name } | Select-Object -First 1
        if (-not $remoteFile) { $remoteFile = $mod.modrinthVersion.files | Where-Object primary | Select-Object -First 1 }
        if ($remoteFile -and $remoteFile.url -and $remoteFile.hashes.sha1) {
            $content = @"
name = "$name"
filename = "$filename"
side = "both"

[download]
url = "$($remoteFile.url)"
hash-format = "sha1"
hash = "$($remoteFile.hashes.sha1)"

[update]
[update.modrinth]
mod-id = "$($mod.modrinthProject.id)"
version = "$($mod.modrinthVersion.id)"
"@
            Set-Content -LiteralPath $metadataPath -Value $content -Encoding utf8NoBOM
            $created++
            $null = $handled.Add($jar.Name)
            continue
        }
    }

    $unmatched.Add($jar.Name)
    $null = $handled.Add($jar.Name)
}

foreach ($jar in $activeFiles.Values) {
    if (-not $handled.Contains($jar.Name)) {
        $unmatched.Add($jar.Name)
    }
}

$reportPath = Join-Path $InstanceRoot 'docs/unmatched-mods.txt'
$unmatched | Sort-Object | Set-Content -LiteralPath $reportPath -Encoding utf8NoBOM
Write-Output "Created $created Packwiz mod references."
Write-Output "$($unmatched.Count) active JARs require manual source/license review; see docs/unmatched-mods.txt."
