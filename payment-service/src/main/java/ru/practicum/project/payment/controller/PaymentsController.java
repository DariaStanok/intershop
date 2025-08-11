package ru.practicum.project.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
import ru.practicum.project.payment.api.PaymentsApi;
import ru.practicum.project.payment.model.AmountRequest;
import ru.practicum.project.payment.model.OperationResponse;
import ru.practicum.project.payment.service.PaymentService;

@RestController
public class PaymentsController implements PaymentsApi {

    private final PaymentService service;

    public PaymentsController(PaymentService service) {
        this.service = service;
    }
	@Override
	public Mono<ResponseEntity<OperationResponse>> deposit(@Valid Mono<AmountRequest> amountRequest,
			ServerWebExchange exchange) {
		return amountRequest
	            .flatMap(req -> service.deposit(req.getAmount(), req.getCurrency()))
	            .map(newBal -> new OperationResponse().success(true).newBalance(newBal))
	            .map(ResponseEntity::ok);
	}

	@Override
	public Mono<ResponseEntity<OperationResponse>> withdraw(@Valid Mono<AmountRequest> amountRequest,
			ServerWebExchange exchange) {
		return amountRequest
	            .flatMap(req -> service.withdraw(req.getAmount(), req.getCurrency()))
	            .map(newBal -> new OperationResponse().success(true).newBalance(newBal))
	            .map(ResponseEntity::ok);
	    }

}
