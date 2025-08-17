package ru.practicum.project.security.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("app_user")
public class AppUser {
	@Id
	private Long id;
	private String username;
	private String password; 
	private boolean enabled;
}
