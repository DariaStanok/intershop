package ru.practicum.project.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.project.model.Cart;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderRepository;

@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {
	@Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Item savedItem;
    
    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();

        Item item = new Item();
        item.setTitle("Test item");
        item.setDescription("Test description");
        item.setPrice(100);
        item.setImgPath("/img.jpg");
        savedItem = itemRepository.save(item);
    }
	@Test
	void createSaveAndRedirectTest() throws Exception {
        Cart cart = new Cart();
        CartLine line = new CartLine(null, 2, cart, savedItem);
        cart.setItems(new ArrayList<>(List.of(line)));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cart", cart);
        mockMvc.perform(post("/orders/buy").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?new=true"));
    }

}
