extends Sprite2D

var speed: float
var session_logger

signal game_over
signal destroyed(enemy: Sprite2D)

func _ready() -> void:
	configure(500.0)

func get_state() -> Dictionary:
	var r := {}
	r['position'] = [global_position.x, global_position.y]
	r['modulate'] = [self_modulate.r8, self_modulate.g8, self_modulate.b8]
	r['speed'] = speed
	r['flip'] = flip_h
	return r

func configure(max_speed: float):
	global_position = Vector2(16 + randf() * (255 - 32), 32)
	visible = true
	self_modulate.r8 = randi() % 256
	speed = self_modulate.r * max_speed
	self_modulate.g8 = 32 + randi() % 96
	self_modulate.b8 = 32 + randi() % 96

func _on_area_2d_body_entered(body: Node2D) -> void:
	if not visible:
		return
	if body.is_in_group('bullet') and not body.disabled:
		body.disable()
		if session_logger != null:
			destroy()

func destroy():
	visible = false
	destroyed.emit(self)

func _physics_process(delta: float) -> void:
	if not visible:
		return
	var direction = 1 if flip_h else -1
	global_position += Vector2(direction * speed * delta, 0)
	if global_position.x <= 16 or global_position.x >= 255 - 16:
		flip_h = not flip_h
		global_position += Vector2(0, 16)
		if session_logger != null:
			session_logger.send_command(name, 'sync', get_state())
		if global_position.y >= 144 - 16:
			game_over.emit()
