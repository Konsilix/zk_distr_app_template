#!/bin/bash
set -e

echo "Starting the application service..."

# The 'docker compose up -d' command has been removed.
# The following line deploys your application to Kubernetes using Helm.
helm upgrade --install chatbot ./helm/chatbot

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Application service started successfully."
else
    echo "Application service via Helm failed!"
fi
