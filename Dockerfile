# syntax=docker/dockerfile:1

# ---- Build stage: compile the Spring Boot fat jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# Resilient downloads: retry on the transient "premature end of content" errors that
# the large firebase-admin dependency tree is prone to. A cached ~/.m2 (BuildKit
# cache mount) makes rebuilds fast without the flaky dependency:go-offline step.
ENV MAVEN_OPTS="-Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true"
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests clean package

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
