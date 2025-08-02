package ru.practicum.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
@Table("items")
public class Item {

    @Id
    private Long id;

    private String title;

    @Column("description")
    private String description;

    private int price;

    @Column("img_path")
    private String imgPath;

    @Transient
    private int count;
}
