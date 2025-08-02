package ru.practicum.project.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.Cart;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
	@Mock
	private ItemRepository itemRepository;

	private ModelMapper modelMapper; 

	private CartServiceImpl cartService;

	private Cart cart;
	private Item item;
	
	@BeforeEach
	void setUp() {
		modelMapper = new ModelMapper();
		cartService = new CartServiceImpl(itemRepository, modelMapper);

		item = new Item();
		item.setId(1L);
		item.setPrice(100);
		item.setTitle("Test item");
		cart = new Cart();
	}
    
    @Test
    void getTotalTest() {
        cart.getItems().add(new CartLine(null, 2, cart, item));
        int total = cartService.getTotal(cart);
        assertEquals(200, total);
    }

    @Test
    void getCartItemsReturnDtoTest() {
        cart.getItems().add(new CartLine(null, 3, cart, item));
        List<ItemDto> result = cartService.getCartItems(cart);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getCount());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void updateItemAddLine() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        cartService.updateItem(cart, 1L, CartAction.ADD);

        assertEquals(1, cart.getItems().size());
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    void updateItemIncrementQuantity() {
        cart.getItems().add(new CartLine(null, 2, cart, item));
        cartService.updateItem(cart, 1L, CartAction.PLUS);

        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void updateItemDecrementQuantity() {
        cart.getItems().add(new CartLine(null, 2, cart, item));
        cartService.updateItem(cart, 1L, CartAction.MINUS);

        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    void updateItemRemoveZeroValue() {
        cart.getItems().add(new CartLine(null, 1, cart, item));
        cartService.updateItem(cart, 1L, CartAction.MINUS);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void updateItemRemoveLine() {
        cart.getItems().add(new CartLine(null, 2, cart, item));
        cartService.updateItem(cart, 1L, CartAction.DELETE);
        assertTrue(cart.getItems().isEmpty());
    }
}
