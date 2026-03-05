package com.beertestshop.controller;

import com.beertestshop.dto.ProductDto;
import com.beertestshop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST контроллер для управления продуктами.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API для управления продуктами")
public class ProductController {

    private final ProductService productService;

    /**
     * Получить все продукты.
     */
    @GetMapping
    @Operation(summary = "Получить все продукты", description = "Возвращает список всех активных продуктов")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.debug("Getting all products");
        List<ProductDto> products = productService.findAllActive();
        return ResponseEntity.ok(products);
    }

    /**
     * Получить все продукты включая неактивные (для склада).
     * Должен быть объявлен перед /{id} для корректной работы.
     */
    @GetMapping("/all")
    @Operation(summary = "Получить все продукты включая неактивные", description = "Возвращает список всех продуктов для администрирования")
    public ResponseEntity<List<ProductDto>> getAllProductsIncludingInactive() {
        log.debug("Getting all products including inactive");
        List<ProductDto> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    /**
     * Получить продукт по ID.
     * Паттерн \\\\d+ обеспечивает обработку только числовых ID, чтобы /all не перехватывался.
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Получить продукт по ID", description = "Возвращает продукт по идентификатору")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        log.debug("Getting product by id: {}", id);
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создать новый продукт (ADMIN).
     */
    @PostMapping
    @Operation(summary = "Создать продукт", description = "Создает новый продукт (только ADMIN)")
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestBody ProductDto productDto) {
        log.info("Creating new product: {}", productDto.getName());
        ProductDto created = productService.save(productDto);
        return ResponseEntity.ok(created);
    }

    /**
     * Обновить продукт (ADMIN).
     */
    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Обновить продукт", description = "Обновляет информацию о продукте (только ADMIN)")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        log.info("Updating product with id: {}", id);
        try {
            ProductDto updated = productService.update(id, productDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удалить продукт (ADMIN).
     */
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Удалить продукт", description = "Удаляет продукт по ID (только ADMIN)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product with id: {}", id);
        if (productService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Обновить статус активности продукта (ADMIN).
     */
    @PutMapping("/{id:\\d+}/toggle-status")
    @Operation(summary = "Переключить статус активности продукта", description = "Включает/выключает продукт (только ADMIN)")
    public ResponseEntity<ProductDto> toggleProductStatus(@PathVariable Long id) {
        log.info("Toggling product status for id: {}", id);
        return productService.findById(id)
                .map(product -> {
                    ProductDto updated = productService.update(id, ProductDto.builder()
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .quantity(product.getQuantity())
                            .isActive(!product.getIsActive())
                            .imageUrl(product.getImageUrl())
                            .build());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
