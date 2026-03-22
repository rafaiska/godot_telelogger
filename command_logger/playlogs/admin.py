"""Django admin registration for the playlogs models."""
from django.contrib import admin

from .models import EntityCommand, PlaySession


@admin.register(PlaySession)
class PlaySessionAdmin(admin.ModelAdmin):
    list_display = ("id", "random_seed", "created_at")
    readonly_fields = ("created_at",)


@admin.register(EntityCommand)
class EntityCommandAdmin(admin.ModelAdmin):
    list_display = ("entity_id", "command_type", "timestamp_ms", "session", "created_at")
    list_filter = ("command_type",)
    readonly_fields = ("created_at",)
