package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) 
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
class ItemRepositoryTest {
	@Autowired
    private ItemRepository itemRepository;

    private Item item1;
    private Item item2;
    
    @BeforeEach
    void setUp() {
        item1 = new Item();
        item1.setTitle("Smartphone");
        item1.setPrice(2000);
        itemRepository.save(item1);
        item2 = new Item();
        item2.setTitle("Smartwatch");
        item2.setPrice(1000);
        itemRepository.save(item2);
    }

	@Test
	void testFindByTitleContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Item> result = itemRepository.findByTitleContainingIgnoreCase("sMaRt", pageable);
        List<Item> items = result.getContent();

        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getTitle)
                         .containsExactlyInAnyOrder("Smartphone", "Smartwatch");
    }

	@Test
	void testNotFindByTitleContainingIgnoreCase() {
		Page<Item> result = itemRepository.findByTitleContainingIgnoreCase("laptop", Pageable.unpaged());
		assertThat(result).isEmpty();
	}

}
