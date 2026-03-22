"""Serialization definitions for the playlogs API."""
from rest_framework import serializers

from .models import EntityCommand, PlaySession


class SessionCommandCreateSerializer(serializers.ModelSerializer):
    """Used for nested command creation inside session payloads."""

    class Meta:
        model = EntityCommand
        fields = ["entity_id", "entity_state", "command_type", "timestamp_ms"]


class EntityCommandSerializer(serializers.ModelSerializer):
    class Meta:
        model = EntityCommand
        fields = [
            "id",
            "session",
            "entity_id",
            "entity_state",
            "command_type",
            "timestamp_ms",
            "created_at",
        ]
        read_only_fields = ["id", "created_at"]


class PlaySessionSerializer(serializers.ModelSerializer):
    commands = SessionCommandCreateSerializer(many=True, required=False, write_only=True)

    class Meta:
        model = PlaySession
        fields = [
            "id",
            "random_seed",
            "preferences",
            "game_state",
            "commands",
            "created_at",
        ]
        read_only_fields = ["id", "created_at"]

    def create(self, validated_data: dict) -> PlaySession:
        commands_data = validated_data.pop("commands", [])
        session = PlaySession.objects.create(**validated_data)
        if commands_data:
            EntityCommand.objects.bulk_create(
                [EntityCommand(session=session, **command_data) for command_data in commands_data]
            )
        return session
