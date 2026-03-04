package com.beertestshop.controller;

import com.beertestshop.dto.ProductDto;
import com.beertestshop.exception.ResourceNotFoundException;
import com.beertestshop.model.Product;
import com.beertestshop.service.InMemoryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST контроллер для управления продуктами.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API для управления продуктами")
public class ProductController {

    private final InMemoryProductService productService;

    /**
     * Получить все продукты.
     */
    @GetMapping
    @Operation(summary = "Получить все продукты", description = "Возвращает список всех продуктов")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.debug("Getting all products");

        List<ProductDto> products = productService.findAll().values().stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(products);
    }

    /**
     * Получить активные продукты.
     */
    @GetMapping("/active")
    @Operation(summary = "Получить активные продукты", description = "Возвращает список активных продуктов")
    public ResponseEntity<List<ProductDto>> getActiveProducts() {
        log.debug("Getting active products");

        List<ProductDto> products = productService.findAllActive().stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(products);
    }

    /**
     * Получить продукт по ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить продукт по ID", description = "Возвращает продукт по идентификатору")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        log.debug("Getting product by id: {}", id);

        Product product = productService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        return ResponseEntity.ok(toDto(product));
    }

    /**
     * Обновить продукт (ADMIN).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить продукт", description = "Обновляет информацию о продукте (только ADMIN)")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        log.info("Updating product with id: {}", id);

        Product existingProduct = productService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setQuantity(productDto.getQuantity());
        existingProduct.setIsActive(productDto.getIsActive());

        Product updated = productService.save(existingProduct);
        log.info("Product updated successfully: {}", updated.getId());

        return ResponseEntity.ok(toDto(updated));
    }

    /**
     * Обновить статус активности продукта (ADMIN).
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Переключить статус активности продукта", description = "Включает/выключает продукт (только ADMIN)")
    public ResponseEntity<ProductDto> toggleProductStatus(@PathVariable Long id) {
        log.info("Toggling product status for id: {}", id);

        Product product = productService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Product updated = productService.setActive(id, !product.getIsActive());
        log.info("Product status toggled: {} -> {}", id, updated.getIsActive());

        return ResponseEntity.ok(toDto(updated));
    }

    /**
     * Конвертировать Product в ProductDto.
     */
    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .isActive(product.getIsActive())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
