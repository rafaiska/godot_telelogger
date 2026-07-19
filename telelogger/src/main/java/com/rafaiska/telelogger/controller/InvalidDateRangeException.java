package com.rafaiska.telelogger.controller;

public class InvalidDateRangeException extends RuntimeException {

	public InvalidDateRangeException() {
		super("start must be the same as or earlier than end.");
	}
}
