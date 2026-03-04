package com.beertestshop.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Модель отзыва.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    private Long id;

    @NotBlank(message = "Имя автора не может быть пустым")
    @Size(max = 100, message = "Имя автора не может превышать 100 символов")
    private String authorName;

    @NotBlank(message = "Email автора не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String authorEmail;

    @NotBlank(message = "Сообщение не может быть пустым")
    @Size(min = 10, max = 1000, message = "Сообщение должно содержать от 10 до 1000 символов")
    private String message;

    private LocalDateTime createdAt;
}
