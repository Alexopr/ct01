#!/bin/bash

echo "Starting backend with Telegram support..."

read -p "Enter your Telegram bot token: " TELEGRAM_BOT_TOKEN

if [ -z "$TELEGRAM_BOT_TOKEN" ]; then
    echo "Error: Telegram bot token is required!"
    exit 1
fi

cd backend
export TELEGRAM_BOT_TOKEN="$TELEGRAM_BOT_TOKEN"
./mvnw spring-boot:run 