package ru.practicum.project.security.model;

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
@Table("app_user")
public class AppUser {
	@Id
	private Long id;
	@Column("username") 
	private String username;
    @Column("password")
	private String password; 
    @Column("enabled")  
	private boolean enabled;
}
