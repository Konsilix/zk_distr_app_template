#!/bin/bash
set -e

echo "Starting the application service..."
# The following line was removed as it is not needed when using Helm.
# docker compose up -d

# Helm deployment command would go here, for example:
# helm upgrade --install chatbot ./helm/chatbot

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Application service started successfully."
else
    echo "Application service via Helm failed!"
fi
