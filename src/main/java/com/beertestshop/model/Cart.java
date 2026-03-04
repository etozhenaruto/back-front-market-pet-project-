package com.beertestshop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Модель корзины пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    private Long userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    /**
     * Добавить товар в корзину.
     */
    public void addItem(CartItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    /**
     * Удалить товар из корзины по ID продукта.
     */
    public void removeItem(Long productId) {
        if (this.items != null) {
            this.items.removeIf(item -> item.getProductId().equals(productId));
        }
    }

    /**
     * Очистить корзину.
     */
    public void clear() {
        if (this.items != null) {
            this.items.clear();
        }
    }
}
