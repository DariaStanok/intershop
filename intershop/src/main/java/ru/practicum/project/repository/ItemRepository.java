package ru.practicum.project.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.practicum.project.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

	Page<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
