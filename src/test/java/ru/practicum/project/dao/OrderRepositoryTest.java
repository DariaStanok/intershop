package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import ru.practicum.project.model.Item;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=create-drop" })
class OrderRepositoryTest {
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ItemRepository itemRepository;

	private Order order;
	private Item item;

	@BeforeEach
	void setUp() {
		item = new Item();
		item.setTitle("Test item");
		item.setPrice(123);
		itemRepository.save(item);

		order = new Order();
		OrderItem orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setItem(item);
		orderItem.setCount(3);
		order.setItems(List.of(orderItem));
		orderRepository.save(order);
	}

	@Test
	void testFindById() {
		Optional<Order> resultOpt = orderRepository.findById(order.getId());

		assertThat(resultOpt).isPresent();
		Order result = resultOpt.get();

		assertThat(result.getItems()).hasSize(1);
		assertThat(result.getItems().get(0).getItem().getTitle()).isEqualTo("Test item");
		assertThat(result.getItems().get(0).getCount()).isEqualTo(3);
	}

	@Test
	void testFindAll() {
		List<Order> orders = orderRepository.findAll();
		assertThat(orders).isNotEmpty();
		assertThat(orders.get(0).getItems()).hasSize(1);
	}
}
