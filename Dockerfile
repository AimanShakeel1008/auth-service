###############################
# 1️⃣ Build Stage
###############################
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy Maven wrapper + settings
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies (cached layer)
RUN ./mvnw -q dependency:go-offline

# Copy source
COPY src ./src

# Build the application
RUN ./mvnw -q clean package -DskipTests

###############################
# 2️⃣ Runtime Stage
###############################
FROM eclipse-temurin:17-jre-alpine

# Basic OS hardening
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat jar
COPY --from=builder /app/target/auth-service-0.1.0.jar app.jar

# Use non-root user
USER appuser

# Expose the application port
EXPOSE 8081

# Healthcheck (Spring Boot actuator)
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# Start the Spring Boot app
ENTRYPOINT ["java", "-Dspring.profiles.active=local", "-jar", "/app/app.jar"]
