package com.beertestshop.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * JPA-сущность корзины пользователя.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carts")
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CartItemEntity> items;

    /**
     * Добавить товар в корзину.
     */
    public void addItem(CartItemEntity item) {
        item.setCart(this);
        if (this.items == null) {
            this.items = new HashSet<>();
        }
        this.items.add(item);
    }

    /**
     * Удалить товар из корзины.
     */
    public void removeItem(CartItemEntity item) {
        item.setCart(null);
        if (this.items != null) {
            this.items.remove(item);
        }
    }

    /**
     * Очистить корзину.
     */
    public void clear() {
        if (this.items != null) {
            this.items.forEach(item -> item.setCart(null));
            this.items.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartEntity)) return false;
        CartEntity that = (CartEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
