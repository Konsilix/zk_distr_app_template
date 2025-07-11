# Stop script execution on any error
$ErrorActionPreference = 'Stop'

$DownProcessOptions = @{
    FilePath = "docker"
    ArgumentList = @("compose", "down")
    UseNewEnvironment = $true
}

Write-Host "Stopping the application service..."
Start-Process -NoNewWindow -Wait @DownProcessOptions

# Check the exit status of the last command
if ($LASTEXITCODE -eq 0) {
    Write-Host "Application service stopped successfully."
} else {
    Write-Host "Application service via Docker failed!"
}