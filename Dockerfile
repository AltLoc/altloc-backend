# === Stage 1: Build ===
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom.xml and download dependencies (to use Docker layer caching)
COPY backend/pom.xml backend/
RUN mvn -f backend/pom.xml dependency:go-offline

# Copy the entire source code
COPY backend/ backend/

# Build the JAR file, skipping tests
RUN mvn -f backend/pom.xml clean package -DskipTests

# === Stage 2: Run ===
FROM openjdk:21 AS altloc-backend
WORKDIR /app

# Copy the JAR file from the previous build stage
COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 4000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
