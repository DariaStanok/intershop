package ru.practicum.project.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// <-- важно
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
import ru.practicum.project.payment.api.PaymentsApi;
import ru.practicum.project.payment.model.AmountRequest;
import ru.practicum.project.payment.model.OperationResponse;
import ru.practicum.project.payment.service.PaymentService;

@RestController
@RequestMapping("/payments") // <--- общий префикс
public class PaymentsController implements PaymentsApi {

    private static final String USER_HEADER = "X-User";
    private final PaymentService service;

    public PaymentsController(PaymentService service) {
        this.service = service;
    }

    @Override
    @PostMapping(
        path = "/deposit",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<OperationResponse>> deposit(@Valid @RequestBody Mono<AmountRequest> amountRequest,
                                                           ServerWebExchange exchange) {
        String username = requireUser(exchange);
        return amountRequest
            .flatMap(req -> service.deposit(req.getAmount(), req.getCurrency(), username))
            .map(newBal -> new OperationResponse().success(true).newBalance(newBal))
            .map(ResponseEntity::ok);
    }

    @Override
    @PostMapping(
        path = "/withdraw",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<OperationResponse>> withdraw(@Valid @RequestBody Mono<AmountRequest> amountRequest,
                                                            ServerWebExchange exchange) {
        String username = requireUser(exchange);
        return amountRequest
            .flatMap(req -> service.withdraw(req.getAmount(), req.getCurrency(), username))
            .map(newBal -> new OperationResponse().success(true).newBalance(newBal))
            .map(ResponseEntity::ok);
    }

    private String requireUser(ServerWebExchange exchange) {
        String user = exchange.getRequest().getHeaders().getFirst(USER_HEADER);
        if (user == null || user.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-User header");
        }
        return user;
    }
}