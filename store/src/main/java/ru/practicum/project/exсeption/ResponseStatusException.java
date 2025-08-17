package ru.practicum.project.exсeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResponseStatusException extends RuntimeException{
	private static final long serialVersionUID = 4928124039863166121L;

}
