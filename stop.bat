@echo off
echo ========================================
echo Stopping EduPath Insight Services
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running.
    pause
    exit /b 1
)

echo Stopping all services...
docker-compose down

if errorlevel 1 (
    echo.
    echo ERROR: Failed to stop services.
    pause
    exit /b 1
)

echo.
echo ========================================
echo All services have been stopped.
echo ========================================
echo.
echo To remove volumes (delete data), run: docker-compose down -v
echo.

pause


