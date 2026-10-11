[CmdletBinding()]
param(
    [string]$ProfilePath = (Join-Path $PSScriptRoot '..\.local\chunkserve\connection.json')
)
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $ProfilePath -PathType Leaf)) {
    throw "Saved connection profile is missing: $ProfilePath. See docs/CHUNKSERVE_ACCESS.md."
}
$connection = Get-Content -LiteralPath $ProfilePath -Raw | ConvertFrom-Json
if ($connection.schema -ne 1 -or $connection.host -notmatch '^[a-zA-Z0-9.-]+$' -or
    $connection.username -notmatch '^[a-zA-Z0-9._-]+$' -or
    $connection.port -lt 1 -or $connection.port -gt 65535) {
    throw 'Invalid Chunkserve connection profile.'
}
$openSshRoot = Join-Path $env:WINDIR 'System32\OpenSSH'
$sftpExe = Join-Path $openSshRoot 'sftp.exe'
$sshAddExe = Join-Path $openSshRoot 'ssh-add.exe'
foreach ($requiredFile in @($sftpExe, $sshAddExe, $connection.identityFile, $connection.knownHostsFile)) {
    if (-not (Test-Path -LiteralPath $requiredFile -PathType Leaf)) { throw "Required local file is missing: $requiredFile" }
}
# Fingerprints only: never display or export private key material.
$agentKeys = @(& $sshAddExe -l 2>&1)
if ($LASTEXITCODE -ne 0 -or -not ($agentKeys -match [regex]::Escape($connection.identityFingerprint))) {
    throw 'The saved key is not loaded in Windows ssh-agent. The user should run ssh-add with the identityFile from connection.json in their own PowerShell and enter the passphrase locally. Do not generate a replacement key or request its secret.'
}
# Only pwd and bye are accepted here. No file listing, traversal, transfers or writes.
$sftpArguments = @(
    '-b', '-', '-P', [string]$connection.port,
    '-i', [string]$connection.identityFile,
    '-o', 'BatchMode=yes',
    '-o', 'IdentitiesOnly=yes',
    '-o', 'PreferredAuthentications=publickey',
    '-o', 'PasswordAuthentication=no',
    '-o', 'StrictHostKeyChecking=yes',
    '-o', ('UserKnownHostsFile="' + $connection.knownHostsFile + '"'),
    '-o', 'ConnectTimeout=20',
    ('{0}@{1}' -f $connection.username, $connection.host)
)
$output = @(@('pwd', 'bye') | & $sftpExe @sftpArguments 2>&1)
if ($LASTEXITCODE -ne 0) { throw "Chunkserve SFTP check failed. No remote files changed.`n$($output -join "`n")" }
[pscustomobject]@{
    Status = 'Connected using SSH key and pinned host key'
    Endpoint = '{0}:{1}' -f $connection.host, $connection.port
    RemoteRoot = '/'
    RemoteFilesChanged = $false
    CheckedAtUtc = [DateTime]::UtcNow.ToString('o')
}