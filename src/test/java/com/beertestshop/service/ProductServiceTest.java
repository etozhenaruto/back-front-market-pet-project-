package com.beertestshop.service;

import com.beertestshop.dto.ProductDto;
import com.beertestshop.entity.ProductEntity;
import com.beertestshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Тесты для ProductService - проверка бизнес-логики управления продуктами.
 * 
 * Тестовые кейсы:
 * - Получение всех активных продуктов
 * - Получение всех продуктов (включая неактивные)
 * - Поиск продукта по ID
 * - Сохранение нового продукта
 * - Обновление существующего продукта
 * - Удаление продукта
 * - Инициализация тестовых данных
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductEntity testProductEntity;
    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProductEntity = ProductEntity.builder()
                .id(1L)
                .name("Test Lager")
                .description("Test Description")
                .price(new BigDecimal("150.00"))
                .quantity(10)
                .isActive(true)
                .imageUrl("https://example.com/test.jpg")
                .build();

        testProductDto = ProductDto.builder()
                .id(1L)
                .name("Test Lager")
                .description("Test Description")
                .price(new BigDecimal("150.00"))
                .quantity(10)
                .isActive(true)
                .imageUrl("https://example.com/test.jpg")
                .build();
    }

    // ==================== findAllActive() ====================

    @Test
    @DisplayName("TC-301: Получение всех активных продуктов - успех")
    void findAllActive_Success() {
        // Arrange
        List<ProductEntity> entities = Arrays.asList(testProductEntity);
        given(productRepository.findByIsActiveTrue()).willReturn(entities);

        // Act
        List<ProductDto> result = productService.findAllActive();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Lager");
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(productRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("TC-302: Получение всех активных продуктов - пустой список")
    void findAllActive_EmptyList() {
        // Arrange
        given(productRepository.findByIsActiveTrue()).willReturn(List.of());

        // Act
        List<ProductDto> result = productService.findAllActive();

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== findAll() ====================

    @Test
    @DisplayName("TC-303: Получение всех продуктов (включая неактивные) - успех")
    void findAll_Success() {
        // Arrange
        ProductEntity inactiveEntity = ProductEntity.builder()
                .id(2L)
                .name("Inactive Beer")
                .description("Inactive")
                .price(new BigDecimal("100.00"))
                .quantity(0)
                .isActive(false)
                .imageUrl("https://example.com/inactive.jpg")
                .build();
        List<ProductEntity> entities = Arrays.asList(testProductEntity, inactiveEntity);
        given(productRepository.findAll()).willReturn(entities);

        // Act
        List<ProductDto> result = productService.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting("isActive").containsExactlyInAnyOrder(true, false);
        verify(productRepository, times(1)).findAll();
    }

    // ==================== findById() ====================

    @Test
    @DisplayName("TC-304: Поиск продукта по ID - успех")
    void findById_Success() {
        // Arrange
        given(productRepository.findById(1L)).willReturn(Optional.of(testProductEntity));

        // Act
        Optional<ProductDto> result = productService.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Lager");
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("TC-305: Поиск продукта по ID - продукт не найден")
    void findById_NotFound() {
        // Arrange
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        // Act
        Optional<ProductDto> result = productService.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== save() ====================

    @Test
    @DisplayName("TC-306: Сохранение нового продукта - успех")
    void save_Success() {
        // Arrange
        ProductEntity savedEntity = ProductEntity.builder()
                .id(1L)
                .name("New Beer")
                .description("New Description")
                .price(new BigDecimal("200.00"))
                .quantity(5)
                .isActive(true)
                .imageUrl("https://example.com/new.jpg")
                .build();
        given(productRepository.save(any(ProductEntity.class))).willReturn(savedEntity);

        // Act
        ProductDto result = productService.save(testProductDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Beer");
        verify(productRepository, times(1)).save(any(ProductEntity.class));
    }

    // ==================== update() ====================

    @Test
    @DisplayName("TC-307: Обновление продукта - успех")
    void update_Success() {
        // Arrange
        ProductEntity existingEntity = ProductEntity.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Description")
                .price(new BigDecimal("100.00"))
                .quantity(5)
                .isActive(true)
                .imageUrl("https://example.com/old.jpg")
                .build();
        given(productRepository.findById(1L)).willReturn(Optional.of(existingEntity));
        given(productRepository.save(any(ProductEntity.class))).willReturn(existingEntity);

        ProductDto updateDto = ProductDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .price(new BigDecimal("250.00"))
                .quantity(15)
                .isActive(false)
                .imageUrl("https://example.com/updated.jpg")
                .build();

        // Act
        ProductDto result = productService.update(1L, updateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(existingEntity.getName()).isEqualTo("Updated Name");
        assertThat(existingEntity.getPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(existingEntity.getQuantity()).isEqualTo(15);
        assertThat(existingEntity.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("TC-308: Обновление продукта - продукт не найден")
    void update_NotFound() {
        // Arrange
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.update(999L, testProductDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ==================== delete() ====================

    @Test
    @DisplayName("TC-309: Удаление продукта - успех")
    void delete_Success() {
        // Arrange
        given(productRepository.existsById(1L)).willReturn(true);

        // Act
        boolean result = productService.delete(1L);

        // Assert
        assertThat(result).isTrue();
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("TC-310: Удаление продукта - продукт не найден")
    void delete_NotFound() {
        // Arrange
        given(productRepository.existsById(999L)).willReturn(false);

        // Act
        boolean result = productService.delete(999L);

        // Assert
        assertThat(result).isFalse();
        verify(productRepository, times(0)).deleteById(999L);
    }

    // ==================== initTestData() ====================

    @Test
    @DisplayName("TC-311: Инициализация тестовых данных - продукты уже существуют")
    void initTestData_ProductsAlreadyExist() {
        // Arrange
        given(productRepository.count()).willReturn(5L);

        // Act
        productService.initTestData();

        // Assert
        verify(productRepository, times(0)).save(any(ProductEntity.class));
    }

    @Test
    @DisplayName("TC-312: Инициализация тестовых данных - создание новых продуктов")
    void initTestData_CreateNewProducts() {
        // Arrange
        given(productRepository.count()).willReturn(0L);
        given(productRepository.save(any(ProductEntity.class))).willReturn(testProductEntity);

        // Act
        productService.initTestData();

        // Assert
        verify(productRepository, times(5)).save(any(ProductEntity.class));
    }
}
