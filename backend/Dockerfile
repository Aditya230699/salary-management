# Build the Spring Boot application in a reproducible Java 17 environment.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

# Keep the runtime image small; Render supplies PORT, which application.properties reads.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/salary-management-backend-1.0.0.jar app.jar

EXPOSE 8080
# Restrict JVM Heap to fit free tier limits (512MB RAM) and use low-overhead Serial GC for fast container startup
ENTRYPOINT ["java", "-Xmx350m", "-Xss512k", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]
