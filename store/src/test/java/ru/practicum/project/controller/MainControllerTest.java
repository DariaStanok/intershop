package ru.practicum.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.service.CartService;
import ru.practicum.project.service.ItemService;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private MainController controller;

    @Test
    void index_redirectsToItems() {
        String view = controller.index();
        assertThat(view).isEqualTo("redirect:/main/items");
    }

    @Test
    void getItemsView_populatesModel_andReturnsMainTemplate() {
        String search = "phone";
        SortType sort = SortType.ABC;
        int pageSize = 6;
        int pageNumber = 2;
        Long cartId = 77L;

        ItemDto a = new ItemDto(); a.setId(1L); a.setTitle("Alpha"); a.setPrice(100);
        ItemDto b = new ItemDto(); b.setId(2L); b.setTitle("Beta");  b.setPrice(200);
        List<List<ItemDto>> rows = List.of(List.of(a, b));

        when(itemService.getItems(search, sort, pageNumber, pageSize, cartId))
            .thenReturn(Mono.just(rows));
        Model model = new ConcurrentModel();

        StepVerifier.create(
                controller.getItemsView(search, sort, pageSize, pageNumber, cartId, model)
        )
        .assertNext(view -> {
            assertThat(view).isEqualTo("main");
            assertThat(model.getAttribute("search")).isEqualTo(search);
            assertThat(model.getAttribute("sort")).isEqualTo(sort);
            assertThat(model.getAttribute("pageSize")).isEqualTo(pageSize);
            assertThat(model.getAttribute("pageNumber")).isEqualTo(pageNumber);
            assertThat(model.getAttribute("cartId")).isEqualTo(cartId);

            @SuppressWarnings("unchecked")
            List<List<ItemDto>> items = (List<List<ItemDto>>) model.getAttribute("items");
            assertThat(items).isEqualTo(rows);
        })
        .verifyComplete();

        verify(itemService).getItems(search, sort, pageNumber, pageSize, cartId);
    }

    @Test
    void updateCartFromMain_redirectsAndCallsService() {
        Long itemId = 10L;
        Long cartId = 77L;
        String search = "phone";
        SortType sort = SortType.NO;
        int pageSize = 5;
        int pageNumber = 3;

        when(cartService.updateItem(cartId, itemId, CartAction.PLUS)).thenReturn(Mono.empty());

        StepVerifier.create(
                controller.updateCartFromMain(
                        itemId,
                        "plus",        
                        cartId,
                        search,
                        sort,
                        pageSize,
                        pageNumber
                )
        )
        .assertNext(view -> {
            String expected = String.format(
                "redirect:/main/items?search=%s&sort=%s&pageSize=%d&pageNumber=%d&cartId=%d",
                search, sort, pageSize, pageNumber, cartId
            );
            assertThat(view).isEqualTo(expected);
        })
        .verifyComplete();

        verify(cartService).updateItem(cartId, itemId, CartAction.PLUS);
    }
}
