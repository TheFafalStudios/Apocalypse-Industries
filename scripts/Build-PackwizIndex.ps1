[CmdletBinding()]
param(
    [string]$InstanceRoot = (Split-Path -Parent $PSScriptRoot),
    [Parameter(Mandatory)][string]$OutputRoot,
    [string]$PythonPath,
    [string]$BaselineRef = 'origin/main'
)
$ErrorActionPreference = 'Stop'
if (-not $PythonPath) {
    $candidates = @('python', (Join-Path $env:USERPROFILE '.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'))
    foreach ($candidate in $candidates) {
        if (Get-Command $candidate -ErrorAction SilentlyContinue) {
            & $candidate -c 'import sys; sys.exit(0 if sys.version_info >= (3,11) else 1)'
            if ($LASTEXITCODE -eq 0) { $PythonPath = $candidate; break }
        }
    }
    if (-not $PythonPath) { throw 'Python 3.11+ is required. Supply -PythonPath with its executable.' }
}
& $PythonPath (Join-Path $PSScriptRoot 'prepare_distribution.py') --instance $InstanceRoot --output $OutputRoot --baseline-ref $BaselineRef
if ($LASTEXITCODE -ne 0) { throw 'Distribution validation failed' }
