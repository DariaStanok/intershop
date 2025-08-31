package ru.practicum.project.security.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.config.PostgresR2dbcTestBase;
import ru.practicum.project.security.repository.UserRoleRepository;

@DataR2dbcTest
class UserRoleRepositoryTest extends PostgresR2dbcTestBase {

    @Autowired DatabaseClient db;
    @Autowired UserRoleRepository userRoleRepository;

    @BeforeEach
    void seed() {
        Mono<Void> chain =
         
            db.sql("""
                    INSERT INTO app_user (username, password, enabled)
                    VALUES (:u, :p, TRUE)
                    ON CONFLICT (username) DO NOTHING
                   """)
              .bind("u", "alice")
              .bind("p", "{noop}password")
              .then()
            .then(db.sql("""
                    INSERT INTO app_role (name) VALUES (:n)
                    ON CONFLICT (name) DO NOTHING
                   """).bind("n", "ROLE_USER").then())
            .then(db.sql("""
                    INSERT INTO app_role (name) VALUES (:n)
                    ON CONFLICT (name) DO NOTHING
                   """).bind("n", "ROLE_ADMIN").then())

            .then(db.sql("""
                    INSERT INTO app_user_roles (user_id, role_id)
                    SELECT au.id, ar.id
                    FROM app_user au, app_role ar
                    WHERE au.username = :u AND ar.name = :r
                    ON CONFLICT (user_id, role_id) DO NOTHING
                   """).bind("u", "alice").bind("r", "ROLE_USER").then())
            .then(db.sql("""
                    INSERT INTO app_user_roles (user_id, role_id)
                    SELECT au.id, ar.id
                    FROM app_user au, app_role ar
                    WHERE au.username = :u AND ar.name = :r
                    ON CONFLICT (user_id, role_id) DO NOTHING
                   """).bind("u", "alice").bind("r", "ROLE_ADMIN").then());

        StepVerifier.create(chain).verifyComplete();
    }

    @Test
    void findRoleNamesByUsername_returnsAllRoles() {
        Flux<String> roles = userRoleRepository.findRoleNamesByUsername("alice");

        StepVerifier.create(roles.collectList())
            .assertNext(list ->
                assertThat(list).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
            )
            .verifyComplete();
    }

    @Test
    void findRoleNamesByUsername_emptyForUnknownUser() {
        StepVerifier.create(userRoleRepository.findRoleNamesByUsername("nobody"))
            .verifyComplete();
    }
}