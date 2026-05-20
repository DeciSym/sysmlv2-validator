# SysML v2 Validator wrapper script
# Validates .sysml files using the SysML v2 Pilot Implementation

$ErrorActionPreference = 'Stop'

function Write-Stderr {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    [Console]::Error.WriteLine($Message)
}

function Resolve-ScriptPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $currentPath = (Resolve-Path -LiteralPath $Path).ProviderPath

    while ($true) {
        $item = Get-Item -LiteralPath $currentPath

        if (-not $item.LinkType -or -not $item.Target) {
            return $item.FullName
        }

        $target = $item.Target
        if ($target -is [array]) {
            $target = $target[0]
        }

        if ([System.IO.Path]::IsPathRooted($target)) {
            $currentPath = $target
        } else {
            $currentPath = Join-Path -Path $item.DirectoryName -ChildPath $target
        }

        $currentPath = (Resolve-Path -LiteralPath $currentPath).ProviderPath
    }
}

function Get-JavaMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JavaPath
    )

    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = $JavaPath
    $processInfo.Arguments = '-version'
    $processInfo.RedirectStandardError = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.UseShellExecute = $false

    $process = [System.Diagnostics.Process]::Start($processInfo)
    $stderr = $process.StandardError.ReadToEnd()
    $stdout = $process.StandardOutput.ReadToEnd()
    $process.WaitForExit()

    if ($process.ExitCode -ne 0) {
        return $null
    }

    $versionLine = (($stderr + [Environment]::NewLine + $stdout) -split "`r?`n" |
        Where-Object { $_.Trim() } |
        Select-Object -First 1)

    if (-not ($versionLine -match '"([^"]+)"')) {
        return $null
    }

    $version = $Matches[1]
    $versionParts = $version -split '[._-]'
    if ($versionParts.Count -gt 1 -and $versionParts[0] -eq '1') {
        return [int]$versionParts[1]
    }

    return [int]$versionParts[0]
}

function Find-Java21 {
    $javaCandidates = @()

    if ($env:JAVA_HOME) {
        $javaCandidates += Join-Path -Path $env:JAVA_HOME -ChildPath 'bin\java.exe'
    }

    $pathJava = Get-Command -Name java -ErrorAction SilentlyContinue
    if ($pathJava) {
        $javaCandidates += $pathJava.Source
    }

    $javaInstallRoots = @(
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java',
        'C:\Program Files\Microsoft',
        'C:\Program Files\Amazon Corretto'
    )

    foreach ($root in $javaInstallRoots) {
        if (-not (Test-Path -LiteralPath $root -PathType Container)) {
            continue
        }

        Get-ChildItem -Directory -LiteralPath $root -ErrorAction SilentlyContinue |
            Sort-Object -Property Name -Descending |
            ForEach-Object {
                $javaCandidates += Join-Path -Path $_.FullName -ChildPath 'bin\java.exe'
            }
    }

    foreach ($candidate in ($javaCandidates | Where-Object { $_ } | Select-Object -Unique)) {
        if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
            continue
        }

        $majorVersion = Get-JavaMajorVersion -JavaPath $candidate
        if ($majorVersion -ge 21) {
            return $candidate
        }
    }

    return $null
}

$scriptPath = $PSCommandPath
if (-not $scriptPath) {
    $scriptPath = $MyInvocation.MyCommand.Path
}

if (-not $scriptPath) {
    Write-Stderr 'Error: unable to determine script path'
    exit 1
}

$scriptDir = Split-Path -Parent (Resolve-ScriptPath -Path $scriptPath)

# Use the Maven build output relative to this script's repository location.
$jar = Join-Path -Path $scriptDir -ChildPath 'target\sysmlv2-validator-1.0.0-SNAPSHOT.jar'
$sysmlLibrary = Join-Path -Path $scriptDir -ChildPath 'target\sysml-download\sysml\sysml.library'

if (-not ((Test-Path -LiteralPath $jar -PathType Leaf) -and (Test-Path -LiteralPath $sysmlLibrary -PathType Container))) {
    Write-Stderr 'Error: sysmlv2-validator not found.'
    Write-Stderr "Run these commands in ${scriptDir}:"
    Write-Stderr '  mvn -Psetup-dependency initialize'
    Write-Stderr '  mvn package'
    exit 1
}

# Find Java 21+.
$java = Find-Java21
if (-not $java) {
    Write-Stderr 'Error: Java 21+ not found'
    exit 1
}

# Run validator.
& $java "-Dsysml.library=$sysmlLibrary" -jar $jar @args
exit $LASTEXITCODE
