# Use Maven to build the project with JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Use a lightweight JDK 17 runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Correct JAR file name
COPY --from=build /app/target/sechamp2025-0.0.1-SNAPSHOT.jar app.jar

# Optional: copy static files if needed and present
# COPY src/main/resources/static/ /app/static

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
