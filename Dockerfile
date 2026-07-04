# ── Stage 1: Build ─────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ───────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# instalar wget para healthcheck
RUN apk add --no-cache wget

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep UP || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]