package ru.practicum.project.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.service.CartService;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @Test
    void viewCartPage_ReturnCartView() {
        List<ItemDto> mockItems = List.of();

        when(cartService.getCartItems(anyLong())).thenReturn(Flux.fromIterable(mockItems));
        when(cartService.getTotal(anyLong())).thenReturn(Mono.just(200));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("cartId", "123")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assert html != null;
                    assert html.contains("Корзина") || html.contains("<title>Cart</title>");
                });
    }
}
