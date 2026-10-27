@echo off
TITLE CYPHER - AICTE Regulatory Platform Local Launcher
CLS

echo ===============================================================================
echo            CYPHER - AICTE Regulatory Officer Portal Launcher
echo ===============================================================================
echo.
echo Choose how you would like to run CYPHER on localhost:
echo.
echo  [1] Docker Compose (RECOMMENDED - Full Stack: PostgreSQL + Spring Boot + React)
echo  [2] Instant Standalone Web Portal (Zero setup, runs instantly at http://localhost:8085/)
echo  [3] Run Backend only (Spring Boot / Maven - requires Java 17+ and PostgreSQL)
echo  [4] Run Frontend only (Vite / React - requires Node.js 18+)
echo  [5] Exit
echo.
set /p opt="Enter choice [1-5]: "

if "%opt%"=="1" goto DOCKER
if "%opt%"=="2" goto STANDALONE
if "%opt%"=="3" goto BACKEND
if "%opt%"=="4" goto FRONTEND
if "%opt%"=="5" goto EXIT
goto INVALID

:DOCKER
echo.
echo [*] Starting CYPHER full stack with Docker Compose...
docker-compose down
docker-compose up --build -d
echo.
echo ===============================================================================
echo  CYPHER services started successfully!
echo   - Frontend Portal:  http://localhost:5173
echo   - Backend REST API: http://localhost:8080/api/v1
echo   - Swagger OpenAPI:  http://localhost:8080/swagger-ui.html
echo   - PostgreSQL DB:    localhost:5432 (db: cypher_db, user: postgres)
echo ===============================================================================
echo.
pause
goto EXIT

:STANDALONE
echo.
echo [*] Launching CYPHER standalone server on http://localhost:8085/ ...
powershell -ExecutionPolicy Bypass -File "%~dp0serve.ps1"
goto EXIT

:BACKEND
echo.
echo [*] Launching Spring Boot backend...
cd backend
mvn clean spring-boot:run
cd ..
goto EXIT

:FRONTEND
echo.
echo [*] Launching React Vite frontend...
cd frontend
call npm install
npm run dev
cd ..
goto EXIT

:INVALID
echo Invalid choice. Exiting.
pause
:EXIT
