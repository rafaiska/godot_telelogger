"""Django admin registration for the playlogs models."""
from django.contrib import admin

from .models import PlaySession, PlayerCommand


@admin.register(PlaySession)
class PlaySessionAdmin(admin.ModelAdmin):
    list_display = ("id", "random_seed", "created_at")
    readonly_fields = ("created_at",)


@admin.register(PlayerCommand)
class PlayerCommandAdmin(admin.ModelAdmin):
    list_display = ("command_type", "timestamp_ms", "session", "created_at")
    list_filter = ("command_type",)
    readonly_fields = ("created_at",)
