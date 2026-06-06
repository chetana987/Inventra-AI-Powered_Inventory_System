# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="Inventra Team" \
      description="AI-Powered Inventory Management System" \
      version="1.0.0"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    apk add --no-cache curl

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=40s \
    CMD curl -sf http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
