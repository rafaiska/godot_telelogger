extends CharacterBody2D

var disabled: bool
@onready var shape: CollisionShape2D = $CollisionShape2D
signal was_disabled(bullet: CharacterBody2D)

func _physics_process(_delta: float) -> void:
	if not disabled:
		move_and_slide()
		if global_position.y < 0:
			disable()

func disable():
	disabled = true
	visible = false 
	velocity = Vector2.ZERO
	global_position += Vector2(0, 160)
	was_disabled.emit(self)

func shoot(position_: Vector2):
	disabled = false
	global_position = position_ + Vector2(-1, -1)
	visible = true
	velocity = Vector2(0, -100.0)
