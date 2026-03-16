"""API views that expose session and command logging."""
from rest_framework import viewsets

from .models import PlaySession, PlayerCommand
from .serializers import PlaySessionSerializer, PlayerCommandSerializer


class PlaySessionViewSet(viewsets.ModelViewSet):
    queryset = PlaySession.objects.all()
    serializer_class = PlaySessionSerializer


class PlayerCommandViewSet(viewsets.ModelViewSet):
    queryset = PlayerCommand.objects.all()
    serializer_class = PlayerCommandSerializer

    def get_queryset(self):
        queryset = super().get_queryset()
        session_id = self.request.query_params.get("session")
        if session_id:
            queryset = queryset.filter(session_id=session_id)
        return queryset
