"""API views that expose session and command logging."""
from django.utils.dateparse import parse_datetime
from rest_framework import status, viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from .models import PlaySession, PlayerCommand
from .serializers import PlaySessionSerializer, PlayerCommandSerializer


class PlaySessionViewSet(viewsets.ModelViewSet):
    queryset = PlaySession.objects.all()
    serializer_class = PlaySessionSerializer

    @action(detail=False, methods=["get"], url_path="between")
    def between(self, request):
        """Return every session created between `start` and `end` ISO 8601 timestamps."""
        start_param = request.query_params.get("start")
        end_param = request.query_params.get("end")
        if not start_param or not end_param:
            return Response(
                {"detail": "start and end query parameters are required."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        start_dt = parse_datetime(start_param)
        end_dt = parse_datetime(end_param)
        if not start_dt or not end_dt:
            return Response(
                {"detail": "start and end must be valid ISO 8601 timestamps."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        if start_dt > end_dt:
            return Response(
                {"detail": "start must be the same as or earlier than end."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        queryset = (
            self.get_queryset()
            .filter(created_at__gte=start_dt, created_at__lte=end_dt)
        )
        serializer = self.get_serializer(queryset, many=True)
        return Response(serializer.data)


class PlayerCommandViewSet(viewsets.ModelViewSet):
    queryset = PlayerCommand.objects.all()
    serializer_class = PlayerCommandSerializer

    def get_queryset(self):
        queryset = super().get_queryset()
        session_id = self.request.query_params.get("session")
        if session_id:
            queryset = queryset.filter(session_id=session_id)
        return queryset
