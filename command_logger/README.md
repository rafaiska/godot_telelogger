# Play Command Logger

This Django project exposes a RESTful API that records play session metadata plus the player commands issued during that session.

## Getting started

1. Create a virtual environment and install dependencies:
   ```
   python3 -m venv .venv
   source .venv/bin/activate
   pip install -r requirements.txt
   ```
2. Run migrations:
   ```
   python manage.py migrate
   ```
3. Start the development server:
   ```
   python manage.py runserver
   ```

## API overview

- `POST /api/sessions/` accepts `random_seed`, `preferences`, `game_state`, and a list of `commands` (each with `command_type` and `timestamp_ms`).
- `GET /api/commands/?session=<id>` filters commands created for a given session.

Integration tests live in `playlogs/tests.py` and exercise both endpoints.
