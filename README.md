# Godot Telelogger

Django REST API for logging and telemetry, to be used by games made with Godot Engine.

# Relevant files and directories

## "Godot Telelogger.yml"

Export file to be used with [Bruno, The Git-native API client](https://www.usebruno.com/).

## command_logger

The Telelogger itself: a Django-based REST API for handling game data

## telelogger-example

Example Godot project that uses the API to log sessions and commands

## telelogger-example/session_logger.gd

GDScript file to handle communications with a running instance of the Telelogger