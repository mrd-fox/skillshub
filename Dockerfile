# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copie du code source
COPY . .

# Build du projet (skip tests pour accélérer)
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copie du jar généré
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
