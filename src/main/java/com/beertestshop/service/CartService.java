package com.beertestshop.service;

import com.beertestshop.dto.CartDto;
import com.beertestshop.dto.CartItemDto;
import com.beertestshop.entity.CartEntity;
import com.beertestshop.entity.CartItemEntity;
import com.beertestshop.repository.CartRepository;
import com.beertestshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления корзиной с хранением данных в PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    private static final int MAX_ITEM_QUANTITY = 5;

    /**
     * Получить корзину пользователя.
     */
    @Transactional(readOnly = true)
    public CartDto getCartByUserId(Long userId) {
        log.debug("Getting cart for user: {}", userId);
        return cartRepository.findByUserId(userId)
                .map(this::toDTO)
                .orElse(CartDto.builder()
                        .userId(userId)
                        .items(List.of())
                        .totalQuantity(0)
                        .totalPrice(BigDecimal.ZERO)
                        .build());
    }

    /**
     * Добавить товар в корзину.
     */
    @Transactional
    public CartDto addItem(Long userId, Long productId, Integer quantity) {
        log.debug("Adding product {} to cart of user {} with quantity {}", productId, userId, quantity);

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден с id: " + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Недостаточно товара на складе. Доступно: " + product.getQuantity() + " шт.");
        }

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CartEntity newCart = CartEntity.builder()
                            .userId(userId)
                            .items(new java.util.HashSet<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        CartItemEntity existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException("Недостаточно товара на складе. Доступно: " + product.getQuantity() + " шт.");
            }
            existingItem.setQuantity(newQuantity);
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            cart.addItem(newItem);
        }

        // Уменьшаем количество товара на складе
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        CartEntity saved = cartRepository.save(cart);
        log.info("Product {} added to cart of user {}", productId, userId);
        return toDTO(saved);
    }

    /**
     * Удалить товар из корзины.
     */
    @Transactional
    public CartDto removeItem(Long userId, Long productId) {
        log.debug("Removing product {} from cart of user {}", productId, userId);

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        var itemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            var item = itemOpt.get();
            // Возвращаем товар на склад
            var product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
            
            cart.removeItem(item);
        }

        CartEntity saved = cartRepository.save(cart);
        log.info("Product {} removed from cart of user {}", productId, userId);
        return toDTO(saved);
    }

    /**
     * Очистить корзину.
     */
    @Transactional
    public CartDto clearCart(Long userId) {
        log.debug("Clearing cart for user: {}", userId);

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        // Возвращаем все товары на склад
        for (var item : cart.getItems()) {
            var product = productRepository.findById(item.getProductId())
                    .orElse(null);
            if (product != null) {
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        cart.clear();
        CartEntity saved = cartRepository.save(cart);
        log.info("Cart cleared for user {}", userId);
        return toDTO(saved);
    }

    private CartDto toDTO(CartEntity entity) {
        List<CartItemDto> itemDTOs = new ArrayList<>(entity.getItems()).stream()
                .map(this::itemToDTO)
                .collect(Collectors.toList());

        int totalQuantity = itemDTOs.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();

        BigDecimal totalPrice = itemDTOs.stream()
                .map(item -> item.getPrice() != null ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.builder()
                .userId(entity.getUserId())
                .items(itemDTOs)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .build();
    }

    private CartItemDto itemToDTO(CartItemEntity entity) {
        var product = productRepository.findById(entity.getProductId()).orElse(null);
        
        BigDecimal price = product != null ? product.getPrice() : BigDecimal.ZERO;
        String productName = product != null ? product.getName() : "Товар удалён";
        BigDecimal total = price.multiply(BigDecimal.valueOf(entity.getQuantity()));

        return CartItemDto.builder()
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .productName(productName)
                .price(price)
                .total(total)
                .build();
    }
}
