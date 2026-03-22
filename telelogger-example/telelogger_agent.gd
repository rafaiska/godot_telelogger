extends Node

const API_BASE = "http://localhost:8000/api"
const SESSION_ENDPOINT = "%s/sessions/" % API_BASE
const COMMAND_ENDPOINT = "%s/commands" % API_BASE

var _session_request: HTTPRequest
var _command_request: HTTPRequest
var failed_state: bool = false
var last_session_id = null
var commands: Array
var current_timestamp: float = 0.0
var random_seed: int

func process_agent(space: Node2D, delta: float):
	if commands.is_empty():
		return
	current_timestamp += delta
	if get_ms() >= commands[-1]['timestamp_ms']:
		var c = commands.pop_back()
		sync_entity(space, c)
		if c.has('function'):
			space.callv(c['function'], c['args'])

func sync_entity(space: Node2D, command: Dictionary):
	if command['entity_id'] == 'player':
		space.get_node('Player').global_position = Vector2(command['sync_position'][0], command['sync_position'][1])
	else:
		var pool = space.get_node('EnemyPool')
		if not pool.has_node(command['entity_id']):
			push_error('Enemy not found: ' + command['entity_id'])
			return
		var enemy = pool.get_node(command['entity_id'])
		enemy.global_position = Vector2(command['sync_position'][0], command['sync_position'][1])
		enemy.self_modulate = Color(command['sync_modulate'][0], command['sync_modulate'][1], command['sync_modulate'][2])
		enemy.speed = command['sync_speed']
		enemy.flip_h = command['sync_flip']

func get_ms():
	return int(current_timestamp * 1000.0)

func _ready() -> void:
	_session_request = HTTPRequest.new()
	add_child(_session_request)
	_session_request.connect("request_completed", Callable(self, "_on_session_request_completed"))
	
	_command_request = HTTPRequest.new()
	add_child(_command_request)
	_command_request.connect("request_completed", Callable(self, "_on_command_request_completed"))

func _on_session_request_completed(_result: int, response_code: int, _headers: Array, body: PackedByteArray) -> void:
	if response_code != 200:
		failed_state = true
		return
	var data: Array = JSON.parse_string(body.get_string_from_utf8())
	if data.is_empty():
		failed_state = true
		return
	var ids = []
	var seeds = {}
	for d in data:
		ids.append(int(d['id']))
		seeds[int(d['id'])] = int(d['random_seed'])
	last_session_id = ids.max()
	random_seed = seeds[last_session_id]

func _on_command_request_completed(_result: int, response_code: int, _headers: Array, body: PackedByteArray) -> void:
	if response_code != 200:
		failed_state = true
		return
	var data: Array = JSON.parse_string(body.get_string_from_utf8())
	if data.is_empty():
		failed_state = true
		return
	data.reverse()
	for d in data:
		commands.append(build_command(d))

func build_command(command_data: Dictionary):
	var command = {'timestamp_ms': int(command_data['timestamp_ms']), 'entity_id': command_data['entity_id']}
	if command_data['entity_id'] == 'player':
		var command_type_l = command_data['command_type'].split('=')
		if command_type_l[0] == 'speed':
			command['function'] = 'set_speed'
			command['args'] = [float(command_type_l[1])]
		elif command_type_l[0] == 'autofire':
			command['function'] = 'set_autofire'
			command['args'] = [command_type_l[1] == 'on']
	else:
		if command_data['command_type'] == 'spawn':
			command['function'] = 'spawn'
			command['args'] = [command_data['entity_id']]
		elif command_data['command_type'] == 'destroy':
			command['function'] = 'destroy'
			command['args'] = [command_data['entity_id']]
		command['sync_speed'] = command_data['entity_state']['speed']
		command['sync_modulate'] = command_data['entity_state']['modulate']
		command['sync_flip'] = command_data['entity_state']['flip']
	command['sync_position'] = command_data['entity_state']['position']
	return command

func load_last():
	var current_time = Time.get_datetime_dict_from_system()
	var url = '%sbetween?start=%d-%02d-%02d&end=%d-%02d-%02d' % [SESSION_ENDPOINT, current_time['year'], current_time['month'], current_time['day'], current_time['year'], current_time['month'], current_time['day'] + 1]
	_session_request.request(url, [], HTTPClient.METHOD_GET, '')
	await _session_request.request_completed
	url = '%s?session=%d' % [COMMAND_ENDPOINT, last_session_id]
	_command_request.request(url, [], HTTPClient.METHOD_GET, '')
	await _command_request.request_completed
