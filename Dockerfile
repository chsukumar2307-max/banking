# ---------- Stage 1 : Build the application ----------
FROM gradle:8.7-jdk17 AS builder

WORKDIR /app

# Copy only Gradle files first (better caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the jar
RUN gradle clean build -x test --no-daemon

# ---------- Stage 2 : Run the application ----------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java","-jar","app.jar"]