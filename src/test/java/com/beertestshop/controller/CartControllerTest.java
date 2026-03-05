package com.beertestshop.controller;

import com.beertestshop.dto.AddToCartRequest;
import com.beertestshop.dto.CartDto;
import com.beertestshop.dto.CartItemDto;
import com.beertestshop.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для CartController - проверка всех эндпоинтов управления корзиной.
 * 
 * Тестовые кейсы:
 * - Получение корзины
 * - Добавление товара в корзину
 * - Удаление товара из корзины
 * - Очистка корзины
 * - Валидация данных (количество товара, ID продукта)
 * - Обработка ошибок (товар не найден, недостаточно на складе)
 */
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartDto emptyCart;
    private CartDto cartWithItems;

    @BeforeEach
    void setUp() {
        // Пустая корзина
        emptyCart = CartDto.builder()
                .userId(1L)
                .items(List.of())
                .totalQuantity(0)
                .totalPrice(BigDecimal.ZERO)
                .build();

        // Корзина с товарами
        CartItemDto item1 = CartItemDto.builder()
                .productId(1L)
                .productName("Light Lager")
                .quantity(2)
                .price(new BigDecimal("150.00"))
                .total(new BigDecimal("300.00"))
                .build();

        CartItemDto item2 = CartItemDto.builder()
                .productId(2L)
                .productName("Amber Ale")
                .quantity(1)
                .price(new BigDecimal("180.00"))
                .total(new BigDecimal("180.00"))
                .build();

        cartWithItems = CartDto.builder()
                .userId(1L)
                .items(List.of(item1, item2))
                .totalQuantity(3)
                .totalPrice(new BigDecimal("480.00"))
                .build();
    }

    // ==================== GET /api/v1/cart ====================

    @Test
    @DisplayName("TC-201: Получение корзины - пустая корзина")
    void getCart_Empty() throws Exception {
        // Arrange
        given(cartService.getCartByUserId(1L)).willReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        // Verify
        verify(cartService, times(1)).getCartByUserId(1L);
    }

    @Test
    @DisplayName("TC-202: Получение корзины - корзина с товарами")
    void getCart_WithItems() throws Exception {
        // Arrange
        given(cartService.getCartByUserId(1L)).willReturn(cartWithItems);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(480.00))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Light Lager"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    // ==================== POST /api/v1/cart/add ====================

    @Test
    @DisplayName("TC-203: Добавление товара в корзину - успех")
    void addToCart_Success() throws Exception {
        // Arrange
        AddToCartRequest request = new AddToCartRequest(1L, 2);
        given(cartService.addItem(eq(1L), eq(1L), eq(2))).willReturn(cartWithItems);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(480.00));

        // Verify
        verify(cartService, times(1)).addItem(1L, 1L, 2);
    }

    @Test
    @DisplayName("TC-204: Добавление товара - валидация: нулевое количество")
    void addToCart_Validation_ZeroQuantity() throws Exception {
        // Arrange
        AddToCartRequest request = new AddToCartRequest(1L, 0);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-205: Добавление товара - валидация: отрицательное количество")
    void addToCart_Validation_NegativeQuantity() throws Exception {
        // Arrange
        AddToCartRequest request = new AddToCartRequest(1L, -5);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-206: Добавление товара - ошибка: товар не найден")
    void addToCart_ProductNotFound() throws Exception {
        // Arrange
        AddToCartRequest request = new AddToCartRequest(999L, 1);
        given(cartService.addItem(eq(1L), eq(999L), eq(1)))
                .willThrow(new IllegalArgumentException("Товар не найден с id: 999"));

        // Act & Assert - IllegalArgumentException обрабатывается как 400 Bad Request
        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-207: Добавление товара - ошибка: недостаточно товара на складе")
    void addToCart_InsufficientStock() throws Exception {
        // Arrange
        AddToCartRequest request = new AddToCartRequest(1L, 100);
        given(cartService.addItem(eq(1L), eq(1L), eq(100)))
                .willThrow(new IllegalArgumentException("Недостаточно товара на складе. Доступно: 5 шт."));

        // Act & Assert - IllegalArgumentException обрабатывается как 400 Bad Request
        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE /api/v1/cart/remove ====================

    @Test
    @DisplayName("TC-208: Удаление товара из корзины - успех")
    void removeFromCart_Success() throws Exception {
        // Arrange
        given(cartService.removeItem(1L, 1L)).willReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/remove")
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalQuantity").value(0));

        // Verify
        verify(cartService, times(1)).removeItem(1L, 1L);
    }

    @Test
    @DisplayName("TC-209: Удаление товара - ошибка: корзина не найдена")
    void removeFromCart_CartNotFound() throws Exception {
        // Arrange
        given(cartService.removeItem(1L, 999L))
                .willThrow(new RuntimeException("Cart not found for user: 1"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/remove")
                        .param("productId", "999"))
                .andExpect(status().is5xxServerError());
    }

    // ==================== DELETE /api/v1/cart/clear ====================

    @Test
    @DisplayName("TC-210: Очистка корзины - успех")
    void clearCart_Success() throws Exception {
        // Arrange
        given(cartService.clearCart(1L)).willReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));

        // Verify
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @DisplayName("TC-211: Очистка корзины - ошибка: корзина не найдена")
    void clearCart_CartNotFound() throws Exception {
        // Arrange
        given(cartService.clearCart(1L))
                .willThrow(new RuntimeException("Cart not found for user: 1"));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/clear"))
                .andExpect(status().is5xxServerError());
    }
}
