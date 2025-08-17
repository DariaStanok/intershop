package ru.practicum.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.service.CartService;
import ru.practicum.project.service.ItemService;

@WebFluxTest(MainController.class)
class MainControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @Test
    void redirectsToMainTest() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main/items");
    }

    @Test
    void mainPageWithModelAttributesTest() {
        when(itemService.getItems(anyString(), any(), anyInt(), anyInt(), anyLong()))
                .thenReturn(Mono.just(List.of()));
        when(cartService.getTotal(anyLong()))
                .thenReturn(Mono.just(0));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageSize", "10")
                        .queryParam("pageNumber", "1")
                        .queryParam("cartId", "123")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class) 
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assert html != null;
                    assert html.contains("main"); 
                });
    }

    @Test
    void updateCartItemTest() {
        when(cartService.updateItem(anyLong(), anyLong(), any()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items/1")
                        .queryParam("action", "plus")
                        .queryParam("cartId", "123")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageSize", "10")
                        .queryParam("pageNumber", "1")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location",
                	    "/main/items\\?.*(cartId=123).*");

        verify(cartService).updateItem(eq(123L), eq(1L), eq(CartAction.PLUS));
    }
}