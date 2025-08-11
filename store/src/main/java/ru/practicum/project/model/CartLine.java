package ru.practicum.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("cart_lines")
public class CartLine {

    @Id
    private Long id;

    @Column("quantity")
    private int quantity;

    @Column("cart_id")
    private Long cartId;

    @Column("item_id")
    private Long itemId;
}
