"""Router definitions for the playlogs REST API."""
from rest_framework.routers import DefaultRouter

from .views import PlaySessionViewSet, PlayerCommandViewSet

router = DefaultRouter()
router.register("sessions", PlaySessionViewSet, basename="playsession")
router.register("commands", PlayerCommandViewSet, basename="playercommand")

urlpatterns = router.urls
