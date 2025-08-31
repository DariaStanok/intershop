package ru.practicum.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.service.CartService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController controller;

    @Test
    void viewCart_populatesModelAndReturnsView() {
        Long cartId = 1L;

        ItemDto i1 = new ItemDto();
        i1.setId(100L);
        i1.setTitle("Phone");
        i1.setPrice(2000);
        i1.setCount(2);

        ItemDto i2 = new ItemDto();
        i2.setId(200L);
        i2.setTitle("Watch");
        i2.setPrice(1000);
        i2.setCount(1);

        when(cartService.getCartItems(cartId)).thenReturn(Flux.just(i1, i2));
        when(cartService.getTotal(cartId)).thenReturn(Mono.just(5000));

        Model model = new ConcurrentModel();

        StepVerifier.create(controller.viewCart(cartId, model))
            .assertNext(viewName -> {
                assertThat(viewName).isEqualTo("cart");
             
                @SuppressWarnings("unchecked")
                List<ItemDto> items = (List<ItemDto>) model.getAttribute("items");
                Integer total = (Integer) model.getAttribute("total");
                Long modelCartId = (Long) model.getAttribute("cartId");

                assertThat(items).containsExactly(i1, i2);
                assertThat(total).isEqualTo(5000);
                assertThat(modelCartId).isEqualTo(cartId);
            })
            .verifyComplete();

        verify(cartService).getCartItems(cartId);
        verify(cartService).getTotal(cartId);
    }

    @Test
    void updateCartFromCart_redirectsAndCallsService() {
        Long cartId = 1L;
        Long itemId = 100L;

        when(cartService.updateItem(cartId, itemId, CartAction.PLUS)).thenReturn(Mono.empty());

        StepVerifier.create(controller.updateCartFromCart(itemId, "plus", cartId))
            .assertNext(view -> assertThat(view).isEqualTo("redirect:/cart/items?cartId=1"))
            .verifyComplete();

        verify(cartService).updateItem(cartId, itemId, CartAction.PLUS);
    }
}
