package ru.practicum.project.repository;


import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import ru.practicum.project.model.Item;

@Repository
public interface ItemRepository extends R2dbcRepository <Item, Long> {

	Flux<Item> findByTitleContainingIgnoreCase(String name);
}
