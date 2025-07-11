# Stop script execution on any error
$ErrorActionPreference = 'Stop'

$UpProcessOptions = @{
    FilePath = "docker"
    ArgumentList = @("compose", "up", "-d")
    UseNewEnvironment = $true
}

Write-Host "Starting the application service..."
Start-Process -NoNewWindow -Wait @UpProcessOptions

# Check the exit status of the last command
if ($LASTEXITCODE -eq 0) {
    Write-Host "Application service started successfully."
} else {
    Write-Host "Application service via Docker failed!"
}