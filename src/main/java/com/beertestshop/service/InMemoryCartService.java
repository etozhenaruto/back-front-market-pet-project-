package com.beertestshop.service;

import com.beertestshop.dto.AddToCartRequest;
import com.beertestshop.dto.CartDto;
import com.beertestshop.dto.CartItemDto;
import com.beertestshop.model.Cart;
import com.beertestshop.model.CartItem;
import com.beertestshop.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для управления корзинами пользователей с хранением данных в памяти.
 * Использует ConcurrentHashMap для потокобезопасности.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryCartService {

    private static final int MAX_QUANTITY_PER_PRODUCT = 5;

    private final Map<Long, Cart> carts = new ConcurrentHashMap<>();
    private final InMemoryProductService productService;

    /**
     * Получить корзину пользователя или создать новую.
     */
    public Cart getOrCreateCart(Long userId) {
        return carts.computeIfAbsent(userId, id -> {
            Cart cart = new Cart();
            cart.setUserId(id);
            log.debug("Created new cart for user: {}", userId);
            return cart;
        });
    }

    /**
     * Добавить товар в корзину с валидацией.
     * - Нельзя добавить больше, чем есть на складе
     * - Лимит 5 шт одного товара в корзине
     */
    @Transactional
    public synchronized Cart addItem(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Long productId = request.getProductId();
        int requestedQuantity = request.getQuantity();

        // Проверяем продукт
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not active: " + product.getName());
        }

        // Считаем текущее количество этого товара в корзине
        int currentQuantityInCart = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .mapToInt(CartItem::getQuantity)
                .sum();

        int newTotalQuantity = currentQuantityInCart + requestedQuantity;

        // Проверка лимита 5 шт одного товара в корзине
        if (newTotalQuantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new RuntimeException(
                    String.format("Cannot add more than %d items of the same product to cart. " +
                            "Current: %d, Requested: %d", MAX_QUANTITY_PER_PRODUCT, currentQuantityInCart, requestedQuantity)
            );
        }

        // Проверка наличия на складе
        if (requestedQuantity > product.getQuantity()) {
            throw new RuntimeException(
                    String.format("Not enough quantity in stock for product '%s'. " +
                            "Available: %d, Requested: %d", product.getName(), product.getQuantity(), requestedQuantity)
            );
        }

        // Добавляем или обновляем элемент в корзине
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + requestedQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(productId)
                    .quantity(requestedQuantity)
                    .build();
            cart.addItem(newItem);
        }

        log.info("Added {} items of product '{}' to cart for user {}", requestedQuantity, product.getName(), userId);
        return cart;
    }

    /**
     * Удалить товар из корзины.
     */
    public Cart removeItem(Long userId, Long productId) {
        Cart cart = carts.get(userId);
        if (cart != null) {
            cart.removeItem(productId);
            log.debug("Removed product {} from cart for user {}", productId, userId);
        }
        return cart;
    }

    /**
     * Очистить корзину пользователя.
     */
    public Cart clearCart(Long userId) {
        Cart cart = carts.get(userId);
        if (cart != null) {
            cart.clear();
            log.debug("Cleared cart for user {}", userId);
        }
        return cart;
    }

    /**
     * Получить корзину пользователя.
     */
    public Optional<Cart> getCart(Long userId) {
        return Optional.ofNullable(carts.get(userId));
    }

    /**
     * Удалить корзину пользователя.
     */
    public boolean deleteCart(Long userId) {
        return carts.remove(userId) != null;
    }

    /**
     * Получить все корзины.
     */
    public Map<Long, Cart> findAll() {
        return Map.copyOf(carts);
    }

    /**
     * Конвертировать Cart в CartDto.
     */
    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDto> itemDtos = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = productService.findById(item.getProductId()).orElse(null);
            if (product != null) {
                CartItemDto itemDto = CartItemDto.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .productName(product.getName())
                        .price(product.getPrice())
                        .total(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build();
                itemDtos.add(itemDto);
                totalQuantity += item.getQuantity();
                totalPrice = totalPrice.add(itemDto.getTotal());
            }
        }

        return CartDto.builder()
                .userId(cart.getUserId())
                .items(itemDtos)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .build();
    }
}
