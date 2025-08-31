package ru.practicum.project.payment.controller;



import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import ru.practicum.project.payment.exception.InsufficientFundsException;
import ru.practicum.project.payment.model.AmountRequest;
import ru.practicum.project.payment.service.PaymentService;

class PaymentsControllerTest {

  private static final String USER = "alice";
  private static final String HDR_USER = "X-User";
  private static final String PATH_DEPOSIT  = "/payments/deposit";
  private static final String PATH_WITHDRAW = "/payments/withdraw";

  private PaymentService service;
  private WebTestClient client;

  @BeforeEach
  void setUp() {
    service = mock(PaymentService.class);
    client = WebTestClient.bindToController(new PaymentsController(service)).build();
  }

  @Test
  void deposit_ok_returnsOperationResponse() {
    AmountRequest req = new AmountRequest().amount(200L).currency("ILS");
    when(service.deposit(200L, "ILS", USER)).thenReturn(Mono.just(1_200L));
    client.post()
        .uri(PATH_DEPOSIT)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HDR_USER, USER)
        .bodyValue(req)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectBody()
          .jsonPath("$.success").isEqualTo(true)
          .jsonPath("$.newBalance").isEqualTo(1200);

    verify(service).deposit(eq(200L), eq("ILS"), eq(USER));
  }

  @Test
  void withdraw_ok_returnsOperationResponse() {
    AmountRequest req = new AmountRequest().amount(300L).currency("ILS");
    when(service.withdraw(300L, "ILS", USER)).thenReturn(Mono.just(700L));
    client.post()
        .uri(PATH_WITHDRAW)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HDR_USER, USER)
        .bodyValue(req)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectBody()
          .jsonPath("$.success").isEqualTo(true)
          .jsonPath("$.newBalance").isEqualTo(700);

    verify(service).withdraw(eq(300L), eq("ILS"), eq(USER));
  }

  @Test
  void missingUserHeader_returns400() {
    AmountRequest req = new AmountRequest().amount(10L).currency("ILS");
    client.post()
        .uri(PATH_DEPOSIT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .exchange()
        .expectStatus().isBadRequest(); 
  }

  @Test
  void withdraw_insufficientFunds_returns422() {
    AmountRequest req = new AmountRequest().amount(10_000L).currency("ILS");
    when(service.withdraw(10_000L, "ILS", USER))
        .thenReturn(Mono.error(new InsufficientFundsException()));
    client.post()
        .uri(PATH_WITHDRAW)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HDR_USER, USER)
        .bodyValue(req)
        .exchange()
        .expectStatus().isEqualTo(422);
  }
}
