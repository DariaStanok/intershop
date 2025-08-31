package ru.practicum.project.payment.dao;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.payment.config.PostgresR2dbcTestBase;
import ru.practicum.project.payment.model.Balance;
import ru.practicum.project.payment.repository.BalanceRepository;

@DataR2dbcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) 
class BalanceRepositoryTest extends PostgresR2dbcTestBase {

  @Autowired 
  DatabaseClient db;
  @Autowired 
  BalanceRepository repo;

  @BeforeAll
  void createSchema() {
    db.sql("""
      CREATE TABLE IF NOT EXISTS balances (
        id BIGSERIAL PRIMARY KEY,
        username  VARCHAR(255) NOT NULL,
        currency  VARCHAR(16)  NOT NULL,
        amount    BIGINT       NOT NULL
      );
    """).fetch().rowsUpdated().block();
    db.sql("""
      CREATE INDEX IF NOT EXISTS idx_balances_user_curr
      ON balances (username, currency);
    """).fetch().rowsUpdated().block();
  }

  @BeforeEach
  void resetAndSeed() {
    Mono<Void> chain = db.sql("TRUNCATE TABLE balances RESTART IDENTITY CASCADE").then();
    StepVerifier.create(chain).verifyComplete();
  }

  @Test
  void save_and_findByUsernameAndCurrency() {
    Balance bal = new Balance(null, "alice", "ILS", 1_000L);

    StepVerifier.create(
        repo.save(bal).flatMap(saved -> repo.findByUsernameAndCurrency("alice", "ILS"))
    )
    .assertNext(found -> {
      assertThat(found.getId()).isNotNull();
      assertThat(found.getUsername()).isEqualTo("alice");
      assertThat(found.getCurrency()).isEqualTo("ILS");
      assertThat(found.getAmount()).isEqualTo(1_000L);
    })
    .verifyComplete();
  }

  @Test
  void findByUsernameAndCurrency_emptyWhenNotExists() {
    StepVerifier.create(repo.findByUsernameAndCurrency("nobody", "USD")).verifyComplete();
  }
}
