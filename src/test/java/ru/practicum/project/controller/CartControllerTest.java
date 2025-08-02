package ru.practicum.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.service.CartService;

@WebMvcTest(CartController.class)
class CartControllerTest {
	@Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    private List<ItemDto> mockItems;

	@Test
	void viewCartPage_ShouldReturnCartViewWithItemsAndTotal() throws Exception {
        List<ItemDto> mockItems = List.of();
        when(cartService.getCartItems(any())).thenReturn(mockItems);
        when(cartService.getTotal(any())).thenReturn(200);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"));
    }

}
