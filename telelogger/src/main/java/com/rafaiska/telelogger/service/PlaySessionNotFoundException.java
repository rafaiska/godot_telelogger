package com.rafaiska.telelogger.service;

public class PlaySessionNotFoundException extends RuntimeException {

	public PlaySessionNotFoundException(Long id) {
		super("Play session " + id + " was not found.");
	}
}
