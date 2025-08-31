package ru.practicum.project.payment.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.payment.exception.BadAmountException;
import ru.practicum.project.payment.exception.InsufficientFundsException;
import ru.practicum.project.payment.model.Balance;
import ru.practicum.project.payment.repository.BalanceRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock BalanceRepository balances;
  PaymentServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new PaymentServiceImpl(balances,50_000L);
  }
  @Test
  void deposit_nonPositive_missingBalance_createsInitialOnly() {
    when(balances.findByUsernameAndCurrency("alice","ILS")).thenReturn(Mono.empty());
    when(balances.save(any(Balance.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.deposit(0L, "ILS", "alice"))
        .assertNext(amount -> assertThat(amount).isEqualTo(50_000L))
        .verifyComplete();

    ArgumentCaptor<Balance> cap = ArgumentCaptor.forClass(Balance.class);
    verify(balances).save(cap.capture());
    assertThat(cap.getValue().getUsername()).isEqualTo("alice");
    assertThat(cap.getValue().getCurrency()).isEqualTo("ILS");
    assertThat(cap.getValue().getAmount()).isEqualTo(50_000L);
  }

  @Test
  void deposit_existingBalance_adds() {
    Balance existing = new Balance(1L, "alice", "ILS", 1000L);
    when(balances.findByUsernameAndCurrency("alice","ILS")).thenReturn(Mono.just(existing));
    when(balances.save(any(Balance.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.deposit(200L, "ILS", "alice"))
        .assertNext(amount -> assertThat(amount).isEqualTo(1200L))
        .verifyComplete();

    ArgumentCaptor<Balance> cap = ArgumentCaptor.forClass(Balance.class);
    verify(balances).save(cap.capture());
    assertThat(cap.getValue().getAmount()).isEqualTo(1200L);
  }

  @Test
  void deposit_missingBalance_createsInitial_withoutAddingAmount() {
    when(balances.findByUsernameAndCurrency("alice","ILS")).thenReturn(Mono.empty());
    when(balances.save(any(Balance.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.deposit(200L, "ILS", "alice"))
        .assertNext(amount -> assertThat(amount).isEqualTo(50_000L))
        .verifyComplete();

    ArgumentCaptor<Balance> cap = ArgumentCaptor.forClass(Balance.class);
    verify(balances, atLeastOnce()).save(cap.capture());
    assertThat(cap.getAllValues()).hasSize(1);
    assertThat(cap.getAllValues().get(0).getAmount()).isEqualTo(50_000L);
  }

  @Test
  void withdraw_rejectsNonPositive() {
    StepVerifier.create(service.withdraw(0, "ILS", "alice"))
        .expectError(BadAmountException.class).verify();
    verifyNoInteractions(balances);
  }

  @Test
  void withdraw_insufficient_throws_andNotSaved() {
    Balance existing = new Balance(1L, "alice", "ILS", 100L);
    when(balances.findByUsernameAndCurrency("alice","ILS")).thenReturn(Mono.just(existing));

    StepVerifier.create(service.withdraw(200L, "ILS", "alice"))
        .expectError(InsufficientFundsException.class).verify();

    verify(balances, never()).save(any());
  }

  @Test
  void withdraw_ok_decreasesAndSaves() {
    Balance existing = new Balance(1L, "alice", "ILS", 1000L);
    when(balances.findByUsernameAndCurrency("alice","ILS")).thenReturn(Mono.just(existing));
    when(balances.save(any(Balance.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.withdraw(200L, "ILS", "alice"))
        .assertNext(amount -> assertThat(amount).isEqualTo(800L))
        .verifyComplete();

    ArgumentCaptor<Balance> cap = ArgumentCaptor.forClass(Balance.class);
    verify(balances).save(cap.capture());
    assertThat(cap.getValue().getAmount()).isEqualTo(800L);
  }
}