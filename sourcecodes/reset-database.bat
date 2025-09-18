@echo off
echo Resetting SecChamp 2025 Database...

echo Stopping containers...
docker-compose down -v

echo Removing mysql data volume...
docker volume rm sourcecodes_mysql-data

echo Rebuilding and starting containers...
docker-compose up --build -d

echo Database reset complete!
echo The database now supports multiple book purchases per user.
echo You can now test the shopping cart functionality.

echo Waiting for services to be ready...
timeout /t 30 /nobreak > nul

echo Checking container status...
docker-compose ps

pause
