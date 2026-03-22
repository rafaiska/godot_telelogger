"""Data models for play session logging."""
from django.db import models


class PlaySession(models.Model):
    random_seed = models.CharField(max_length=150)
    preferences = models.JSONField(default=dict, blank=True)
    game_state = models.JSONField(default=dict, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ("-created_at",)

    def __str__(self) -> str:
        return f"Session {self.id} (seed={self.random_seed})"


class EntityCommand(models.Model):
    session = models.ForeignKey(
        PlaySession, related_name="commands", on_delete=models.CASCADE
    )
    entity_id = models.CharField(max_length=150)
    entity_state = models.JSONField(default=dict, blank=True)
    command_type = models.CharField(max_length=100)
    timestamp_ms = models.PositiveIntegerField()
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ("timestamp_ms",)

    def __str__(self) -> str:
        return f"{self.command_type}@{self.timestamp_ms}"
