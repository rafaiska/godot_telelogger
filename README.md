# Godot Telelogger

Spring Boot REST service for collecting logs and telemetry from games made with the Godot Engine. The application is designed to run on Google Cloud Run and persist data in a Neon PostgreSQL database.

## Technology stack

- Java 26 and Spring Boot
- Spring Web MVC, Spring Data JPA and Bean Validation
- PostgreSQL with Flyway migrations
- Maven Wrapper
- Docker / Google Cloud Run

## Running locally

Requirements: JDK 26 and a PostgreSQL database. From `telelogger/`, configure the connection and start the service:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/telelogger
export DATABASE_USERNAME=telelogger
export DATABASE_PASSWORD=telelogger
./mvnw spring-boot:run
```

The application listens on port `8080` by default. Cloud Run can override it through `PORT`. The optional `DATABASE_MAX_POOL_SIZE` variable defaults to `5`, which keeps the connection pool suitable for serverless database deployments.

## API

All session endpoints are rooted at `/api/sessions/`.

- `POST /api/sessions/` creates a play session. `random_seed` is required; `preferences`, `game_state`, and `commands` are optional.
- `GET /api/sessions/` lists sessions, newest first.
- `GET /api/sessions/{id}/` retrieves one session.
- `GET /api/sessions/between/?start={ISO}&end={ISO}` lists sessions in an inclusive date/time range.

Example request:

```json
{
  "random_seed": "seed-123",
  "preferences": {"target": "stealth"},
  "game_state": {"level": 3, "health": 75},
  "commands": [
    {
      "entity_id": "player-1",
      "entity_state": {"position": [0, 0], "hp": 100},
      "command_type": "move_forward",
      "timestamp_ms": 120
    }
  ]
}
```

Nested commands are persisted atomically with the session and are not included in session responses.

## Tests and packaging

Run commands from `telelogger/`:

```bash
./mvnw test
./mvnw verify
./mvnw package
```

Tests use an in-memory H2 database and require no local PostgreSQL instance.

## Cloud Run deployment

Build the container from `telelogger/`:

```bash
docker build -t godot-telelogger .
```

Deploy it with `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` supplied as Cloud Run environment variables or secrets. Neon requires an SSL connection; include `?sslmode=require` in the JDBC URL. Flyway applies the database schema automatically when the application starts.
