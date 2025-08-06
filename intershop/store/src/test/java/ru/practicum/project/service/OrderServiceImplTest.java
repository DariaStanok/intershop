package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.exeption.EmptyCartException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartLineRepository cartLineRepository;

    @Mock
    private ItemRepository itemRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    private OrderServiceImpl orderService;

    private final Long cartId = 1L;
    private final Long itemId = 10L;
    private final Long orderId = 100L;

    private CartLine cartLine;
    private Item item;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderItemRepository, cartLineRepository, itemRepository, modelMapper);

        cartLine = new CartLine(5L, 2, cartId, itemId);
        item = new Item(itemId, "Phone", "desc", 300, "img.jpg", 0);
    }

    @Test
    void shouldCreateOrderSuccessfullyAndDeleteCart() {
        Order order = new Order(orderId);

        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(cartLine));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.fromIterable(List.of(
                new OrderItem(null, orderId, itemId, 2)
        )));
        when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));
        when(cartLineRepository.deleteById(cartLine.getId())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder(cartId))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(orderId);
                assertThat(dto.getItems()).hasSize(1);
                assertThat(dto.getItems().get(0).getId()).isEqualTo(itemId);
                assertThat(dto.getTotal()).isEqualTo(600); 
            })
            .verifyComplete();

        verify(cartLineRepository).deleteById(cartLine.getId());
    }

    @Test
    void shouldThrowIfCartIsEmpty() {
        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.createOrder(cartId))
            .expectError(EmptyCartException.class)
            .verify();
    }

    @Test
    void shouldGetAllOrdersWithItems() {
        Order order = new Order(orderId);
        OrderItem orderItem = new OrderItem(1L, orderId, itemId, 2);

        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        StepVerifier.create(orderService.getAllOrders())
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(orderId);
                assertThat(dto.getItems()).hasSize(1);
                assertThat(dto.getTotal()).isEqualTo(600);
            })
            .verifyComplete();
    }

    @Test
    void shouldGetOrderByIdWithItems() {
        Order order = new Order(orderId);
        OrderItem orderItem = new OrderItem(1L, orderId, itemId, 2);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        StepVerifier.create(orderService.getOrderById(orderId))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(orderId);
                assertThat(dto.getItems()).hasSize(1);
                assertThat(dto.getTotal()).isEqualTo(600);
            })
            .verifyComplete();
    }
}
