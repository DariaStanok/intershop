package ru.practicum.project.security.utils;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SecurityUtilsTest {

    @Test
    void currentUsername_returnsUsername_whenAuthenticated() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pw", Collections.emptyList());
        Mono<String> mono = SecurityUtils.currentUsername()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        StepVerifier.create(mono)
            .expectNext("alice")
            .verifyComplete();
    }

    @Test
    void currentUsername_returnsEmpty_whenNoAuthentication() {
        StepVerifier.create(SecurityUtils.currentUsername())
            .verifyComplete();
    }

    @Test
    void currentUsernameOrEmpty_returnsUsername_whenAuthenticated() {
        Authentication auth = new TestingAuthenticationToken("bob", "pw", "ROLE_USER");
        Mono<String> mono = SecurityUtils.currentUsernameOrEmpty()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        StepVerifier.create(mono)
            .expectNext("bob")
            .verifyComplete();
    }

    @Test
    void currentUsernameOrEmpty_returnsEmptyString_whenNoAuthentication() {
        StepVerifier.create(SecurityUtils.currentUsernameOrEmpty())
            .expectNext("")
            .verifyComplete();
    }
}