# Multi-stage build for GXT Backend monolith
FROM gradle:8.12.1-jdk21 AS build
WORKDIR /app
COPY gradle gradle
COPY gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradlew ./
COPY modules modules
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/gxt-backend.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
