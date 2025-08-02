package ru.practicum.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("cart_line")
public class CartLine {

    @Id
    private Long id;

    private int quantity;

    @Column("cart_id")
    private Long cartId;

    @Column("item_id")
    private Long itemId;
}
