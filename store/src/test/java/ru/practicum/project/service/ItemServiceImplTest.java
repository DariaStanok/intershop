package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.exception.ItemNotFoundException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.security.config.CartAccessGuard;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartLineRepository cartLineRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    @Mock
    private ItemQueryService itemQueryService;

    @Mock
    private CartAccessGuard cartAccessGuard;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item1;
    private Item item2;
    private ItemDto dto1;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, cartLineRepository, modelMapper, itemQueryService, cartAccessGuard);

        item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Alpha");
        item1.setPrice(200);

        item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Beta");
        item2.setPrice(100);

        dto1 = new ItemDto();
        dto1.setId(1L);
        dto1.setTitle("Alpha");
        dto1.setPrice(200);
    }

    @Test
    void getItems_returnsPagedSortedAndWithCounts() {
        when(itemRepository.findByTitleContainingIgnoreCase("a"))
            .thenReturn(Flux.just(item1, item2) 
                .sort(Comparator.comparing(Item::getId))); 
        Long cartId = 10L;
        when(cartAccessGuard.requireOwner(cartId)).thenReturn(Mono.empty());
        when(cartLineRepository.findByCartId(cartId))
            .thenReturn(Flux.just(
                new CartLine(100L, 3, cartId, 1L), 
                new CartLine(101L, 1, cartId, 2L) 
            ));

   
        StepVerifier.create(itemService.getItems("a", SortType.PRICE, 1, 2, cartId))
            .assertNext(rows -> {
                assertThat(rows).hasSize(1);
                List<ItemDto> firstRow = rows.get(0);
                assertThat(firstRow).hasSize(2);
                assertThat(firstRow.get(0).getId()).isEqualTo(2L);
                assertThat(firstRow.get(0).getCount()).isEqualTo(1);
                assertThat(firstRow.get(1).getId()).isEqualTo(1L);
                assertThat(firstRow.get(1).getCount()).isEqualTo(3);
            })
            .verifyComplete();
    }

    @Test
    void getItems_emptyResult_throwsItemNotFound() {
        when(itemRepository.findByTitleContainingIgnoreCase("zzz")).thenReturn(Flux.empty());
        StepVerifier.create(itemService.getItems("zzz", SortType.NO, 1, 10, null))
            .expectError(ItemNotFoundException.class)
            .verify();
    }

    @Test
    void getItems_withoutCartId_setsCountToZero() {
        when(itemRepository.findByTitleContainingIgnoreCase("a"))
            .thenReturn(Flux.just(item1, item2));

        StepVerifier.create(itemService.getItems("a", SortType.ABC, 1, 5, null))
            .assertNext(rows -> {
                List<ItemDto> all = rows.stream().flatMap(List::stream).toList();
                assertThat(all).hasSize(2);
                assertThat(all.get(0).getCount()).isZero();
                assertThat(all.get(1).getCount()).isZero();
            })
            .verifyComplete();
    }

    @Test
    void getItemById_returnsDtoWithCount() {
        when(itemQueryService.getItemById(1L)).thenReturn(Mono.just(dto1));

        StepVerifier.create(itemService.getItemById(1L, 5))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getTitle()).isEqualTo("Alpha");
                assertThat(dto.getCount()).isEqualTo(5);
            })
            .verifyComplete();
    }

    @Test
    void getItemById_whenServiceEmpty_throwsItemNotFound() {
        when(itemQueryService.getItemById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItemById(1L, 1))
            .expectError(ItemNotFoundException.class)
            .verify();
    }
}
