package ru.practicum.project.controller;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
	@GetMapping("/debug-template")
    public ResponseEntity<String> debugTemplate() {
        ClassPathResource resource = new ClassPathResource("templates/test.html");
        try {
            if (resource.exists()) {
                return ResponseEntity.ok("Файл найден: " + resource.getURI());
            } else {
                return ResponseEntity.status(404).body("Файл не найден");
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }
}
