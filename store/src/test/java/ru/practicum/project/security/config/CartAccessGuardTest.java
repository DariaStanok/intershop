package ru.practicum.project.security.config;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.exception.ResponseStatusException;
import ru.practicum.project.repository.CartRepository;
import ru.practicum.project.security.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class CartAccessGuardTest {

    @Mock
    CartRepository cartRepository;
    @InjectMocks
    CartAccessGuard guard;

    @Test
    void requireOwner_completes_whenUserOwnsCart() {
        Long cartId = 42L;

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just("alice"));
            when(cartRepository.existsByIdAndOwnerUsername(cartId, "alice"))
                .thenReturn(Mono.just(true));

            StepVerifier.create(guard.requireOwner(cartId))
                .verifyComplete();
            verify(cartRepository).existsByIdAndOwnerUsername(cartId, "alice");
            verifyNoMoreInteractions(cartRepository);
        }
    }

    @Test
    void requireOwner_errors_whenUserNotOwner() {
        Long cartId = 13L;

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just("bob"));
            when(cartRepository.existsByIdAndOwnerUsername(cartId, "bob"))
                .thenReturn(Mono.just(false));
            StepVerifier.create(guard.requireOwner(cartId))
                .expectError(ResponseStatusException.class)
                .verify();

            verify(cartRepository).existsByIdAndOwnerUsername(cartId, "bob");
            verifyNoMoreInteractions(cartRepository);
        }
    }

    @Test
    void requireOwner_errors_whenNoAuthenticatedUser() {
        Long cartId = 7L;

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.empty());

            StepVerifier.create(guard.requireOwner(cartId))
                .expectError(ResponseStatusException.class)
                .verify();
            verifyNoInteractions(cartRepository);
        }
    }
}
