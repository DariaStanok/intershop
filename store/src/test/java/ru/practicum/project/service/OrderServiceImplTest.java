package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import ru.practicum.project.dto.ItemDto;
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

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartLineRepository cartLineRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private PaymentClient paymentClient;
    @Mock private ItemQueryService itemQueryService; 
    
    private final ModelMapper modelMapper = new ModelMapper();
    private OrderServiceImpl orderService;

    private final Long cartId = 1L;
    private final Long itemId = 10L;
    private final Long orderId = 100L;

    private CartLine cartLine;
    private Item item;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
            orderRepository, orderItemRepository, cartLineRepository, itemRepository, modelMapper, paymentClient, itemQueryService
        );
        cartLine = new CartLine(5L, 2, cartId, itemId);
        item = new Item(itemId, "Phone", "desc", 300, "img.jpg", 0);
    }

    @Test
    void shouldCreateOrderSuccessfullyAndDeleteCart() {
        Order order = new Order(orderId);
        long expectedTotal = 600L;

        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(cartLine));
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item));
        when(paymentClient.withdraw(eq(expectedTotal), eq("ILS"))).thenReturn(Mono.just(true));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> {
            OrderItem oi = inv.getArgument(0);
            return Mono.just(new OrderItem(1L, oi.getOrderId(), oi.getItemId(), oi.getCount()));
        });
        when(cartLineRepository.deleteById(cartId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder(cartId))
            .expectNextMatches(dto -> dto.getId().equals(orderId) && dto.getTotal() == 600)
            .verifyComplete();

        verify(cartLineRepository).deleteById(cartId);
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

        ItemDto dto = modelMapper.map(item, ItemDto.class);
        when(itemQueryService.getItemById(itemId)).thenReturn(Mono.just(dto));
        
        StepVerifier.create(orderService.getAllOrders())
            .assertNext(res -> {
                assertThat(res.getId()).isEqualTo(orderId);
                assertThat(res.getItems()).hasSize(1);
                assertThat(res.getItems().get(0).getId()).isEqualTo(itemId);
                assertThat(res.getItems().get(0).getCount()).isEqualTo(2);
                assertThat(res.getTotal()).isEqualTo(600);
            })
            .verifyComplete();
    }

    @Test
    void shouldGetOrderByIdWithItems() {
        Order order = new Order(orderId);
        OrderItem orderItem = new OrderItem(1L, orderId, itemId, 2);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));

        ItemDto dto = modelMapper.map(item, ItemDto.class);
        when(itemQueryService.getItemById(itemId)).thenReturn(Mono.just(dto));

        StepVerifier.create(orderService.getOrderById(orderId))
            .assertNext(res -> {
                assertThat(res.getId()).isEqualTo(orderId);
                assertThat(res.getItems()).hasSize(1);
                assertThat(res.getItems().get(0).getId()).isEqualTo(itemId);
                assertThat(res.getItems().get(0).getCount()).isEqualTo(2);
                assertThat(res.getTotal()).isEqualTo(600);
            })
            .verifyComplete();
    }
}