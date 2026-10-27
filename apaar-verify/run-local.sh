#!/bin/bash
# ==============================================================================
# CYPHER - AICTE Regulatory Platform Local Launcher (Linux / macOS / WSL)
# ==============================================================================

clear
echo "==============================================================================="
echo "           CYPHER - AICTE Regulatory Officer Portal Launcher"
echo "==============================================================================="
echo ""
echo "Choose how you would like to run CYPHER on localhost:"
echo ""
echo " [1] Docker Compose (RECOMMENDED - Full Stack: PostgreSQL + Spring Boot + React)"
echo " [2] Run Backend only (Spring Boot / Maven - requires Java 17+ and PostgreSQL)"
echo " [3] Run Frontend only (Vite / React - requires Node.js 18+)"
echo " [4] Exit"
echo ""
read -p "Enter choice [1-4]: " opt

case $opt in
  1)
    echo ""
    echo "[*] Launching CYPHER full stack with Docker Compose..."
    docker-compose down
    docker-compose up --build -d
    echo ""
    echo "==============================================================================="
    echo " CYPHER services are live!"
    echo "  - Frontend Portal:  http://localhost:5173"
    echo "  - Backend REST API: http://localhost:8080/api/v1"
    echo "  - Swagger OpenAPI:  http://localhost:8080/swagger-ui.html"
    echo "  - PostgreSQL DB:    localhost:5432 (db: cypher_db, user: postgres)"
    echo "==============================================================================="
    ;;
  2)
    echo ""
    echo "[*] Launching Spring Boot backend..."
    cd backend && mvn clean spring-boot:run
    ;;
  3)
    echo ""
    echo "[*] Launching React Vite frontend..."
    cd frontend && npm install && npm run dev
    ;;
  4)
    exit 0
    ;;
  *)
    echo "Invalid option."
    exit 1
    ;;
esac
