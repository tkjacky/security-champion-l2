#!/bin/bash

echo "🎯 Running Race Condition Test..."
echo "=================================="

# Navigate to the Spring Boot project directory
cd "$(dirname "$0")"

# Run the race condition test
echo "Starting race condition demonstration..."

./mvnw spring-boot:run -Dspring-boot.run.main-class=com.cybersecurity.sechamp2025.test.RaceConditionTestMain

echo ""
echo "✅ Race condition test completed!"
