package com.beertestshop.service;

import com.beertestshop.dto.CartDto;
import com.beertestshop.entity.CartEntity;
import com.beertestshop.entity.CartItemEntity;
import com.beertestshop.entity.ProductEntity;
import com.beertestshop.repository.CartRepository;
import com.beertestshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Тесты для CartService - проверка бизнес-логики управления корзиной.
 * 
 * Тестовые кейсы:
 * - Получение корзины пользователя
 * - Добавление товара в корзину
 * - Удаление товара из корзины
 * - Очистка корзины
 * - Валидация количества товара
 * - Проверка остатков на складе
 * - Возврат товара на склад при удалении
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private CartEntity testCart;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        // Тестовый товар
        testProduct = ProductEntity.builder()
                .id(1L)
                .name("Test Lager")
                .description("Test Description")
                .price(new BigDecimal("150.00"))
                .quantity(10)
                .isActive(true)
                .imageUrl("https://example.com/test.jpg")
                .build();

        // Тестовая корзина
        testCart = CartEntity.builder()
                .userId(1L)
                .items(new HashSet<>())
                .build();
    }

    // ==================== getCartByUserId() ====================

    @Test
    @DisplayName("TC-501: Получение корзины - корзина существует")
    void getCartByUserId_CartExists() {
        // Arrange
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));

        // Act
        CartDto result = cartService.getCartByUserId(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(cartRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("TC-502: Получение корзины - корзина не существует (возвращается пустая)")
    void getCartByUserId_CartNotExists() {
        // Arrange
        given(cartRepository.findByUserId(1L)).willReturn(Optional.empty());

        // Act
        CartDto result = cartService.getCartByUserId(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalQuantity()).isZero();
    }

    // ==================== addItem() ====================

    @Test
    @DisplayName("TC-503: Добавление товара в корзину - успех (новый товар)")
    void addItem_NewItem_Success() {
        // Arrange
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(cartRepository.save(any(CartEntity.class))).willReturn(testCart);
        given(productRepository.save(any(ProductEntity.class))).willReturn(testProduct);

        // Act
        CartDto result = cartService.addItem(1L, 1L, 2);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).hasSize(1);
        assertThat(testProduct.getQuantity()).isEqualTo(8); // 10 - 2
        verify(productRepository, times(1)).save(any(ProductEntity.class));
    }

    @Test
    @DisplayName("TC-504: Добавление товара - товар не найден")
    void addItem_ProductNotFound() {
        // Arrange
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, 999L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Товар не найден");
    }

    @Test
    @DisplayName("TC-505: Добавление товара - нулевое количество")
    void addItem_ZeroQuantity() {
        // Arrange
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Количество должно быть больше 0");
    }

    @Test
    @DisplayName("TC-506: Добавление товара - отрицательное количество")
    void addItem_NegativeQuantity() {
        // Arrange
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, 1L, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Количество должно быть больше 0");
    }

    @Test
    @DisplayName("TC-507: Добавление товара - недостаточно на складе")
    void addItem_InsufficientStock() {
        // Arrange
        testProduct.setQuantity(3); // На складе только 3 шт
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 5)) // Пытаемся добавить 5
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно товара на складе");
    }

    @Test
    @DisplayName("TC-508: Добавление товара - корзина не существует (создается новая)")
    void addItem_CartNotExists_CreateNew() {
        // Arrange
        CartEntity newCart = CartEntity.builder()
                .userId(1L)
                .items(new HashSet<>())
                .build();
        given(cartRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(cartRepository.save(any(CartEntity.class))).willReturn(newCart);
        given(productRepository.save(any(ProductEntity.class))).willReturn(testProduct);

        // Act
        CartDto result = cartService.addItem(1L, 1L, 2);

        // Assert
        assertThat(result).isNotNull();
        // save вызывается 2 раза: создание корзины и обновление после добавления товара
        verify(cartRepository, times(2)).save(any(CartEntity.class));
        verify(productRepository, times(1)).save(any(ProductEntity.class));
    }

    @Test
    @DisplayName("TC-509: Добавление товара - увеличение количества существующего")
    void addItem_ExistingItem_IncreaseQuantity() {
        // Arrange
        CartItemEntity existingItem = CartItemEntity.builder()
                .productId(1L)
                .quantity(1)
                .build();
        testCart.getItems().add(existingItem);
        
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(cartRepository.save(any(CartEntity.class))).willReturn(testCart);
        given(productRepository.save(any(ProductEntity.class))).willReturn(testProduct);

        // Act
        CartDto result = cartService.addItem(1L, 1L, 2); // Добавляем еще 2

        // Assert
        assertThat(result).isNotNull();
        assertThat(existingItem.getQuantity()).isEqualTo(3); // 1 + 2
        assertThat(testProduct.getQuantity()).isEqualTo(8); // 10 - 2
    }

    @Test
    @DisplayName("TC-510: Добавление товара - превышение остатка при добавлении к существующему")
    void addItem_ExistingItem_ExceedsStock() {
        // Arrange
        CartItemEntity existingItem = CartItemEntity.builder()
                .productId(1L)
                .quantity(8)
                .build();
        testCart.getItems().add(existingItem);
        testProduct.setQuantity(2); // На складе осталось только 2 шт
        
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        // Act & Assert - пытаемся добавить еще 3, но на складе только 2
        // Ожидаем IllegalArgumentException
        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно товара на складе");
    }

    // ==================== removeItem() ====================

    @Test
    @DisplayName("TC-511: Удаление товара из корзины - успех")
    void removeItem_Success() {
        // Arrange
        CartItemEntity itemToRemove = CartItemEntity.builder()
                .productId(1L)
                .quantity(2)
                .build();
        testCart.getItems().add(itemToRemove);
        
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(cartRepository.save(any(CartEntity.class))).willReturn(testCart);
        given(productRepository.save(any(ProductEntity.class))).willReturn(testProduct);

        // Act
        CartDto result = cartService.removeItem(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).isEmpty();
        assertThat(testProduct.getQuantity()).isEqualTo(12); // 10 + 2 (возврат на склад)
    }

    @Test
    @DisplayName("TC-512: Удаление товара - корзина не найдена")
    void removeItem_CartNotFound() {
        // Arrange
        given(cartRepository.findByUserId(1L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.removeItem(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart not found");
    }

    @Test
    @DisplayName("TC-513: Удаление товара - товар не в корзине (возвращается текущее состояние)")
    void removeItem_ItemNotInCart() {
        // Arrange
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));
        given(cartRepository.save(any(CartEntity.class))).willReturn(testCart);

        // Act
        CartDto result = cartService.removeItem(1L, 999L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).isEmpty();
    }

    // ==================== clearCart() ====================

    @Test
    @DisplayName("TC-514: Очистка корзины - успех")
    void clearCart_Success() {
        // Arrange
        CartItemEntity item1 = CartItemEntity.builder()
                .productId(1L)
                .quantity(2)
                .build();
        testCart.getItems().add(item1);
        
        ProductEntity product1 = ProductEntity.builder()
                .id(1L)
                .name("Product 1")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .isActive(true)
                .build();
        
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(product1));
        given(cartRepository.save(any(CartEntity.class))).willReturn(testCart);
        given(productRepository.save(any(ProductEntity.class))).willReturn(product1);

        // Act
        CartDto result = cartService.clearCart(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).isEmpty();
        assertThat(product1.getQuantity()).isEqualTo(7); // 5 + 2 (возврат)
    }

    @Test
    @DisplayName("TC-515: Очистка корзины - корзина не найдена")
    void clearCart_CartNotFound() {
        // Arrange
        given(cartRepository.findByUserId(1L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.clearCart(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart not found");
    }

    @Test
    @DisplayName("TC-516: Очистка корзины - возврат всех товаров на склад")
    void clearCart_ReturnAllItemsToStock() {
        // Arrange
        CartItemEntity item1 = CartItemEntity.builder().productId(1L).quantity(2).build();
        CartItemEntity item2 = CartItemEntity.builder().productId(2L).quantity(3).build();
        testCart.getItems().add(item1);
        testCart.getItems().add(item2);
        
        ProductEntity product1 = ProductEntity.builder().id(1L).name("P1").price(new BigDecimal("100")).quantity(5).isActive(true).build();
        ProductEntity product2 = ProductEntity.builder().id(2L).name("P2").price(new BigDecimal("200")).quantity(10).isActive(true).build();
        
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(testCart));
        given(productRepository.findById(1L)).willReturn(Optional.of(product1));
        given(productRepository.findById(2L)).willReturn(Optional.of(product2));
        given(cartRepository.save(any(CartEntity.class))).willReturn(testCart);
        given(productRepository.save(any(ProductEntity.class))).willReturn(product1, product2);

        // Act
        CartDto result = cartService.clearCart(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).isEmpty();
        assertThat(product1.getQuantity()).isEqualTo(7); // 5 + 2
        assertThat(product2.getQuantity()).isEqualTo(13); // 10 + 3
    }
}
