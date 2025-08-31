package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.store.payment.client.api.PaymentsApi;
import ru.practicum.project.store.payment.client.model.AmountRequest;
import ru.practicum.project.store.payment.client.model.OperationResponse;

@ExtendWith(MockitoExtension.class)
class PaymentClientTest {

    @Mock private PaymentsApi paymentsApi;
    
    @InjectMocks
    private PaymentClient client;

    @Test
    void withdraw_returnsTrueOnSuccess() {
        OperationResponse resp = new OperationResponse();
        resp.setSuccess(true);

        when(paymentsApi.withdraw(org.mockito.ArgumentMatchers.any(AmountRequest.class)))
            .thenReturn(Mono.just(resp));

        StepVerifier.create(client.withdraw(1234L, "ILS"))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void withdraw_returnsFalseOnFailure() {
        OperationResponse resp = new OperationResponse();
        resp.setSuccess(false);

        when(paymentsApi.withdraw(org.mockito.ArgumentMatchers.any(AmountRequest.class)))
            .thenReturn(Mono.just(resp));

        StepVerifier.create(client.withdraw(999L, "ILS"))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void deposit_returnsNewBalance() {
        OperationResponse resp = new OperationResponse();
        resp.setNewBalance(42L);

        when(paymentsApi.deposit(org.mockito.ArgumentMatchers.any(AmountRequest.class)))
            .thenReturn(Mono.just(resp));

        StepVerifier.create(client.deposit(100L, "ILS"))
            .assertNext(balance -> assertThat(balance).isEqualTo(42L))
            .verifyComplete();
    }
}
