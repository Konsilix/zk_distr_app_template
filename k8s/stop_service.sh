#!/bin/bash
set -e

echo "Stopping the application service..."
docker compose down

# Check the exit status of the last command
if [ $? -eq 0 ]; then
    echo "Application service stopped successfully."
else
    echo "Application service via Docker failed!"
fi
