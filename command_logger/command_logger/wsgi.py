"""WSGI entry point for the Django project."""
import os

from django.core.wsgi import get_wsgi_application


os.environ.setdefault("DJANGO_SETTINGS_MODULE", "command_logger.settings")

application = get_wsgi_application()
