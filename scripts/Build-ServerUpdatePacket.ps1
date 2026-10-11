[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$Version,
    [Parameter(Mandatory)][string]$ClientVersion,
    [Parameter(Mandatory)][string]$Commit,
    [Parameter(Mandatory)][string]$ChangelogPath,
    [Parameter(Mandatory)][string]$BeforeRoot,
    [Parameter(Mandatory)][string]$CandidateRoot,
    [string[]]$Paths = @(),
    [string[]]$ReviewPaths = @(),
    [string]$BaselineRoot,
    [string]$AttemptId,
    [string]$VersionsRoot = 'Q:\Other Games\ATLauncher\servers\Apocalypse Industries versions',
    [string]$PythonPath = 'python',
    [switch]$Full,
    [switch]$CreateZip,
    [switch]$MismatchApproved,
    [switch]$BaselineVerified
)
$ErrorActionPreference = 'Stop'
# Preparing a record never deploys or writes a target's compatibility marker.
if ($Version -notmatch '^[A-Za-z0-9][A-Za-z0-9._-]*$') { throw 'Invalid version' }
if ($AttemptId -and $AttemptId -notmatch '^[A-Za-z0-9][A-Za-z0-9._-]*$') { throw 'Invalid attempt ID' }
$recordRoot = Join-Path $VersionsRoot $Version
if ($AttemptId) { $recordRoot = Join-Path (Join-Path $recordRoot 'attempts') $AttemptId }
& $PythonPath -c 'import sys; assert sys.version_info >= (3,11), "Python 3.11+ required; supply -PythonPath"'
if ($LASTEXITCODE -ne 0) { throw 'Python 3.11+ required; supply -PythonPath' }
$recordArgs = @((Join-Path $PSScriptRoot 'build_server_deployment_record.py'),
    '--version',$Version,'--client-version',$ClientVersion,'--commit',$Commit,
    '--changelog',$ChangelogPath,'--before-root',$BeforeRoot,'--candidate-root',$CandidateRoot,'--output',$recordRoot)
foreach ($relative in $Paths) { $recordArgs += @('--path',$relative) }
foreach ($relative in $ReviewPaths) { $recordArgs += @('--review-path',$relative) }
if ($BaselineRoot) { $recordArgs += @('--baseline-root',$BaselineRoot) }
if ($Full) { $recordArgs += '--full' }
if ($CreateZip) { $recordArgs += '--create-zip' }
if ($MismatchApproved) { $recordArgs += '--mismatch-approved' }
if ($BaselineVerified) { $recordArgs += '--baseline-verified' }
& $PythonPath @recordArgs
if ($LASTEXITCODE -ne 0) { throw 'Deployment record preparation failed' }