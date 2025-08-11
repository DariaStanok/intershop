package ru.practicum.project.payment.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import ru.practicum.project.payment.exception.BadAmountException;
import ru.practicum.project.payment.exception.InsufficientFundsException;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final AtomicLong balance;

	public PaymentServiceImpl(@Value("${payments.initial-balance:50000}") long initialBalance) {
		this.balance = new AtomicLong(initialBalance);
	}

	@Override
	public Mono<Long> deposit(long amount, String currency) {
		if (amount <= 0) {
			return Mono.error(new BadAmountException());
		}
		return Mono.fromSupplier(() -> balance.addAndGet(amount));
	}

	@Override
	public Mono<Long> withdraw(long amount, String currency) {
		if (amount <= 0) {
			return Mono.error(new BadAmountException());
		}
		return Mono.defer(() -> {
			long current = balance.get();
			if (current < amount) {
				return Mono.error(new InsufficientFundsException());
			}
			long next = balance.addAndGet(-amount);
			return Mono.just(next);
		});
	}
}
