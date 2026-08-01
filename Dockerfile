# syntax=docker/dockerfile:1

# ---- Build stage: compile the Spring Boot fat jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# Cache dependencies first (only re-downloads when pom.xml changes)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Runtime stage: slim JRE, non-root ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -r app && useradd -r -g app app
# The repackaged (executable) jar; *.jar.original is excluded by the pattern.
COPY --from=build /build/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080
# Config (DB, AWS, Razorpay, FCM path, …) is supplied as environment variables at
# runtime — never baked into the image. See deploy/docker-compose.prod.yml.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
