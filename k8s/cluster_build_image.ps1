#
# Cluster Build Image Script
#
# This script is used to build the Docker image for the application.
#

#docker context use default
docker context use desktop-linux

# Define Variables
$IMAGE = "theApp"
$VERSION = "v1"

# Stop script execution on any error
$ErrorActionPreference = 'Stop'

# Change to Parent Directory
Set-Location ".."

$CompileProcessOptions = @{
    FilePath = "mvn.cmd"
    ArgumentList = @("clean", "package", "-DskipTests=true")
    UseNewEnvironment = $true
}

# Build Project (equivalent to mvn clean package -DskipTests=true)
Write-Host "Starting the application service..."
Start-Process -NoNewWindow -Wait @CompileProcessOptions

# Copy JAR File
$jarFile = Get-ChildItem -Path "target" -Filter "*.jar" | Select-Object -First 1
Copy-Item $jarFile.FullName "./k8s"

# Change to 'docker' Directory
Set-Location "./k8s"

# Remove Existing Docker Image (If It Exists)
if (docker image ls | Where-Object { $_ -match "${IMAGE}:${VERSION}" }) {
    docker image rm "${IMAGE}:${VERSION}"
}

$BuildProcessOptions = @{
    FilePath = "minikube"
    ArgumentList = @("image", "build", "-t", "${IMAGE}:${VERSION}", ".")
    UseNewEnvironment = $true
}

# Build Docker Image
Write-Host "Building the Docker image..."
Start-Process -NoNewWindow -Wait @BuildProcessOptions
Remove-Item -Path $jarFile.FullName
Write-Host "Built the Docker image. Enjoy!"

