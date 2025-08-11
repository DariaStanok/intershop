package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartLineRepository cartLineRepository;

    @Mock
    private ItemRepository itemRepository;

    private CartServiceImpl cartService;

    private final ModelMapper modelMapper = new ModelMapper();

    private final Long cartId = 1L;
    private final Long itemId = 10L;

    private Item item;
    private CartLine line;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartLineRepository, itemRepository, modelMapper);

        item = new Item();
        item.setId(itemId);
        item.setTitle("Test item");
        item.setPrice(100);
        item.setDescription("desc");
        item.setImgPath("img.jpg");

        line = new CartLine(5L, 2, cartId, itemId);
    }

    @Test
    void shouldReturnCartItems() {
        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(line));
        when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        StepVerifier.create(cartService.getCartItems(cartId))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(itemId);
                assertThat(dto.getTitle()).isEqualTo(item.getTitle());
                assertThat(dto.getCount()).isEqualTo(2);
            })
            .verifyComplete();
    }

    @Test
    void shouldCalculateTotal() {
        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(line));
        when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        StepVerifier.create(cartService.getTotal(cartId))
            .expectNext(200)
            .verifyComplete();
    }

    @Test
    void shouldAddNewLineIfAbsent() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId))
            .thenReturn(Mono.empty());

        when(cartLineRepository.save(any(CartLine.class)))
            .thenReturn(Mono.just(new CartLine(7L, 1, cartId, itemId)));

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.ADD))
            .verifyComplete();

        verify(cartLineRepository).save(argThat(saved ->
            saved.getCartId().equals(cartId) &&
            saved.getItemId().equals(itemId) &&
            saved.getQuantity() == 1
        ));
    }

    @Test
    void shouldIncrementQuantity() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId))
            .thenReturn(Mono.just(line));

        when(cartLineRepository.save(any(CartLine.class)))
            .thenReturn(Mono.just(new CartLine(5L, 3, cartId, itemId)));

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.PLUS))
            .verifyComplete();

        verify(cartLineRepository).save(argThat(saved -> saved.getQuantity() == 3));
    }

    @Test
    void shouldDecrementQuantity() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId))
            .thenReturn(Mono.just(line));

        when(cartLineRepository.save(any(CartLine.class)))
            .thenReturn(Mono.just(new CartLine(5L, 1, cartId, itemId)));

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.MINUS))
            .verifyComplete();

        verify(cartLineRepository).save(argThat(saved -> saved.getQuantity() == 1));
    }

    @Test
    void shouldRemoveLineWhenQuantityReachesZero() {
        CartLine single = new CartLine(5L, 1, cartId, itemId);

        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId))
            .thenReturn(Mono.just(single));

        when(cartLineRepository.deleteById(single.getId()))
            .thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.MINUS))
            .verifyComplete();

        verify(cartLineRepository).deleteById(5L);
    }

    @Test
    void shouldDeleteLineWhenActionIsDelete() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId))
            .thenReturn(Mono.just(line));

        when(cartLineRepository.deleteById(line.getId()))
            .thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.DELETE))
            .verifyComplete();

        verify(cartLineRepository).deleteById(line.getId());
    }
}
