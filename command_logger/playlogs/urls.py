"""Router definitions for the playlogs REST API."""
from rest_framework.routers import DefaultRouter

from .views import EntityCommandViewSet, PlaySessionViewSet

router = DefaultRouter()
router.register("sessions", PlaySessionViewSet, basename="playsession")
router.register("commands", EntityCommandViewSet, basename="entitycommand")

urlpatterns = router.urls
