package com.beertestshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO для корзины.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private Long userId;

    @Builder.Default
    private List<CartItemDto> items = new ArrayList<>();

    private Integer totalQuantity;

    private BigDecimal totalPrice;
}
