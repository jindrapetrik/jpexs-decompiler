# Downloads the official OpenCode CLI 2.2.5 Windows x64 binary.
# The binary is intentionally not stored in Git. Run this script before ant build.
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$Version = '2.2.5'
$ExpectedSha256 = '0C346FDAF519D2F23A31D3B6CB9D65E369A72D77CAC6F027B5E0A06376EC3A43'
$PackageUrl = "https://registry.npmjs.org/@opencode/cli-windows-x64/-/cli-windows-x64-$Version.tgz"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Split-Path -Parent $ScriptDir
$DestinationDir = Join-Path $RepoRoot 'resources\opencode'
$Destination = Join-Path $DestinationDir 'opencode.exe'
$TempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("ffdec-opencode-" + [guid]::NewGuid().ToString('N'))
$Tarball = Join-Path $TempDir "opencode-cli-windows-x64-$Version.tgz"
$ExtractDir = Join-Path $TempDir 'extracted'

try {
    New-Item -ItemType Directory -Path $DestinationDir -Force | Out-Null
    New-Item -ItemType Directory -Path $TempDir -Force | Out-Null

    Write-Host "Downloading OpenCode CLI $Version from npm registry..."
    Invoke-WebRequest -Uri $PackageUrl -OutFile $Tarball -UseBasicParsing

    New-Item -ItemType Directory -Path $ExtractDir -Force | Out-Null
    tar -xzf $Tarball -C $ExtractDir
    if ($LASTEXITCODE -ne 0) {
        throw "tar failed with exit code $LASTEXITCODE"
    }

    $Binary = Join-Path $ExtractDir 'package\bin\opencode.exe'
    if (-not (Test-Path -LiteralPath $Binary -PathType Leaf)) {
        throw "opencode.exe was not found in the downloaded package."
    }

    $Hash = (Get-FileHash -LiteralPath $Binary -Algorithm SHA256).Hash
    if (-not $Hash.Equals($ExpectedSha256, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "SHA-256 mismatch. Expected $ExpectedSha256, got $Hash"
    }

    Copy-Item -LiteralPath $Binary -Destination $Destination -Force
    Write-Host "OpenCode CLI downloaded and verified at $Destination"
} finally {
    if (Test-Path -LiteralPath $TempDir) {
        Remove-Item -LiteralPath $TempDir -Recurse -Force
    }
}
