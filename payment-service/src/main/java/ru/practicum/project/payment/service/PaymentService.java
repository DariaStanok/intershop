package ru.practicum.project.payment.service;

import reactor.core.publisher.Mono;

public interface PaymentService {

	Mono<Long> deposit(long amount, String currency);
    Mono<Long> withdraw(long amount, String currency);
}
