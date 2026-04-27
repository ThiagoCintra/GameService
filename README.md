GameService
===========

Worker microservice that consumes TransactionEvent messages from SQS and applies gamification rules.
Exposes actuator health/metrics endpoints on port **8082**.

Quickstart
----------

1. Build (requires Java 21)

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64   # adjust path as needed
mvn -DskipTests package
```

2. Start dependencies with docker-compose

```bash
docker compose up -d localstack mongo redis
# LocalStack init script auto-creates the SQS queues
```

3. Run the service

```bash
java -jar target/game-service-0.0.1-SNAPSHOT.jar
# Health check: curl http://localhost:8082/actuator/health
```

Or with Docker Compose (build will run maven inside image):

```bash
docker compose build
docker compose up
```

Automated setup
---------------

```bash
bash scripts/setup.sh
```

The setup script auto-detects Java 21, builds the JAR, starts infra, and launches the service.

Health check
------------

```
GET http://localhost:8082/actuator/health
```

Expected response: `{"status":"UP"}`

Tests
-----

Unit tests: `mvn test` (integration tests require LocalStack/Mongo)

Notes
-----
- The SQS queues (`transactions`, `transactions-dlq`) are created automatically when LocalStack starts
  via `scripts/localstack-init.sh`.
- Virtual threads are used for per-message processing.
- Use `app.worker.enabled=false` to disable SQS polling (useful for running tests).
