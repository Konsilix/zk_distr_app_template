#!/bin/bash
set -e

echo "Stopping the application service..."

# The 'docker compose down' command has been removed.
# The following line uninstalls your application from Kubernetes using Helm.
helm uninstall chatbot

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Application service stopped successfully."
else
    echo "Application service via Helm failed!"
fi
