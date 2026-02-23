# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy source
COPY . .

# Ensure Maven Wrapper is executable in Linux (GitHub Actions runner)
RUN chmod +x mvnw

# Build project (skip tests for faster image build; tests run in CI job already)
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy generated jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
