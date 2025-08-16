package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.exсeption.EmptyCartException;
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
                orderRepository, orderItemRepository, cartLineRepository,
                itemRepository, modelMapper, paymentClient, itemQueryService
        );
        cartLine = new CartLine(5L, 2, cartId, itemId);
        item = new Item(itemId, "Phone", "desc", 300, "img.jpg", 0);
    }

    @Test
    void createOrder_success_singleLine_andCleansCart() {
        Order saved = new Order(orderId);
        long expectedTotal = 2L * 300L; 

        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(cartLine));
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item));
        when(paymentClient.withdraw(anyLong(), anyString())).thenReturn(Mono.just(true));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(saved));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> {
            OrderItem oi = inv.getArgument(0);
            return Mono.just(new OrderItem(1L, oi.getOrderId(), oi.getItemId(), oi.getCount()));
        });
        when(cartLineRepository.deleteByCartId(cartId)).thenReturn(Mono.just(1L));

        StepVerifier.create(orderService.createOrder(cartId))
                .assertNext(dto -> {
                    assertThat(dto.getId()).isEqualTo(orderId);
                    assertThat(dto.getTotal()).isEqualTo((int) expectedTotal);
                })
                .verifyComplete();

        verify(paymentClient).withdraw(eq(expectedTotal), eq("ILS"));
        verify(cartLineRepository).deleteByCartId(cartId);
    }

    @Test
    void createOrder_success_withDuplicateItemLines() {
        CartLine cartLine2 = new CartLine(6L, 3, cartId, itemId);
        long expectedTotal = (2L + 3L) * 300L; 

        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.fromIterable(List.of(cartLine, cartLine2)));
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item));
        when(paymentClient.withdraw(anyLong(), anyString())).thenReturn(Mono.just(true));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(new Order(orderId)));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> {
            OrderItem oi = inv.getArgument(0);
            return Mono.just(new OrderItem(1L, oi.getOrderId(), oi.getItemId(), oi.getCount()));
        });
        when(cartLineRepository.deleteByCartId(cartId)).thenReturn(Mono.just(1L));
        StepVerifier.create(orderService.createOrder(cartId))
                .assertNext(dto -> {
                    assertThat(dto.getId()).isEqualTo(orderId);
                    assertThat(dto.getTotal()).isEqualTo((int) expectedTotal);
                })
                .verifyComplete();

        verify(paymentClient).withdraw(eq(expectedTotal), eq("ILS"));
        verify(cartLineRepository).deleteByCartId(cartId);
    }

    @Test
    void createOrder_emptyCart_throwsEmptyCartException() {
        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.empty());
        StepVerifier.create(orderService.createOrder(cartId))
                .expectError(EmptyCartException.class)
                .verify();
    }

    @Test
    void createOrder_missingItem_throwsIllegalStateException_andDoesNotClean() {
        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(cartLine));
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.empty());
        StepVerifier.create(orderService.createOrder(cartId))
                .expectError(IllegalStateException.class)
                .verify();

        verify(paymentClient, never()).withdraw(anyLong(), anyString());
        verify(cartLineRepository, never()).deleteByCartId(cartId);
    }

    @Test
    void createOrder_paymentFails_throwsRuntimeException_andDoesNotClean() {
        long expectedTotal = 2L * 300L;

        when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(cartLine));
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item));
        when(paymentClient.withdraw(anyLong(), anyString())).thenReturn(Mono.just(false));
        StepVerifier.create(orderService.createOrder(cartId))
                .expectError(RuntimeException.class)
                .verify();

        verify(paymentClient).withdraw(eq(expectedTotal), eq("ILS"));
        verify(cartLineRepository, never()).deleteByCartId(cartId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllOrders_returnsOrdersWithItemsAndTotal() {
        Order order = new Order(orderId);
        OrderItem oi = new OrderItem(1L, orderId, itemId, 2);

        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(oi));
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
    void getOrderById_returnsOrderWithItemsAndTotal() {
        Order order = new Order(orderId);
        OrderItem oi = new OrderItem(1L, orderId, itemId, 2);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(oi));
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