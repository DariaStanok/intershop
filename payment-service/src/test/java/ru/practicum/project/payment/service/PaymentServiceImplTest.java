package ru.practicum.project.payment.service;

import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;
import ru.practicum.project.payment.exception.BadAmountException;
import ru.practicum.project.payment.exception.InsufficientFundsException;

class PaymentServiceImplTest {
	
	private final PaymentServiceImpl service = new PaymentServiceImpl(50_000);

	@Test
	void deposit_ok() {
        StepVerifier.create(service.deposit(1_000, "ILS"))
                .expectNext(51_000L)
                .verifyComplete();
    }

    @Test
    void deposit_badAmount_400() {
        StepVerifier.create(service.deposit(0, "ILS"))
                .expectError(BadAmountException.class)
                .verify();
    }

    @Test
    void withdraw_ok() {
        StepVerifier.create(service.withdraw(10_000, "ILS"))
                .expectNext(40_000L)
                .verifyComplete();
    }

    @Test
    void withdraw_insufficient_422() {
        StepVerifier.create(service.withdraw(100_000, "ILS"))
                .expectError(InsufficientFundsException.class)
                .verify();
    }
}
