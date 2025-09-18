# PowerShell script to reset the database with correct schema
# This script can be run on any computer after git pull

Write-Host "Resetting SecChamp 2025 Database..." -ForegroundColor Green

# Stop and remove existing containers and volumes
Write-Host "Stopping containers..." -ForegroundColor Yellow
docker-compose down -v

# Remove the mysql volume to ensure clean state
Write-Host "Removing mysql data volume..." -ForegroundColor Yellow
docker volume rm sourcecodes_mysql-data -f

# Rebuild and start containers
Write-Host "Rebuilding and starting containers..." -ForegroundColor Yellow
docker-compose up --build -d

Write-Host "Database reset complete!" -ForegroundColor Green
Write-Host "The database now supports multiple book purchases per user." -ForegroundColor Cyan
Write-Host "You can now test the shopping cart functionality." -ForegroundColor Cyan

# Wait for containers to be ready
Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check status
docker-compose ps
