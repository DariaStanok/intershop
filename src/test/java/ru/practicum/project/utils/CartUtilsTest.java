package ru.practicum.project.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpSession;
import ru.practicum.project.model.Cart;
import ru.practicum.project.util.CartUtils;

class CartUtilsTest {

	private HttpSession session;

    @BeforeEach
    void setUp() {
        session = mock(HttpSession.class);
    }

    @Test
    void getOrCreateCartTest() {
        Cart expectedCart = new Cart();
        when(session.getAttribute("cart")).thenReturn(expectedCart);

        Cart result = CartUtils.getOrCreateCart(session);
        assertSame(expectedCart, result);
    }

    @Test
    void getOrCreateNewCartTest() {
        when(session.getAttribute("cart")).thenReturn(null);

        Cart result = CartUtils.getOrCreateCart(session);

        assertNotNull(result);
        verify(session).setAttribute(eq("cart"), any(Cart.class));
    }

    @Test
    void getSelectionTest() {
        Map<Long, Integer> selection = new HashMap<>();
        when(session.getAttribute("selection")).thenReturn(selection);

        Map<Long, Integer> result = CartUtils.getSelection(session);
        assertSame(selection, result);
    }

    @Test
    void getNewSelectionTest() {
        when(session.getAttribute("selection")).thenReturn(null);

        Map<Long, Integer> result = CartUtils.getSelection(session);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).setAttribute(eq("selection"), any(Map.class));
    }

    @Test
    void clearSelectionTest() {
        CartUtils.clearSelection(session);
        verify(session).removeAttribute("selection");
    }

}
