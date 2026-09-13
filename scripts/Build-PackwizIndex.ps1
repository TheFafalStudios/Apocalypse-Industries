[CmdletBinding()]
param([Parameter(Mandatory)][string]$InstanceRoot, [Parameter(Mandatory)][string]$OutputRoot, [string]$PythonPath = "python")
$ErrorActionPreference = "Stop"
& $PythonPath (Join-Path $PSScriptRoot "prepare_distribution.py") --instance $InstanceRoot --output $OutputRoot
if ($LASTEXITCODE -ne 0) { throw "Distribution validation failed" }
