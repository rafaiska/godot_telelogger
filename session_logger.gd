extends Node

const API_BASE = "http://localhost:8000/api"
const SESSION_ENDPOINT = "%s/sessions/" % API_BASE
const COMMAND_ENDPOINT = "%s/commands/" % API_BASE

var random_seed: String = ""
var preferences := {}
var game_state := {}
var session_id: int = 0
var session_start_ms: int = 0

var _pending_commands := []

var _session_request: HTTPRequest
var _command_request: HTTPRequest
var _rng := RandomNumberGenerator.new()

func _ready() -> void:
	_session_request = HTTPRequest.new()
	add_child(_session_request)
	_session_request.connect("request_completed", Callable(self, "_on_session_request_completed"))

	_command_request = HTTPRequest.new()
	add_child(_command_request)
	_command_request.connect("request_completed", Callable(self, "_on_command_request_completed"))

func configure_new_session(s_seed: String = "", new_preferences := {}, new_game_state := {}) -> void:
	_rng.randomize()
	random_seed = s_seed if s_seed != "" else str(_rng.randi())
	preferences = new_preferences.duplicate(true)
	game_state = new_game_state.duplicate(true)
	session_start_ms = Time.get_ticks_msec()
	session_id = 0
	_pending_commands.clear()

	var payload := {
		"random_seed": random_seed,
		"preferences": preferences,
		"game_state": game_state,
		"commands": []
	}
	_session_request.request(SESSION_ENDPOINT, ['Content-Type: application/json'], HTTPClient.METHOD_POST, JSON.stringify(payload))

func send_command(command_type: String) -> void:
	if session_id == 0:
		_pending_commands.append(command_type)
		return
	_post_command(command_type)

func _post_command(command_type: String) -> void:
	var timestamp_ms = Time.get_ticks_msec() - session_start_ms
	var payload := {
		"session": session_id,
		"command_type": command_type,
		"timestamp_ms": timestamp_ms
	}
	_command_request.request(COMMAND_ENDPOINT, ['Content-Type: application/json'], HTTPClient.METHOD_POST,  JSON.stringify(payload))

func _on_session_request_completed(_result: int, response_code: int, _headers: Array, body: PackedByteArray) -> void:
	if response_code != 201:
		push_error("failed to create session: %s" % response_code)
		return
	var decoded =  JSON.parse_string(body.get_string_from_utf8())
	if typeof(decoded) == TYPE_DICTIONARY and decoded.has("id"):
		session_id = int(decoded["id"])
		session_start_ms = Time.get_ticks_msec()
		for command in _pending_commands:
			_post_command(command)
		_pending_commands.clear()
	else:
		push_error("invalid session response: %s" % decoded)

func _on_command_request_completed(_result: int, response_code: int, _headers: Array, body: PackedByteArray) -> void:
	if response_code not in [200, 201]:
		push_warning("command post failed (%d) %s" % [response_code, body.get_string_from_utf8()])
