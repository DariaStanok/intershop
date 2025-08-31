package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;
import ru.practicum.project.config.PostgresR2dbcTestBase;
import ru.practicum.project.model.Item;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;

@DataR2dbcTest
@ActiveProfiles("test")
class OrderRepositoryTest extends PostgresR2dbcTestBase {

    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired ItemRepository itemRepository;

    Long orderId;

    @BeforeEach
    void seed() {
        Item item = new Item(null, "Phone", "d", 1000, "img", 0);
        Long itemId = itemRepository.save(item).map(Item::getId).block();

        Order o = new Order(null, "alice");
        orderId = orderRepository.save(o).map(Order::getId).block();

        orderItemRepository.save(new OrderItem(null, orderId, itemId, 3)).block();
    }

    @Test
    void findByUserName_and_findByIdAndUserName_and_findOrderItems() {
        StepVerifier.create(orderRepository.findByUserName("alice").collectList())
            .assertNext(list -> {
                assertThat(list).hasSize(1);
                assertThat(list.get(0).getId()).isEqualTo(orderId);
            }).verifyComplete();

        StepVerifier.create(orderRepository.findByIdAndUserName(orderId, "alice"))
            .assertNext(o -> assertThat(o.getId()).isEqualTo(orderId))
            .verifyComplete();

        StepVerifier.create(orderItemRepository.findByOrderId(orderId).collectList())
            .assertNext(items -> {
                assertThat(items).hasSize(1);
                assertThat(items.get(0).getCount()).isEqualTo(3);
            }).verifyComplete();
    }
}
