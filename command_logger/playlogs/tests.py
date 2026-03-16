"""Integration tests that cover the REST logging workflow."""
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase

from .models import PlaySession, PlayerCommand


class PlaylogsIntegrationTests(APITestCase):
    def test_create_session_with_nested_commands(self):
        payload = {
            "random_seed": "seed-123",
            "preferences": {"target": "stealth"},
            "game_state": {"level": 3, "health": 75},
            "commands": [
                {"command_type": "move_forward", "timestamp_ms": 120},
                {"command_type": "jump", "timestamp_ms": 450},
            ],
        }
        url = reverse("playsession-list")
        response = self.client.post(url, payload, format="json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)

        session = PlaySession.objects.get(id=response.data["id"])
        self.assertEqual(session.random_seed, payload["random_seed"])
        self.assertEqual(session.preferences, payload["preferences"])
        self.assertEqual(session.game_state, payload["game_state"])

        recorded = list(session.commands.all())
        self.assertEqual(len(recorded), len(payload["commands"]))
        for command_data, recorded_command in zip(payload["commands"], recorded):
            self.assertEqual(recorded_command.command_type, command_data["command_type"])
            self.assertEqual(recorded_command.timestamp_ms, command_data["timestamp_ms"])

    def test_command_list_can_be_filtered_by_session(self):
        session = PlaySession.objects.create(random_seed="seed-filter")
        PlayerCommand.objects.create(
            session=session,
            command_type="dash",
            timestamp_ms=10,
        )
        PlayerCommand.objects.create(
            session=session,
            command_type="attack",
            timestamp_ms=200,
        )
        other_session = PlaySession.objects.create(random_seed="seed-other")
        PlayerCommand.objects.create(
            session=other_session,
            command_type="idle",
            timestamp_ms=0,
        )

        url = reverse("playercommand-list") + f"?session={session.id}"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
