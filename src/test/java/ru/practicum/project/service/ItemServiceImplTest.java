package ru.practicum.project.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.exeption.ItemNotFoundException;
import ru.practicum.project.model.Cart;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
	@Mock
	private ItemRepository itemRepository;

	private ModelMapper modelMapper;
	private ItemServiceImpl itemService;
	
	private Item item;
    private Cart cart;
    private Page<Item> itemPage;
    
    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        itemService = new ItemServiceImpl(itemRepository, modelMapper); 
        item = new Item();
        item.setId(1L);                         
        item.setTitle("item");                
        item.setPrice(100);
        cart = new Cart();
        CartLine line = new CartLine(1L, 2, cart, item);
        cart.getItems().add(line);
        itemPage = new PageImpl<>(List.of(item));
    }

    @Test
    void getItemsDtoRowsTest() {
        when(itemRepository.findByTitleContainingIgnoreCase(eq(""), any(Pageable.class)))
                .thenReturn(itemPage);

        List<List<ItemDto>> result = itemService.getItems("", SortType.NO, 1, 10, cart);

        assertEquals(1, result.size());
        ItemDto dto = result.get(0).get(0);
        assertEquals("item", dto.getTitle());
        assertEquals(2, dto.getCount());
        assertEquals(100, dto.getPrice());
    }
    
    @Test
    void getItemsEmptyTest() {
        when(itemRepository.findByTitleContainingIgnoreCase(eq(""), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(ItemNotFoundException.class, () ->
                itemService.getItems("", SortType.NO, 1, 10, cart));
    }

    @Test
    void getItemByIdTest() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto dto = itemService.getItemById(1L, 3);
        assertEquals("item", dto.getTitle());
        assertEquals(3, dto.getCount());
    }
    @Test
    void fetchItemPage_ShouldCallRepository() {
        when(itemRepository.findByTitleContainingIgnoreCase(eq("test"), any(Pageable.class)))
                .thenReturn(itemPage);

        Page<Item> result = itemService.fetchItemPage("test", SortType.NO, 1, 10);
        assertEquals(itemPage, result);
        verify(itemRepository).findByTitleContainingIgnoreCase(eq("test"), any(Pageable.class));
    }
}
