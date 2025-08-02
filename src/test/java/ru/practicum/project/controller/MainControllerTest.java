package ru.practicum.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.Item;
import ru.practicum.project.service.CartService;
import ru.practicum.project.service.ItemService;

@WebMvcTest(MainController.class)
class MainControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;
    
    @Test
    void redirectsToMainTest() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/main/items"));
    }
    
    @Test
    void mainPageWithModelAttributesTest() throws Exception {
      
        List<List<ItemDto>> rows = List.of();
        Page<Item> itemPage = new PageImpl<>(List.of());

        when(cartService.getCartItems(any())).thenReturn(List.of());
        when(itemService.getItems(anyString(), any(), anyInt(), anyInt(), any())).thenReturn(rows);
        when(itemService.fetchItemPage(anyString(), any(), anyInt(), anyInt())).thenReturn(itemPage);

        mockMvc.perform(get("/main/items")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items", "pageNumber", "pageSize", "hasNext", "hasPrevious", "search", "sort"));
    }
    
    @Test
    void updateCartItemTest() throws Exception {
        mockMvc.perform(post("/main/items/1")
                        .param("action", "plus")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items?search=&sort=NO&pageSize=10&pageNumber=1"));

        verify(cartService).updateItem(any(), eq(1L), eq(CartAction.PLUS));
    }


}
