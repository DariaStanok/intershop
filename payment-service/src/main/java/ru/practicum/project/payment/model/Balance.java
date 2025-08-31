package ru.practicum.project.payment.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("balances")
public class Balance {
	@Id
	Long id;
	String username;
	String currency;
	long amount;
}
