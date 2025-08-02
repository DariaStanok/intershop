package ru.practicum.project.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.service.OrderService;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrdersReturnView() {
        when(orderService.getAllOrders()).thenReturn(Flux.fromIterable(List.of()));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assert html != null;
                    assert html.contains("orders") || html.contains("<title>Orders</title>");
                });
    }

    @Test
    void getOrderByIdReturnView() {
        OrderDto mockOrder = new OrderDto();
        mockOrder.setId(1L);
        mockOrder.setTotal(200);

        when(orderService.getOrderById(1L)).thenReturn(Mono.just(mockOrder));

        webTestClient.get()
                .uri("/orders/1?newOrder=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String html = response.getResponseBody();
                    assert html != null;
                    assert html.contains("order") || html.contains("200");
                });
    }

    @Test
    void createOrderRedirectToOrderPage() {
        OrderDto mockOrder = new OrderDto();
        mockOrder.setId(42L);
        mockOrder.setTotal(300);

        when(orderService.createOrder(anyLong())).thenReturn(Mono.just(mockOrder));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/orders/buy")
                        .queryParam("cartId", "42")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/orders/42\\?new=true");

        verify(orderService).createOrder(42L);
    }
}
