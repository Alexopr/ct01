@echo off
echo Starting backend with Telegram support...

set /p TELEGRAM_BOT_TOKEN="Enter your Telegram bot token: "

if "%TELEGRAM_BOT_TOKEN%"=="" (
    echo Error: Telegram bot token is required!
    pause
    exit /b 1
)

cd backend
set TELEGRAM_BOT_TOKEN=%TELEGRAM_BOT_TOKEN%
mvnw.cmd spring-boot:run

pause 