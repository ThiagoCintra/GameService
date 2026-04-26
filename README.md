GameService
===========

Worker-only microservice that consumes TransactionEvent messages from SQS and applies gamification rules.

Quickstart
----------

1. Build

```bash
cd /path/to/GameService
mvn -DskipTests package
```

2. Start dependencies with docker-compose

```bash
docker compose up -d localstack mongo redis
# wait for localstack to be ready
```

3. Create queues in LocalStack

```bash
./scripts/create_queues.sh
```

4. Run the service

```bash
java -jar target/game-service-0.0.1-SNAPSHOT.jar
```

Or with Docker Compose (build will run maven inside image):

```bash
docker compose build
docker compose up
```

Tests
-----

Unit tests: `mvn test` (some integration tests may be skipped if LocalStack/Mongo not available)

Notes
-----
- The service runs as a worker only (no HTTP endpoints): `spring.main.web-application-type=none`.
- Use `app.worker.enabled=false` to disable SQS polling (useful for running tests).
- Virtual threads are used for per-message processing.
