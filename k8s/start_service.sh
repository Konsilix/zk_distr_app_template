#!/bin/bash
set -e

echo "Starting the application service..."
docker compose up -d

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Application service started successfully."
else
    echo "Application service via Docker failed!"
fi
