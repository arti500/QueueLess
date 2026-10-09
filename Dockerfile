
# Build the React frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Build the Spring Boot backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/ ./backend/
COPY --from=frontend-build /app/frontend/dist ./backend/src/main/resources/static/
WORKDIR /app/backend
RUN mvn -B clean package -DskipTests

# Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
ENV SERVER_PORT=10000
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
