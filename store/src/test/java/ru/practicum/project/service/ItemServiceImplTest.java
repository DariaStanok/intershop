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
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.exсeption.ItemNotFoundException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private CartLineRepository cartLineRepository;
    @Mock private ItemQueryService itemQueryService;

    private final ModelMapper modelMapper = new ModelMapper();
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, cartLineRepository, modelMapper, itemQueryService);
    }

    @Test
    void shouldReturnItemDtoById() {
        Item item = new Item(1L, "Phone", "Desc", 999, "img.jpg", 0);
        ItemDto dto = modelMapper.map(item, ItemDto.class);
        when(itemQueryService.getItemById(1L)).thenReturn(Mono.just(dto));

        StepVerifier.create(itemService.getItemById(1L, 2))
            .assertNext(res -> {
                assertThat(res.getId()).isEqualTo(1L);
                assertThat(res.getCount()).isEqualTo(2);
                assertThat(res.getTitle()).isEqualTo("Phone");
            })
            .verifyComplete();
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        when(itemQueryService.getItemById(99L)).thenReturn(Mono.empty());

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

        when(itemRepository.findByTitleContainingIgnoreCase(""))
            .thenReturn(Flux.fromIterable(List.of(item1, item2, item3, item4)));

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