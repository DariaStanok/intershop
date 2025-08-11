package ru.practicum.project.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) 
public class BadAmountException extends RuntimeException {

	private static final long serialVersionUID = -3495065744153842575L;

}
