package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;
import ru.practicum.project.config.PostgresR2dbcTestBase;
import ru.practicum.project.model.Cart;
import ru.practicum.project.repository.CartRepository;

@DataR2dbcTest
@ActiveProfiles("test")
class CartRepositoryTest extends PostgresR2dbcTestBase {

    @Autowired CartRepository cartRepository;

    @Test
    void findByOwnerUsername_and_existsByIdAndOwnerUsername() {
        Cart c = new Cart(null, "alice");
        StepVerifier.create(
            cartRepository.save(c)
                .flatMap(saved -> cartRepository.findByOwnerUsername("alice")
                    .zipWith(cartRepository.existsByIdAndOwnerUsername(saved.getId(), "alice"),
                             (found, exists) -> new Object[]{found, exists})
                )
        ).assertNext(arr -> {
            Cart found = (Cart) arr[0];
            Boolean exists = (Boolean) arr[1];
            assertThat(found.getOwnerUsername()).isEqualTo("alice");
            assertThat(exists).isTrue();
        }).verifyComplete();
    }
}
