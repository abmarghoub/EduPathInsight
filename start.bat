@echo off
echo ========================================
echo Starting EduPath Insight Services
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

echo Building and starting all services...
echo This may take several minutes on first run...
echo.
echo IMPORTANT: The prediction-ia-service build may take 10-20 minutes
echo due to PyTorch dependencies (~2GB download).
echo.
echo If the build fails with "rpc error", try:
echo   1. Increase Docker Desktop memory (Settings -^> Resources -^> Memory: 4GB+)
echo   2. Build separately: build-prediction-service.bat
echo   3. Check your internet connection
echo.

REM Build and start all services
docker-compose up -d --build

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start services.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Services are starting...
echo ========================================
echo.
echo Waiting for services to be healthy...
timeout /t 30 /nobreak >nul

echo.
echo ========================================
echo Service URLs:
echo ========================================
echo Eureka Server:        http://localhost:8761
echo API Gateway:          http://localhost:8080
echo Auth Service:         http://localhost:8081
echo Data Ingestion:       http://localhost:8082
echo Module Service:       http://localhost:8083
echo Note Service:         http://localhost:8084
echo Activities Service:   http://localhost:5000
echo Prediction IA:        http://localhost:8001
echo Explainability:       http://localhost:5001
echo Notification Service: http://localhost:8085
echo Admin Frontend:       http://localhost:3003
echo.
echo RabbitMQ Management:  http://localhost:15672 (guest/guest)
echo Neo4j Browser:        http://localhost:7474 (neo4j/password)
echo.
echo ========================================
echo To view logs: docker-compose logs -f
echo To stop: stop.bat
echo ========================================
echo.

pause

