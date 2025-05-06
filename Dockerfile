
FROM openjdk:20-jdk-slim AS backend

WORKDIR /app

ADD backend/target/*.jar app.jar
EXPOSE 4000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]






