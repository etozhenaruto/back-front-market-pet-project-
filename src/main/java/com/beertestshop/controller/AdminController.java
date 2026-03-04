package com.beertestshop.controller;

import com.beertestshop.dto.FeedbackDto;
import com.beertestshop.dto.ProductDto;
import com.beertestshop.exception.ResourceNotFoundException;
import com.beertestshop.model.Feedback;
import com.beertestshop.model.Product;
import com.beertestshop.service.InMemoryFeedbackService;
import com.beertestshop.service.InMemoryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * REST контроллер для административных операций.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Административное API (только ADMIN)")
public class AdminController {

    private final InMemoryProductService productService;
    private final InMemoryFeedbackService feedbackService;

    // ==================== Продукты ====================

    /**
     * Получить все продукты (ADMIN).
     */
    @GetMapping("/products")
    @Operation(summary = "Все продукты (ADMIN)", description = "Возвращает все продукты включая неактивные")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.debug("Admin getting all products");

        List<ProductDto> products = productService.findAll().values().stream()
                .map(this::toProductDto)
                .toList();

        return ResponseEntity.ok(products);
    }

    /**
     * Создать новый продукт.
     */
    @PostMapping("/products")
    @Operation(summary = "Создать продукт", description = "Создает новый продукт")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("Admin creating new product: {}", productDto.getName());

        Product product = Product.builder()
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .quantity(productDto.getQuantity())
                .isActive(productDto.getIsActive())
                .build();

        Product created = productService.save(product);
        log.info("Product created successfully with id: {}", created.getId());

        return ResponseEntity.ok(toProductDto(created));
    }

    /**
     * Обновить продукт.
     */
    @PutMapping("/products/{id}")
    @Operation(summary = "Обновить продукт", description = "Обновляет информацию о продукте")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        log.info("Admin updating product with id: {}", id);

        Product existingProduct = productService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setQuantity(productDto.getQuantity());
        existingProduct.setIsActive(productDto.getIsActive());

        Product updated = productService.save(existingProduct);
        log.info("Product updated successfully: {}", updated.getId());

        return ResponseEntity.ok(toProductDto(updated));
    }

    /**
     * Удалить продукт.
     */
    @DeleteMapping("/products/{id}")
    @Operation(summary = "Удалить продукт", description = "Удаляет продукт по ID")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Admin deleting product with id: {}", id);

        if (!productService.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Product", id);
        }

        productService.delete(id);
        log.info("Product deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    // ==================== Отзывы ====================

    /**
     * Получить все отзывы (ADMIN).
     */
    @GetMapping("/feedback")
    @Operation(summary = "Все отзывы (ADMIN)", description = "Возвращает все отзывы")
    public ResponseEntity<List<FeedbackDto>> getAllFeedback() {
        log.debug("Admin getting all feedback");

        List<FeedbackDto> feedbackList = feedbackService.findAllAsList().stream()
                .map(this::toFeedbackDto)
                .toList();

        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Обновить отзыв.
     */
    @PutMapping("/feedback/{id}")
    @Operation(summary = "Обновить отзыв", description = "Обновляет отзыв")
    public ResponseEntity<FeedbackDto> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackDto feedbackDto) {
        log.info("Admin updating feedback with id: {}", id);

        Feedback existingFeedback = feedbackService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));

        existingFeedback.setAuthorName(feedbackDto.getAuthorName());
        existingFeedback.setAuthorEmail(feedbackDto.getAuthorEmail());
        existingFeedback.setMessage(feedbackDto.getMessage());

        Feedback updated = feedbackService.save(existingFeedback);
        log.info("Feedback updated successfully: {}", updated.getId());

        return ResponseEntity.ok(toFeedbackDto(updated));
    }

    /**
     * Удалить отзыв.
     */
    @DeleteMapping("/feedback/{id}")
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв по ID")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        log.info("Admin deleting feedback with id: {}", id);

        if (!feedbackService.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Feedback", id);
        }

        feedbackService.delete(id);
        log.info("Feedback deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    // ==================== Методы конвертации ====================

    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .isActive(product.getIsActive())
                .build();
    }

    private FeedbackDto toFeedbackDto(Feedback feedback) {
        return FeedbackDto.builder()
                .id(feedback.getId())
                .authorName(feedback.getAuthorName())
                .authorEmail(feedback.getAuthorEmail())
                .message(feedback.getMessage())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
