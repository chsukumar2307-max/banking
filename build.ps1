param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs
)

$workspaceRoot = $PSScriptRoot
$projectRoot = Join-Path $workspaceRoot 'banking'
$gradleWrapper = Join-Path $projectRoot 'gradlew.bat'

if (-not (Test-Path $projectRoot)) {
    Write-Error "Nested project directory not found: $projectRoot"
    exit 1
}

if (-not (Test-Path $gradleWrapper)) {
    Write-Error "Gradle wrapper not found: $gradleWrapper"
    exit 1
}

if (-not $GradleArgs -or $GradleArgs.Count -eq 0) {
    $GradleArgs = @('build')
}

& $gradleWrapper '-p' $projectRoot @GradleArgs
exit $LASTEXITCODE

