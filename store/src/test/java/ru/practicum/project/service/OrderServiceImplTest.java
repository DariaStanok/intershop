package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.exception.EmptyCartException;
import ru.practicum.project.exception.ResponseStatusException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;
import ru.practicum.project.security.config.CartAccessGuard;
import ru.practicum.project.security.utils.SecurityUtils;

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
    @Mock 
    private PaymentClient paymentClient;
    @Mock 
    private ItemQueryService itemQueryService;
    @Mock 
    private CartAccessGuard cartAccessGuard;
    private final ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private OrderServiceImpl service;
    private final Long cartId = 10L;
    private final String username = "alice";
    private CartLine line1;
    private CartLine line2;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
            orderRepository, orderItemRepository, cartLineRepository,
            itemRepository, modelMapper, paymentClient, itemQueryService, cartAccessGuard
        );

        line1 = new CartLine(1L, 2, cartId, 100L);
        line2 = new CartLine(2L, 1, cartId, 200L);

        item1 = new Item();
        item1.setId(100L);
        item1.setTitle("Phone");
        item1.setPrice(1500);

        item2 = new Item();
        item2.setId(200L);
        item2.setTitle("Watch");
        item2.setPrice(500);
    }

    @Test
    void createOrder_happyPath() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just(username));
            when(cartAccessGuard.requireOwner(cartId)).thenReturn(Mono.empty());
            when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(line1, line2));
            when(itemRepository.findAllById(List.of(100L, 200L))).thenReturn(Flux.just(item1, item2));
            when(paymentClient.withdraw(3500, "ILS")).thenReturn(Mono.just(Boolean.TRUE));

            Order savedOrder = new Order();
            savedOrder.setId(77L);
            savedOrder.setUserName(username);
            when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));

            when(orderItemRepository.save(any(OrderItem.class)))
                .thenAnswer(inv -> {
                    OrderItem oi = inv.getArgument(0, OrderItem.class);
                    return Mono.just(new OrderItem(
                        oi.getId() == null ? 1L : oi.getId(),
                        oi.getOrderId(), oi.getItemId(), oi.getCount()
                    ));
                });

            when(cartLineRepository.deleteByCartId(cartId)).thenReturn(Mono.empty());

            StepVerifier.create(service.createOrder(cartId))
                .assertNext(dto -> {
                    assertThat(dto.getId()).isEqualTo(77L);
                    assertThat(dto.getItems()).hasSize(2);
                    Map<Long, Integer> counts = dto.getItems().stream()
                        .collect(java.util.stream.Collectors.toMap(ItemDto::getId, ItemDto::getCount));
                    assertThat(counts.get(100L)).isEqualTo(2);
                    assertThat(counts.get(200L)).isEqualTo(1);
                    assertThat(dto.getTotal()).isEqualTo(3500);
                })
                .verifyComplete();

            verify(orderItemRepository).save(argThat(oi ->
                oi.getOrderId().equals(77L) && oi.getItemId().equals(100L) && oi.getCount() == 2));
            verify(orderItemRepository).save(argThat(oi ->
                oi.getOrderId().equals(77L) && oi.getItemId().equals(200L) && oi.getCount() == 1));
            verify(cartLineRepository).deleteByCartId(cartId);
        }
    }

    @Test
    void createOrder_emptyCart_throws() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just(username));
            when(cartAccessGuard.requireOwner(cartId)).thenReturn(Mono.empty());
            when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.empty());

            StepVerifier.create(service.createOrder(cartId))
                .expectError(EmptyCartException.class)
                .verify();

            verify(paymentClient, never()).withdraw(org.mockito.Mockito.anyInt(), org.mockito.Mockito.anyString());
            verify(orderRepository, never()).save(any());
        }
    }

    @Test
    void createOrder_missingItem_throwsIllegalState() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just(username));
            when(cartAccessGuard.requireOwner(cartId)).thenReturn(Mono.empty());
            when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(line1, line2));
            when(itemRepository.findAllById(List.of(100L, 200L))).thenReturn(Flux.just(item1));

            StepVerifier.create(service.createOrder(cartId))
                .expectError(IllegalStateException.class)
                .verify();

            verify(paymentClient, never()).withdraw(org.mockito.Mockito.anyInt(), org.mockito.Mockito.anyString());
            verify(orderRepository, never()).save(any());
        }
    }

    @Test
    void createOrder_paymentFails_throwsRuntimeException() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just(username));
            when(cartAccessGuard.requireOwner(cartId)).thenReturn(Mono.empty());
            when(cartLineRepository.findByCartId(cartId)).thenReturn(Flux.just(line1, line2));
            when(itemRepository.findAllById(List.of(100L, 200L))).thenReturn(Flux.just(item1, item2));

            when(paymentClient.withdraw(org.mockito.ArgumentMatchers.anyInt(),
                                        org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(Boolean.FALSE));

            StepVerifier.create(service.createOrder(cartId))
                .expectError(RuntimeException.class)
                .verify();
            verify(orderRepository, never()).save(any());
        }
    }

    @Test
    void createOrder_withoutUsername_throwsResponseStatus() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.empty());
            when(cartAccessGuard.requireOwner(cartId)).thenReturn(Mono.empty());

            StepVerifier.create(service.createOrder(cartId))
                .expectError(ResponseStatusException.class)
                .verify();
            verify(cartLineRepository, never()).findByCartId(cartId);
        }
    }

    @Test
    void getAllOrders_aggregatesItemsAndTotals() {
        Order order = new Order();
        order.setId(5L);
        order.setUserName("bob");
        when(orderRepository.findAll()).thenReturn(Flux.just(order));

        OrderItem oi1 = new OrderItem(1L, 5L, 100L, 2);
        OrderItem oi2 = new OrderItem(2L, 5L, 200L, 1);
        when(orderItemRepository.findByOrderId(5L)).thenReturn(Flux.just(oi1, oi2));

        ItemDto dto1 = new ItemDto(); dto1.setId(100L); dto1.setTitle("Phone"); dto1.setPrice(1500);
        ItemDto dto2 = new ItemDto(); dto2.setId(200L); dto2.setTitle("Watch"); dto2.setPrice(500);
        when(itemQueryService.getItemById(100L)).thenReturn(Mono.just(dto1));
        when(itemQueryService.getItemById(200L)).thenReturn(Mono.just(dto2));

        StepVerifier.create(service.getAllOrders())
            .assertNext(od -> {
                assertThat(od.getId()).isEqualTo(5L);
                assertThat(od.getItems()).hasSize(2);
                assertThat(od.getTotal()).isEqualTo(3500);
            })
            .verifyComplete();
    }

    @Test
    void getOrderById_returnsAggregatedOrder() {
        Order order = new Order();
        order.setId(6L);
        order.setUserName("bob");
        when(orderRepository.findById(6L)).thenReturn(Mono.just(order));

        OrderItem oi = new OrderItem(1L, 6L, 100L, 3);
        when(orderItemRepository.findByOrderId(6L)).thenReturn(Flux.just(oi));

        ItemDto dto = new ItemDto(); dto.setId(100L); dto.setTitle("Phone"); dto.setPrice(1500);
        when(itemQueryService.getItemById(100L)).thenReturn(Mono.just(dto));

        StepVerifier.create(service.getOrderById(6L))
            .assertNext(od -> {
                assertThat(od.getId()).isEqualTo(6L);
                assertThat(od.getTotal()).isEqualTo(4500);
                assertThat(od.getItems()).hasSize(1);
                assertThat(od.getItems().get(0).getCount()).isEqualTo(3);
            })
            .verifyComplete();
    }

    @Test
    void getMyOrders_filtersByUsername() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just(username));

            Order order = new Order();
            order.setId(9L);
            order.setUserName(username);
            when(orderRepository.findByUserName(username)).thenReturn(Flux.just(order));

            OrderItem oi = new OrderItem(1L, 9L, 200L, 1);
            when(orderItemRepository.findByOrderId(9L)).thenReturn(Flux.just(oi));

            ItemDto dto = new ItemDto(); dto.setId(200L); dto.setTitle("Watch"); dto.setPrice(500);
            when(itemQueryService.getItemById(200L)).thenReturn(Mono.just(dto));

            StepVerifier.create(service.getMyOrders())
                .assertNext(od -> {
                    assertThat(od.getId()).isEqualTo(9L);
                    assertThat(od.getTotal()).isEqualTo(500);
                })
                .verifyComplete();
        }
    }

    @Test
    void getMyOrderById_filtersByUsername() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::currentUsername).thenReturn(Mono.just(username));

            Order order = new Order();
            order.setId(11L);
            order.setUserName(username);
            when(orderRepository.findByIdAndUserName(11L, username)).thenReturn(Mono.just(order));

            OrderItem oi = new OrderItem(1L, 11L, 100L, 2);
            when(orderItemRepository.findByOrderId(11L)).thenReturn(Flux.just(oi));

            ItemDto dto = new ItemDto(); dto.setId(100L); dto.setTitle("Phone"); dto.setPrice(1500);
            when(itemQueryService.getItemById(100L)).thenReturn(Mono.just(dto));

            StepVerifier.create(service.getMyOrderById(11L))
                .assertNext(od -> {
                    assertThat(od.getId()).isEqualTo(11L);
                    assertThat(od.getTotal()).isEqualTo(3000);
                })
                .verifyComplete();
        }
    }
}
