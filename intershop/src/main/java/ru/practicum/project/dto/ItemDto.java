package ru.practicum.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

	private Long id;
	private String title;
	private String description;
	private String imgPath;
	private int price;
	private int count;
}
