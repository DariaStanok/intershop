package ru.practicum.project.payment.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import ru.practicum.project.payment.exception.BadAmountException;
import ru.practicum.project.payment.exception.InsufficientFundsException;
import ru.practicum.project.payment.model.AmountRequest;
import ru.practicum.project.payment.model.OperationResponse;
import ru.practicum.project.payment.service.PaymentService;

@WebFluxTest(PaymentsController.class)
class PaymentsControllerTest {

	@Autowired
	WebTestClient webTestClient;

	@MockBean
	PaymentService paymentService;

	@Test
	void deposit_200() {
        Mockito.when(paymentService.deposit(1_000L, "ILS")).thenReturn(Mono.just(51_000L));

        AmountRequest req = new AmountRequest().amount(1_000L).currency("ILS");

        webTestClient.post().uri("/api/payments/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OperationResponse.class)
                .value(r -> {
                    assert r.getSuccess() == Boolean.TRUE;
                    assert r.getNewBalance() != null && r.getNewBalance() == 51_000L;
                });
    }
	@Test
    void withdraw_422() {
        Mockito.when(paymentService.withdraw(999_999L, "ILS"))
                .thenReturn(Mono.error(new InsufficientFundsException()));

        AmountRequest req = new AmountRequest().amount(999_999L).currency("ILS");

        webTestClient.post().uri("/api/payments/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void deposit_400_badAmount() {
        Mockito.when(paymentService.deposit(0L, "ILS"))
                .thenReturn(Mono.error(new BadAmountException()));

        AmountRequest req = new AmountRequest().amount(0L).currency("ILS");

        webTestClient.post().uri("/api/payments/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();
    }

}
