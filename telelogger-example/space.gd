extends Node2D

@onready var packed_bullet: PackedScene = load("res://bullet.tscn")
var disabled_bullets = []

func _input(event: InputEvent) -> void:
	if event.is_action_pressed('ui_accept'):
		fire()

func fire():
	var new_b = null
	if disabled_bullets.size() > 0:
		new_b = disabled_bullets.pop_back()
	else:
		new_b = packed_bullet.instantiate()
		new_b.was_disabled.connect(_on_bullet_was_disabled)
		$BulletPool.add_child(new_b)
	new_b.call_deferred('shoot', $Player.global_position)

func _on_bullet_was_disabled(bullet: CharacterBody2D) -> void:
	disabled_bullets.append(bullet)
