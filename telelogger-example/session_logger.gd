extends Node

const API_BASE = "http://localhost:8000/api"
const SESSION_ENDPOINT = "%s/sessions/" % API_BASE
const COMMAND_ENDPOINT = "%s/commands/" % API_BASE

var random_seed: int
var preferences := {}
var game_state := {}
var session_id: int = 0
var session_start_ms: int = 0
var fail_state: bool

var _pending_commands: Array[Dictionary] = []
var _command_in_flight := false
var _active_command := {}

var _session_request: HTTPRequest
var _command_request: HTTPRequest

func _ready() -> void:
	_session_request = HTTPRequest.new()
	add_child(_session_request)
	_session_request.connect("request_completed", Callable(self, "_on_session_request_completed"))

	_command_request = HTTPRequest.new()
	add_child(_command_request)
	_command_request.connect("request_completed", Callable(self, "_on_command_request_completed"))

func configure_new_session(s_seed: int, new_preferences := {}, new_game_state := {}) -> void:
	fail_state = false
	random_seed = s_seed
	preferences = new_preferences.duplicate(true)
	game_state = new_game_state.duplicate(true)
	session_start_ms = Time.get_ticks_msec()
	session_id = 0
	_pending_commands.clear()
	_command_in_flight = false
	_active_command = {}
	_command_request.cancel_request()

	var payload := {
		"random_seed": random_seed,
		"preferences": preferences,
		"game_state": game_state,
		"commands": []
	}
	_session_request.request(SESSION_ENDPOINT, ['Content-Type: application/json'], HTTPClient.METHOD_POST, JSON.stringify(payload))

func send_command(entity_id: String, command_type: String, entity_state := {}) -> void:
	if fail_state:
		return
	_pending_commands.append(_build_command_payload(entity_id, command_type, entity_state))
	_flush_command_queue()

func _build_command_payload(entity_id: String, command_type: String, entity_state: Dictionary) -> Dictionary:
	return {
		"session": session_id,
		"entity_id": entity_id,
		"entity_state": entity_state,
		"command_type": command_type,
		"timestamp_ms": Time.get_ticks_msec() - session_start_ms
	}

func _flush_command_queue() -> void:
	if fail_state or session_id == 0 or _command_in_flight or _pending_commands.is_empty():
		return
	if session_id == 0 or session_id == null:
		push_error('Session was not initialized')
		return

	_active_command = _pending_commands[0]
	_pending_commands.remove_at(0)
	_active_command["session"] = session_id

	var error := _command_request.request(
		COMMAND_ENDPOINT,
		['Content-Type: application/json'],
		HTTPClient.METHOD_POST,
		JSON.stringify(_active_command)
	)
	if error != OK:
		_pending_commands.insert(0, _active_command)
		_active_command = {}
		push_warning("command post request failed to start (%d)" % error)
		return
	_command_in_flight = true

func _on_session_request_completed(_result: int, response_code: int, _headers: Array, body: PackedByteArray) -> void:
	if response_code != 201:
		push_error("failed to create session: %s" % response_code)
		fail_state = true
		return
	var decoded =  JSON.parse_string(body.get_string_from_utf8())
	if typeof(decoded) == TYPE_DICTIONARY and decoded.has("id"):
		session_id = int(decoded["id"])
		_flush_command_queue()
	else:
		push_error("invalid session response: %s" % decoded)

func _on_command_request_completed(_result: int, response_code: int, _headers: Array, body: PackedByteArray) -> void:
	_command_in_flight = false
	if response_code not in [200, 201]:
		push_warning("command post failed (%d) %s" % [response_code, body.get_string_from_utf8()])
	_active_command = {}
	_flush_command_queue()
