package ru.practicum.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.practicum.project.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{

}
