#!/bin/bash

# SecChamp2025 Database Reset Script (Linux/macOS)
# This script resets the database to ensure consistent schema across all environments
# Run this after git pull to synchronize database changes

echo "🔄 Starting database reset process..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose or docker compose is available
if command -v docker-compose > /dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
elif docker compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
else
    echo "❌ Error: Neither docker-compose nor 'docker compose' is available."
    exit 1
fi

echo "📍 Using compose command: $COMPOSE_CMD"

# Stop and remove all containers
echo "🛑 Stopping all containers..."
$COMPOSE_CMD down

# Remove volumes to ensure clean database
echo "🗑️  Removing database volumes..."
$COMPOSE_CMD down -v

# Remove any orphaned containers
echo "🧹 Cleaning up orphaned containers..."
docker container prune -f

# Remove unused volumes
echo "💾 Removing unused volumes..."
docker volume prune -f

# Rebuild and start containers
echo "🔨 Rebuilding containers with updated schema..."
$COMPOSE_CMD up --build -d

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 10

# Check if MySQL is ready
echo "🔍 Checking MySQL connection..."
MAX_ATTEMPTS=30
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if docker exec mysql-db mysql -u root -pP@ssw0rd -e "SELECT 1;" > /dev/null 2>&1; then
        echo "✅ MySQL is ready!"
        break
    fi
    
    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo "❌ MySQL failed to start after $MAX_ATTEMPTS attempts"
        echo "📋 Container logs:"
        docker logs mysql-db --tail 20
        exit 1
    fi
    
    echo "⏳ Waiting for MySQL... (attempt $ATTEMPT/$MAX_ATTEMPTS)"
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

# Check if Spring Boot app is ready
echo "🔍 Checking Spring Boot application..."
MAX_ATTEMPTS=30
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if docker logs sechamp2025_app 2>&1 | grep -q "Started SecChampApplication"; then
        echo "✅ Spring Boot application is ready!"
        break
    fi
    
    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo "❌ Spring Boot application failed to start after $MAX_ATTEMPTS attempts"
        echo "📋 Application logs:"
        docker logs sechamp2025_app --tail 20
        exit 1
    fi
    
    echo "⏳ Waiting for Spring Boot application... (attempt $ATTEMPT/$MAX_ATTEMPTS)"
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

# Verify database schema
echo "🔍 Verifying database schema..."
if docker exec mysql-db mysql -u root -pP@ssw0rd -e "USE secchamp2025; DESCRIBE user_books;" > /dev/null 2>&1; then
    echo "✅ Database schema verified successfully!"
else
    echo "❌ Database schema verification failed"
    exit 1
fi

echo ""
echo "🎉 Database reset completed successfully!"
echo "🌐 Application is available at: http://localhost:8080"
echo "📊 Database is available at: localhost:3306"
echo ""
echo "🛒 Shopping cart features:"
echo "   • Multiple book purchases supported"
echo "   • Race condition vulnerabilities preserved"
echo "   • Cart and checkout system fully functional"
echo ""
echo "✨ Your development environment is ready!"
