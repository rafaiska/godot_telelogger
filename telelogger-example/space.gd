extends Node2D

@onready var packed_bullet: PackedScene = load("res://bullet.tscn")
@onready var packed_enemy: PackedScene = load("res://enemy.tscn")
@onready var shoot_delay: Timer = $ShootDelay
var disabled_bullets = []
var disabled_enemies = []
var max_speed = 50.0
var score: int = 0
var autofire: bool
var telelogger_agent = null
var session_logger = null
var speed: float = 0.0

const DIFFICULTY_RATE = 3.0

func set_speed(speed_: float):
	speed = speed_

func set_autofire(toggle: bool):
	autofire = toggle

func spawn(enemy_name: String):
	var new_e = null
	if $EnemyPool.has_node(enemy_name):
		new_e = $EnemyPool.get_node(enemy_name)
	else:
		new_e = packed_enemy.instantiate()
		new_e.name = enemy_name
		new_e.game_over.connect(_on_game_over)
		new_e.destroyed.connect(_on_enemy_destroyed)
		$EnemyPool.add_child(new_e)
	new_e.configure(max_speed)

func destroy(enemy_name: String):
	if $EnemyPool.has_node(enemy_name):
		$EnemyPool.get_node(enemy_name).destroy()

func _ready() -> void:
	Engine.time_scale = 0.0
	$NewSessionB.grab_focus()

func _input(event: InputEvent) -> void:
	if session_logger != null and Engine.time_scale > 0.0:
		if event.is_action_pressed('ui_accept'):
			autofire = true
			send_player_command('autofire=on')
		if event.is_action_released('ui_accept'):
			autofire = false
			send_player_command('autofire=off')

func _physics_process(delta: float) -> void:
	if session_logger != null and Engine.time_scale > 0.0:
		var new_speed = Input.get_axis('ui_left', 'ui_right') * 100.0
		if new_speed != speed:
			speed = new_speed
			send_player_command('speed=%.1f' % speed)
	if speed != 0:
		$Player.global_position += Vector2(speed * delta, 0)
		if $Player.global_position.x < 32:
			$Player.global_position.x = 32
		if $Player.global_position.x > 255 - 32:
			$Player.global_position.x = 255 - 32

func send_player_command(command: String):
	if session_logger != null:
		session_logger.send_command('player', command, get_player_state())

func get_player_state() -> Dictionary:
	return {
		'position': [$Player.global_position.x, $Player.global_position.y]
	}

func _process(delta: float) -> void:
	if telelogger_agent != null and Engine.time_scale > 0.0:
		telelogger_agent.process_agent(self, delta)
	max_speed += delta * DIFFICULTY_RATE
	if autofire and shoot_delay.is_stopped():
		fire()
		shoot_delay.start()

func fire():
	var new_b = null
	if disabled_bullets.size() > 0:
		new_b = disabled_bullets.pop_back()
	else:
		new_b = packed_bullet.instantiate()
		new_b.was_disabled.connect(_on_bullet_was_disabled)
		$BulletPool.add_child(new_b)
	new_b.shoot($Player.global_position)

func _on_bullet_was_disabled(bullet: CharacterBody2D) -> void:
	disabled_bullets.append(bullet)

func _on_game_over():
	Engine.time_scale = 0.0

func _on_enemy_destroyed(enemy: Sprite2D):
	if session_logger != null:
		session_logger.send_command(enemy.name, 'destroy', enemy.get_state())
	score += int(enemy.speed)
	$Score.text = '%06d' % score
	disabled_enemies.append(enemy)

func _on_enemy_spawn_timeout() -> void:
	var new_e = null
	if disabled_enemies.size() > 0:
		new_e = disabled_enemies.pop_back()
	else:
		new_e = packed_enemy.instantiate()
		new_e.session_logger = session_logger
		new_e.game_over.connect(_on_game_over)
		new_e.destroyed.connect(_on_enemy_destroyed)
		$EnemyPool.add_child(new_e)
	new_e.configure(max_speed)
	if session_logger != null:
		session_logger.send_command(new_e.name, 'spawn', new_e.get_state())

func _on_new_session_b_pressed() -> void:
	session_logger = Node.new()
	session_logger.set_script(load('res://session_logger.gd'))
	add_child(session_logger)
	session_logger.configure_new_session(randi())
	_game_start()
	$EnemySpawn.start()

func _on_replay_last_b_pressed() -> void:
	telelogger_agent = Node.new()
	telelogger_agent.set_script(load('res://telelogger_agent.gd'))
	add_child(telelogger_agent)
	telelogger_agent.load_last()
	assert(not telelogger_agent.failed_state)
	_game_start()

func _game_start():
	if telelogger_agent != null:
		seed(telelogger_agent.random_seed)
	if session_logger != null:
		seed(session_logger.random_seed)
	$NewSessionB.visible = false
	$ReplayLastB.visible = false
	Engine.time_scale = 1.0
