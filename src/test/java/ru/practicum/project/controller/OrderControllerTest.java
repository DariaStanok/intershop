package ru.practicum.project.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.model.Cart;
import ru.practicum.project.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
	@Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;
	
    @Test
    void getOrdersReturnView() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void getOrderByIddReturnView() throws Exception {
        OrderDto mockOrder = new OrderDto();
        mockOrder.setId(1L);
        mockOrder.setTotal(200);

        when(orderService.getOrderById(1L)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/1").param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", mockOrder))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void createOrderRedirectToOrderPage() throws Exception {
        OrderDto mockOrder = new OrderDto();
        mockOrder.setId(42L);
        mockOrder.setTotal(300);

        when(orderService.createOrder(any())).thenReturn(mockOrder);

        mockMvc.perform(post("/orders/buy").sessionAttr("cart", new Cart()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/42?new=true"));

        verify(orderService).createOrder(any());
    }
}
