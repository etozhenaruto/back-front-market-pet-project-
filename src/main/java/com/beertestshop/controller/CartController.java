package com.beertestshop.controller;

import com.beertestshop.dto.AddToCartRequest;
import com.beertestshop.dto.CartDto;
import com.beertestshop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для управления корзиной.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API для управления корзиной")
public class CartController {

    private final CartService cartService;

    // Используем фиксированный ID для корзины (без авторизации)
    private static final Long DEFAULT_CART_ID = 1L;

    /**
     * Получить текущую корзину.
     */
    @GetMapping
    @Operation(summary = "Получить корзину", description = "Возвращает корзину")
    public ResponseEntity<CartDto> getCart() {
        log.debug("Getting cart");
        CartDto cart = cartService.getCartByUserId(DEFAULT_CART_ID);
        return ResponseEntity.ok(cart);
    }

    /**
     * Добавить товар в корзину.
     */
    @PostMapping("/add")
    @Operation(summary = "Добавить товар в корзину", description = "Добавляет товар в корзину")
    public ResponseEntity<CartDto> addToCart(@Valid @RequestBody AddToCartRequest request) {
        log.info("Adding {} items of product {} to cart", request.getQuantity(), request.getProductId());

        CartDto cart = cartService.addItem(DEFAULT_CART_ID, request.getProductId(), request.getQuantity());
        log.info("Cart updated: {} items", cart.getItems().size());
        return ResponseEntity.ok(cart);
    }

    /**
     * Удалить товар из корзины.
     */
    @DeleteMapping("/remove")
    @Operation(summary = "Удалить товар из корзины", description = "Удаляет товар из корзины по ID продукта")
    public ResponseEntity<CartDto> removeFromCart(@RequestParam Long productId) {
        log.info("Removing product {} from cart", productId);

        CartDto cart = cartService.removeItem(DEFAULT_CART_ID, productId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Очистить корзину.
     */
    @DeleteMapping("/clear")
    @Operation(summary = "Очистить корзину", description = "Очищает корзину")
    public ResponseEntity<CartDto> clearCart() {
        log.info("Clearing cart");

        CartDto cart = cartService.clearCart(DEFAULT_CART_ID);
        return ResponseEntity.ok(cart);
    }
}
