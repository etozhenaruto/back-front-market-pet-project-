package com.beertestshop.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Модель продукта (пива).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;

    @NotBlank(message = "Название продукта не может быть пустым")
    private String name;

    private String description;

    @NotNull(message = "Цена не может быть пустой")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    private BigDecimal price;

    @NotNull(message = "Количество не может быть пустым")
    @Min(value = 0, message = "Количество должно быть неотрицательным")
    private Integer quantity;

    @NotNull(message = "Статус активности не может быть пустым")
    private Boolean isActive;

    private String imageUrl;
}
