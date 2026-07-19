package com.rafaiska.telelogger.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.rafaiska.telelogger.service.PlaySessionNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(PlaySessionNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(PlaySessionNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(InvalidDateRangeException.class)
	public ResponseEntity<Map<String, String>> handleInvalidRange(InvalidDateRangeException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, String>> handleMissingParameter() {
		return error(HttpStatus.BAD_REQUEST, "start and end query parameters are required.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, String>> handleInvalidParameter() {
		return error(HttpStatus.BAD_REQUEST, "start and end must be valid ISO 8601 timestamps.");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
		String detail = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.orElse("Request body is invalid.");
		return error(HttpStatus.BAD_REQUEST, detail);
	}

	private ResponseEntity<Map<String, String>> error(HttpStatus status, String detail) {
		return ResponseEntity.status(status).body(Map.of("detail", detail));
	}
}
