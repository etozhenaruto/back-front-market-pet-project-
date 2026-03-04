package com.beertestshop.controller;

import com.beertestshop.dto.AddToCartRequest;
import com.beertestshop.dto.CartDto;
import com.beertestshop.exception.ResourceNotFoundException;
import com.beertestshop.model.Cart;
import com.beertestshop.service.InMemoryCartService;
import com.beertestshop.service.InMemoryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    private final InMemoryCartService cartService;
    private final InMemoryUserService userService;

    /**
     * Получить текущую корзину пользователя.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить корзину", description = "Возвращает корзину текущего пользователя")
    public ResponseEntity<CartDto> getCart() {
        Long userId = getCurrentUserId();
        log.debug("Getting cart for user: {}", userId);

        Cart cart = cartService.getCart(userId)
                .orElseGet(() -> cartService.getOrCreateCart(userId));

        return ResponseEntity.ok(cartService.toDto(cart));
    }

    /**
     * Добавить товар в корзину.
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Добавить товар в корзину", description = "Добавляет товар в корзину текущего пользователя")
    public ResponseEntity<CartDto> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        log.info("User {} adding {} items of product {} to cart", userId, request.getQuantity(), request.getProductId());

        Cart cart = cartService.addItem(userId, request);

        log.info("Cart updated for user {}: {} items", userId, cart.getItems().size());
        return ResponseEntity.ok(cartService.toDto(cart));
    }

    /**
     * Удалить товар из корзины.
     */
    @DeleteMapping("/remove")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Удалить товар из корзины", description = "Удаляет товар из корзины по ID продукта")
    public ResponseEntity<CartDto> removeFromCart(Long productId) {
        Long userId = getCurrentUserId();
        log.info("User {} removing product {} from cart", userId, productId);

        Cart cart = cartService.removeItem(userId, productId);

        if (cart == null) {
            cart = cartService.getOrCreateCart(userId);
        }

        return ResponseEntity.ok(cartService.toDto(cart));
    }

    /**
     * Очистить корзину.
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Очистить корзину", description = "Очищает корзину текущего пользователя")
    public ResponseEntity<CartDto> clearCart() {
        Long userId = getCurrentUserId();
        log.info("User {} clearing cart", userId);

        Cart cart = cartService.clearCart(userId);

        if (cart == null) {
            cart = cartService.getOrCreateCart(userId);
        }

        return ResponseEntity.ok(cartService.toDto(cart));
    }

    /**
     * Получить ID текущего авторизованного пользователя.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        String username = authentication.getName();
        return userService.findByUsername(username)
                .map(com.beertestshop.model.User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));
    }
}
