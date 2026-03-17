# Play Command Logger

Play Command Logger is a lightweight Django + Django REST framework service that lets you record each play session together with the sequence of player commands issued during that session. It models a session’s metadata (random seed, preferences, game state) and the list of timestamped commands so you can later analyze what happened within a playthrough.

## Project context

This app lives under the `godot_telelogger/command_logger` subtree; the parent `../README.md` briefly describes the broader telelogger service that aggregates data from Godot game builds, so consult it for high-level goals and links to sibling components.

## Tech stack

- Python 3.10+ (recommended for Django 4.2 support)
- Django 4.2+
- Django REST framework 3.15+
- SQLite (default `db.sqlite3`, no additional configuration required for development)

## Getting started

1. **Create and activate a virtual environment**
   ```bash
   python3 -m venv .venv
   source .venv/bin/activate
   ```
2. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```
3. **Apply migrations**
   ```bash
   python manage.py migrate
   ```
4. **Run the development server**
   ```bash
   python manage.py runserver
   ```

## API reference

All requests are rooted at `/api/`.

### Sessions (`/api/sessions/`)

- `POST /api/sessions/` – create a new play session with optional nested commands. The payload must include `random_seed` and may include `preferences`, `game_state`, and `commands`. Each command needs `command_type` and `timestamp_ms`. Example:
  ```json
  {
    "random_seed": "seed-123",
    "preferences": {"target": "stealth"},
    "game_state": {"level": 3, "health": 75},
    "commands": [
      {"command_type": "move_forward", "timestamp_ms": 120},
      {"command_type": "jump", "timestamp_ms": 450}
    ]
  }
  ```
  The response includes the created session but never echoes back the `commands` list it accepted.

- `GET /api/sessions/` – list recorded sessions (ordered by newest first). You can retrieve a single session with `GET /api/sessions/{id}/`.

- `GET /api/sessions/between/?start={ISO}&end={ISO}` – custom action that returns sessions created between the two ISO 8601 timestamps. Both `start` and `end` are required, and `start` must be same or earlier than `end`.

### Commands (`/api/commands/`)

- `GET /api/commands/?session={id}` – list all commands recorded for a specific session.
- `POST /api/commands/` – create a command manually (session, command_type, timestamp_ms required).

## Testing

Integration tests cover the REST workflow (`playlogs/tests.py`). Run them with:

```bash
python manage.py test
```

## Development notes

- `playlogs.models` defines `PlaySession` and `PlayerCommand` along with sensible default ordering (`created_at` descending for sessions, `timestamp_ms` ascending for commands).
- Serializers (`playlogs/serializers.py`) support nested creation so you can submit a session with all its commands in one request.
- The API router is declared in `playlogs/urls.py` and mounted at `/api/` via `command_logger/urls.py`.

Feel free to seed additional commands manually and extend the serializers or views if you need richer validation or filtering in the future.
