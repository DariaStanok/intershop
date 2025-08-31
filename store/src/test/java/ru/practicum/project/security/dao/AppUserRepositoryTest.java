package ru.practicum.project.security.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.config.PostgresR2dbcTestBase;
import ru.practicum.project.security.model.AppUser;
import ru.practicum.project.security.repository.AppUserRepository;

@DataR2dbcTest
class AppUserRepositoryTest extends PostgresR2dbcTestBase {

    @Autowired AppUserRepository appUserRepository;
    @Autowired DatabaseClient db;

    @BeforeEach
    void resetAndSeed() {
        Mono<Void> chain =
            db.sql("""
                TRUNCATE TABLE
                  order_item, cart_lines, orders, carts, items,
                  app_user_roles, app_role, app_user, balances
                RESTART IDENTITY CASCADE
            """).then()
            .then(db.sql("""
                INSERT INTO app_user(id, username, password, enabled)
                VALUES (1, 'alice', '{noop}password', TRUE)
            """).then());
        StepVerifier.create(chain).verifyComplete();
    }

    @Test
    void findByUsername_fromPreloadedData() {
        StepVerifier.create(appUserRepository.findByUsername("alice"))
            .assertNext((AppUser u) -> {
                assertThat(u.getId()).isEqualTo(1L);
                assertThat(u.getUsername()).isEqualTo("alice");
                assertThat(Boolean.TRUE.equals(u.isEnabled())).isTrue(); 
            })
            .verifyComplete();
    }

    @Test
    void findByUsername_emptyWhenNotExists() {
        StepVerifier.create(appUserRepository.findByUsername("nobody"))
            .verifyComplete();
    }
}