package ru.practicum.project.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import ru.practicum.project.model.Order;

@Repository
public interface OrderRepository extends R2dbcRepository <Order, Long>{

}
