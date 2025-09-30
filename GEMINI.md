# Project Overview

This project is a Spring Boot backend application for a concert reservation system. It leverages a variety of modern technologies to provide a robust, scalable, and event-driven architecture.

**Key Technologies:**
*   **Framework:** Spring Boot (Java)
*   **Build Tool:** Gradle
*   **Database:** MySQL (with Flyway for migrations)
*   **Caching/Distributed Lock:** Redis (with Redisson)
*   **Messaging:** Apache Kafka
*   **API Documentation:** Swagger (SpringDoc OpenAPI)
*   **Asynchronous Processing:** Spring WebFlux, Spring Async
*   **Testing:** JUnit 5, Testcontainers
*   **Monitoring:** Micrometer, Prometheus
*   **Containerization:** Docker, Docker Compose

**Architecture Highlights:**
The application is designed with a focus on event-driven patterns, utilizing Kafka for asynchronous processing of events like payment success/failure and reservation creation. It incorporates distributed locking with Redisson for critical sections, and uses Redis for caching. The `docker-compose.yml` orchestrates the necessary infrastructure components including MySQL, Redis, Zookeeper, and a Kafka cluster.

# Building and Running

## Prerequisites
*   Java 17
*   Docker and Docker Compose

## Build
To build the project, navigate to the root directory and run:
```bash
./gradlew clean build
```

## Run with Docker Compose
The project can be run using Docker Compose, which will set up MySQL, Redis, Zookeeper, and Kafka.
```bash
docker-compose up -d
```
After the infrastructure is up, you can run the Spring Boot application:
```bash
./gradlew bootRun
```
Alternatively, you can build a Docker image for the Spring Boot application and run it as part of the Docker Compose setup (this would require modifying `docker-compose.yml` to include the application service).

## Testing
To run the unit and integration tests:
```bash
./gradlew test
```

# Development Conventions

*   **Language:** Java 17
*   **Build System:** Gradle (Kotlin DSL)
*   **API Documentation:** Uses SpringDoc OpenAPI for Swagger UI.
*   **Database Migrations:** Flyway is used for managing database schema changes.
*   **Distributed Locking:** Redisson is integrated for distributed locking mechanisms.
*   **Event-Driven:** Kafka is used for event-driven communication between services.
*   **Testing:** Extensive use of JUnit 5 and Testcontainers for robust testing.
