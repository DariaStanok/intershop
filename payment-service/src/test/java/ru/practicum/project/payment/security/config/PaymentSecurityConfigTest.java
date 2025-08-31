package ru.practicum.project.payment.security.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import ru.practicum.project.payment.controller.PaymentsController;
import ru.practicum.project.payment.model.AmountRequest;
import ru.practicum.project.payment.service.PaymentService;

@WebFluxTest(controllers = PaymentsController.class)
@Import(PaymentSecurityConfig.class)
@TestPropertySource(properties = {
    "server.port=0" 
})
class PaymentSecurityConfigTest {

  private static final String HDR_USER = "X-User";
  private static final String USER = "alice";

  @Autowired 
  WebTestClient client;

  @MockBean 
  PaymentService service;

  @TestConfiguration
  static class JwtStubConfig {
    @Bean
    ReactiveJwtDecoder jwtDecoder() {
      return Mockito.mock(ReactiveJwtDecoder.class);
    }
  }

  @Test
  void deposit_unauthorized_withoutToken_401() {
    AmountRequest req = new AmountRequest().amount(100L).currency("ILS");

    client.post().uri("/payments/deposit")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HDR_USER, USER)
        .bodyValue(req)
        .exchange()
        .expectStatus().isUnauthorized(); 
  }

  @Test
  void deposit_forbidden_withReadScope_403() {
    AmountRequest req = new AmountRequest().amount(100L).currency("ILS");

    client.mutateWith(
            mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_payment.read"))
        )
        .post().uri("/payments/deposit")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HDR_USER, USER)
        .bodyValue(req)
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void deposit_ok_withWriteScope_200() {
    AmountRequest req = new AmountRequest().amount(200L).currency("ILS");
    when(service.deposit(eq(200L), eq("ILS"), eq(USER))).thenReturn(Mono.just(50_200L));

    client.mutateWith(
            mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_payment.write"))
        )
        .post().uri("/payments/deposit")
        .contentType(MediaType.APPLICATION_JSON)
        .header(HDR_USER, USER)
        .bodyValue(req)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
          .jsonPath("$.success").isEqualTo(true)
          .jsonPath("$.newBalance").isEqualTo(50_200);
  }

  @Test
  void deposit_badRequest_whenMissingUserHeader_butAuthenticated() {
    AmountRequest req = new AmountRequest().amount(100L).currency("ILS");

    client.mutateWith(
            mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_payment.write"))
        )
        .post().uri("/payments/deposit")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .exchange()
        .expectStatus().isBadRequest(); 
  }
}
