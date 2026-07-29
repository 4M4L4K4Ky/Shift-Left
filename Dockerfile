# 1. Compilación
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# 2. Ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]