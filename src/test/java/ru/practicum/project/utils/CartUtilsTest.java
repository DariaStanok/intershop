package ru.practicum.project.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;
import ru.practicum.project.util.CartUtils;

class CartUtilsTest {

    private WebSession session;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        session = mock(WebSession.class);
        when(session.getAttribute(anyString())).thenAnswer(inv -> attributes.get(inv.getArgument(0)));
        when(session.getAttributes()).thenReturn(attributes);
    }

    @Test
    void shouldReturnExistingCartId() {
        attributes.put("cartId", 123L);

        Mono<Long> result = CartUtils.getOrCreateCartId(session);

        assertThat(result.block()).isEqualTo(123L);
    }

    @Test
    void shouldGenerateAndStoreNewCartId() {
        Mono<Long> result = CartUtils.getOrCreateCartId(session);

        Long cartId = result.block();
        assertThat(cartId).isNotNull();
        assertThat(attributes).containsEntry("cartId", cartId);
    }

    @Test
    void shouldReturnExistingSelectionMap() {
        Map<Long, Integer> selection = Map.of(1L, 2);
        attributes.put("selection", selection);

        Map<Long, Integer> result = CartUtils.getSelection(session);

        assertThat(result).isEqualTo(selection);
    }

    @Test
    void shouldCreateNewSelectionMapIfNoneExists() {
        Map<Long, Integer> result = CartUtils.getSelection(session);

        assertThat(result).isEmpty();
        assertThat(attributes.get("selection")).isSameAs(result);
    }

    @Test
    void shouldResetSelectionIfAttributeWrongType() {
        attributes.put("selection", "not a map");

        Map<Long, Integer> result = CartUtils.getSelection(session);

        assertThat(result).isEmpty();
        assertThat(attributes.get("selection")).isSameAs(result);
    }

    @Test
    void shouldClearSelection() {
        attributes.put("selection", Map.of(1L, 1));

        CartUtils.clearSelection(session);

        assertThat(attributes).doesNotContainKey("selection");
    }
}
