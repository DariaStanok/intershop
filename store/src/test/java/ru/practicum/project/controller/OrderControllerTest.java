package ru.practicum.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.exception.ResponseStatusException;
import ru.practicum.project.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController controller;

    private OrderDto sampleOrder(long id, int total) {
        ItemDto it = new ItemDto();
        it.setId(1L); it.setTitle("Phone"); it.setPrice(1000); it.setCount(2);
        return new OrderDto(id, List.of(it), total);
    }

    @Test
    void createOrder_redirectsToOrderPageWithNewOrderFlag() {
        Long cartId = 5L;
        OrderDto order = sampleOrder(42L, 2000);
        when(orderService.createOrder(cartId)).thenReturn(Mono.just(order));

        StepVerifier.create(controller.createOrder(cartId))
            .assertNext(view ->
                assertThat(view).isEqualTo("redirect:/orders/42?newOrder=true"))
            .verifyComplete();
    }

    @Test
    void getMyOrderById_populatesModelAndReturnsOrderTemplate() {
        Long orderId = 7L;
        boolean newOrder = true;
        OrderDto order = sampleOrder(orderId, 1234);
        when(orderService.getMyOrderById(orderId)).thenReturn(Mono.just(order));

        Model model = new ConcurrentModel();

        StepVerifier.create(controller.getMyOrderById(orderId, newOrder, model))
            .assertNext(view -> {
                assertThat(view).isEqualTo("order");
                Object mOrder = model.getAttribute("order");
                Object mNew = model.getAttribute("newOrder");
                assertThat(mOrder).isInstanceOf(OrderDto.class);
                assertThat(((OrderDto) mOrder).getId()).isEqualTo(orderId);
                assertThat(mNew).isEqualTo(true);
            })
            .verifyComplete();
    }

    @Test
    void getMyOrderById_empty_throwsResponseStatusException() {
        Long orderId = 99L;
        when(orderService.getMyOrderById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(controller.getMyOrderById(orderId, false, new ConcurrentModel()))
            .expectError(ResponseStatusException.class)
            .verify();
    }

    @Test
    void adminGetOrderById_populatesModelAndReturnsOrderTemplate() {
        Long orderId = 11L;
        OrderDto order = sampleOrder(orderId, 777);
        when(orderService.getOrderById(orderId)).thenReturn(Mono.just(order));

        Model model = new ConcurrentModel();

        StepVerifier.create(controller.adminGetOrderById(orderId, model))
            .assertNext(view -> {
                assertThat(view).isEqualTo("order");
                OrderDto dto = (OrderDto) model.getAttribute("order");
                Boolean newFlag = (Boolean) model.getAttribute("newOrder");
                assertThat(dto.getId()).isEqualTo(orderId);
                assertThat(newFlag).isFalse();
            })
            .verifyComplete();
    }

    @Test
    void adminGetOrderById_empty_throwsResponseStatusException() {
        Long orderId = 12L;
        when(orderService.getOrderById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(controller.adminGetOrderById(orderId, new ConcurrentModel()))
            .expectError(ResponseStatusException.class)
            .verify();
    }
}
