package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.exeption.ItemNotFoundException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartLineRepository cartLineRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, cartLineRepository, modelMapper);
    }

    @Test
    void shouldReturnItemDtoById() {
        Item item = new Item(1L, "Phone", "Desc", 999, "img.jpg", 0);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));

        StepVerifier.create(itemService.getItemById(1L, 2))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getCount()).isEqualTo(2);
                assertThat(dto.getTitle()).isEqualTo("Phone");
            })
            .verifyComplete();
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItemById(99L, 1))
            .expectError(ItemNotFoundException.class)
            .verify();
    }

    @Test
    void shouldReturnPagedAndSortedItemRows() {
        Item item1 = new Item(1L, "A", "desc", 100, "img1", 0);
        Item item2 = new Item(2L, "B", "desc", 200, "img2", 0);
        Item item3 = new Item(3L, "C", "desc", 150, "img3", 0);
        Item item4 = new Item(4L, "D", "desc", 300, "img4", 0);

        List<Item> allItems = List.of(item1, item2, item3, item4);

        when(itemRepository.findByTitleContainingIgnoreCase(""))
            .thenReturn(Flux.fromIterable(allItems));

        when(cartLineRepository.findByCartId(1L))
            .thenReturn(Flux.just(
                new CartLine(null, 2, 1L, 1L),
                new CartLine(null, 1, 1L, 3L)
            ));

        StepVerifier.create(itemService.getItems("", SortType.PRICE, 1, 4, 1L))
        .assertNext(rows -> {
            assertThat(rows).hasSize(2); 
            assertThat(rows.get(0)).hasSize(3);
            assertThat(rows.get(1)).hasSize(1);
            assertThat(rows.get(0).get(0).getId()).isEqualTo(1L);
            assertThat(rows.get(0).get(0).getCount()).isEqualTo(2);
            assertThat(rows.get(0).get(1).getId()).isEqualTo(3L); 
            assertThat(rows.get(0).get(1).getCount()).isEqualTo(1);
            assertThat(rows.get(0).get(2).getId()).isEqualTo(2L); 
            assertThat(rows.get(0).get(2).getCount()).isEqualTo(0);
            assertThat(rows.get(1).get(0).getId()).isEqualTo(4L); 
            assertThat(rows.get(1).get(0).getCount()).isEqualTo(0);
        })
        .verifyComplete();
}

    @Test
    void shouldReturnEmptyMapWhenCartIdIsNull() {
        Item item = new Item(1L, "Book", "desc", 100, "img", 0);

        when(itemRepository.findByTitleContainingIgnoreCase("book"))
            .thenReturn(Flux.just(item));

        StepVerifier.create(itemService.getItems("book", SortType.NO, 1, 3, null))
            .assertNext(rows -> {
                assertThat(rows).hasSize(1);
                assertThat(rows.get(0)).hasSize(1);
                assertThat(rows.get(0).get(0).getCount()).isEqualTo(0);
            })
            .verifyComplete();
    }

    @Test
    void shouldThrowWhenNoItemsFoundAfterFiltering() {
        when(itemRepository.findByTitleContainingIgnoreCase("xyz"))
            .thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems("xyz", SortType.NO, 1, 3, null))
            .expectError(ItemNotFoundException.class)
            .verify();
    }
}
