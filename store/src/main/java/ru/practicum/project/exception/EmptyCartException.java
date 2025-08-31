package ru.practicum.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmptyCartException extends RuntimeException {

	private static final long serialVersionUID = 6065887216661380577L;

}
