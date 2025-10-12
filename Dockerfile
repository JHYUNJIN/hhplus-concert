# Stage 1: Build the application
FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
COPY user-service user-service
RUN ./gradlew bootJar

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]