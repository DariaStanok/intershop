package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;
import ru.practicum.project.config.PostgresR2dbcTestBase;
import ru.practicum.project.model.Cart;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.CartRepository;
import ru.practicum.project.repository.ItemRepository;

@DataR2dbcTest
@ActiveProfiles("test")
class CartLineRepositoryTest extends PostgresR2dbcTestBase {

    @Autowired CartLineRepository cartLineRepository;
    @Autowired CartRepository cartRepository;
    @Autowired ItemRepository itemRepository;

    Long cartId;
    Long itemId;

    @BeforeEach
    void seed() {
        Cart cart = new Cart(null, "alice");
        Item item = new Item(null, "Phone", "d", 1000, "img", 0);

        cartId = cartRepository.save(cart).map(Cart::getId).block();
        itemId = itemRepository.save(item).map(Item::getId).block();

        cartLineRepository.save(new CartLine(null, 2, cartId, itemId)).block();
    }

    @Test
    void findByCartId_and_findByCartIdAndItemId_and_deleteByCartId() {
        StepVerifier.create(cartLineRepository.findByCartId(cartId).collectList())
            .assertNext(list -> {
                assertThat(list).hasSize(1);
                assertThat(list.get(0).getQuantity()).isEqualTo(2);
            }).verifyComplete();

        StepVerifier.create(cartLineRepository.findByCartIdAndItemId(cartId, itemId))
            .assertNext(cl -> assertThat(cl.getQuantity()).isEqualTo(2))
            .verifyComplete();

        StepVerifier.create(cartLineRepository.deleteByCartId(cartId))
            .expectNext(1L) 
            .verifyComplete();

        StepVerifier.create(cartLineRepository.findByCartId(cartId)).verifyComplete();
    }
}
