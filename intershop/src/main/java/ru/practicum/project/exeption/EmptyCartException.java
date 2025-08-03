package ru.practicum.project.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmptyCartException extends RuntimeException {

	private static final long serialVersionUID = 6065887216661380577L;

}
