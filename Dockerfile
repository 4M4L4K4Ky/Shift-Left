# 1. Compilación de Frontend y Backend
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Instalar Node.js en la etapa de build para compilar la UI
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

COPY . .

# Compilar Frontend -> Generar dist
RUN cd landing && npm install && npm run build

# Copiar el dist generado al static de Spring Boot
RUN mkdir -p src/main/resources/static && \
    cp -r landing/dist/* src/main/resources/static/

# Compilar Java JAR
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# 2. Ejecución (Imagen final ligera)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx384m", "-XX:+UseContainerSupport", "-jar", "app.jar"]