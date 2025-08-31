package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.security.config.CartAccessGuard;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartLineRepository cartLineRepository;

    @Mock
    private ItemQueryService itemQueryService;

    @Mock
    private CartAccessGuard cartAccessGuard;

    @InjectMocks
    private CartServiceImpl cartService;

    private final Long cartId = 1L;
    private final Long itemId = 10L;

    private CartLine line;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        line = new CartLine(5L, 2, cartId, itemId);

        itemDto = new ItemDto();
        itemDto.setId(itemId);
        itemDto.setTitle("Test item");
        itemDto.setPrice(100);

        when(cartAccessGuard.requireOwner(cartId)).thenReturn(Mono.empty());
    }

    @Test
    void shouldReturnCartItems_usesItemQueryService() {
        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(line));
        when(itemQueryService.getItemById(itemId)).thenReturn(Mono.just(itemDto));

        StepVerifier.create(cartService.getCartItems(cartId))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(itemId);
                assertThat(dto.getTitle()).isEqualTo("Test item");
                assertThat(dto.getCount()).isEqualTo(2);
            })
            .verifyComplete();

        verify(itemQueryService).getItemById(itemId);
    }

    @Test
    void shouldCalculateTotal_usesItemQueryService() {
        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(line));
        when(itemQueryService.getItemById(itemId)).thenReturn(Mono.just(itemDto));

        StepVerifier.create(cartService.getTotal(cartId))
            .expectNext(200)
            .verifyComplete();
    }

    @Test
    void shouldAddNewLineIfAbsent() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Mono.empty());
        when(cartLineRepository.save(any(CartLine.class)))
            .thenReturn(Mono.just(new CartLine(7L, 1, cartId, itemId)));

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.ADD))
            .verifyComplete();
        verify(cartLineRepository).save(argThat(saved ->
            saved.getCartId().equals(cartId) &&
            saved.getItemId().equals(itemId) &&
            saved.getQuantity() == 1
        ));
        verify(cartAccessGuard).requireOwner(cartId);
        verify(itemQueryService, never()).getItemById(itemId);
    }

    @Test
    void shouldIncrementQuantity() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Mono.just(line));
        when(cartLineRepository.save(any(CartLine.class)))
            .thenReturn(Mono.just(new CartLine(5L, 3, cartId, itemId)));

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.PLUS))
            .verifyComplete();
       verify(cartLineRepository).save(argThat(saved -> saved.getQuantity() == 3));
    }

    @Test
    void shouldDecrementQuantity() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Mono.just(line));
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
        when(cartLineRepository.deleteById(single.getId())).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.MINUS))
            .verifyComplete();
        verify(cartLineRepository).deleteById(5L);
    }

    @Test
    void shouldDeleteLineWhenActionIsDelete() {
        when(cartLineRepository.findByCartIdAndItemId(cartId, itemId)).thenReturn(Mono.just(line));
        when(cartLineRepository.deleteById(line.getId())).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItem(cartId, itemId, CartAction.DELETE))
            .verifyComplete();
        verify(cartLineRepository).deleteById(line.getId());
    }
}
