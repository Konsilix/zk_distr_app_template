#!/bin/bash
set -e

echo "Stopping the application service..."
# The following line was removed as it is not needed when using Helm.
# docker compose down

# Your Helm undeployment command would go here, for example:
# helm uninstall chatbot

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Application service stopped successfully."
else
    echo "Application service via Helm failed!"
fi
