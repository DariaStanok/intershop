package ru.practicum.project.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;

public class CartUtils {

	private static final String CART_ID = "cartId";
	private static final String SELECTION_ATTR = "selection";

    private CartUtils() {
    }

    public static Mono<Long> getOrCreateCartId(WebSession session) {
        Object value = session.getAttribute(CART_ID);
        if (value instanceof Long cartId) {
            return Mono.just(cartId);
        }
        long newId = generateCartId();
        session.getAttributes().put(CART_ID, newId);
        return Mono.just(newId);
    }

    public static Map<Long, Integer> getSelection(WebSession session) {
        Object attr = session.getAttribute(SELECTION_ATTR);
        if (attr instanceof Map<?, ?> rawMap) {
            try {
                return (Map<Long, Integer>) rawMap;
            } catch (ClassCastException e) {
                Map<Long, Integer> newMap = new HashMap<>();
                session.getAttributes().put(SELECTION_ATTR, newMap);
                return newMap;
            }
        } else {
            Map<Long, Integer> newMap = new HashMap<>();
            session.getAttributes().put(SELECTION_ATTR, newMap);
            return newMap;
        }
    }

    public static void clearSelection(WebSession session) {
        session.getAttributes().remove(SELECTION_ATTR);
    }

    private static long generateCartId() {
        return System.currentTimeMillis(); 
    }
}
