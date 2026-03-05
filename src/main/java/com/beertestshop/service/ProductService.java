package com.beertestshop.service;

import com.beertestshop.dto.ProductDto;
import com.beertestshop.entity.ProductEntity;
import com.beertestshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления продуктами с хранением данных в PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Получить все активные продукты.
     */
    @Transactional(readOnly = true)
    public List<ProductDto> findAllActive() {
        log.debug("Getting all active products");
        return productRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить все продукты (включая неактивные).
     */
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        log.debug("Getting all products");
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить продукт по ID.
     */
    @Transactional(readOnly = true)
    public Optional<ProductDto> findById(Long id) {
        log.debug("Getting product by id: {}", id);
        return productRepository.findById(id).map(this::toDTO);
    }

    /**
     * Сохранить продукт.
     */
    @Transactional
    public ProductDto save(ProductDto productDTO) {
        log.debug("Saving product: {}", productDTO);
        ProductEntity entity = toEntity(productDTO);
        ProductEntity saved = productRepository.save(entity);
        return toDTO(saved);
    }

    /**
     * Обновить продукт.
     */
    @Transactional
    public ProductDto update(Long id, ProductDto productDTO) {
        log.debug("Updating product with id: {}", id);
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setName(productDTO.getName());
                    existing.setDescription(productDTO.getDescription());
                    existing.setPrice(productDTO.getPrice());
                    existing.setQuantity(productDTO.getQuantity());
                    existing.setIsActive(productDTO.getIsActive());
                    existing.setImageUrl(productDTO.getImageUrl());
                    ProductEntity saved = productRepository.save(existing);
                    return toDTO(saved);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Удалить продукт по ID.
     */
    @Transactional
    public boolean delete(Long id) {
        log.debug("Deleting product with id: {}", id);
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Инициализировать тестовые данные.
     */
    @Transactional
    public void initTestData() {
        log.info("Initializing products...");

        if (productRepository.count() > 0) {
            log.info("Products already exist, skipping initialization");
            return;
        }

        ProductEntity lightLager = ProductEntity.builder()
                .name("Light Lager")
                .description("Легкий светлый лагер с мягким вкусом и низкой горечью. Идеален для жаркого дня.")
                .price(new java.math.BigDecimal("150.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=300&fit=crop")
                .build();
        productRepository.save(lightLager);

        ProductEntity amberAle = ProductEntity.builder()
                .name("Amber Ale")
                .description("Янтарный эль с карамельными нотками и умеренной хмелевой горечью.")
                .price(new java.math.BigDecimal("180.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1566633806327-68e152aaf26d?w=400&h=300&fit=crop")
                .build();
        productRepository.save(amberAle);

        ProductEntity ipa = ProductEntity.builder()
                .name("India Pale Ale")
                .description("IPA с ярким хмелевым ароматом, цитрусовыми нотами и выраженной горечью.")
                .price(new java.math.BigDecimal("220.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1535958636474-b021ee8876a3?w=400&h=300&fit=crop")
                .build();
        productRepository.save(ipa);

        ProductEntity darkStout = ProductEntity.builder()
                .name("Dark Stout")
                .description("Тёмный стаут с кофейными и шоколадными оттенками, плотным телом.")
                .price(new java.math.BigDecimal("200.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1518176258769-f227c798150e?w=400&h=300&fit=crop")
                .build();
        productRepository.save(darkStout);

        ProductEntity wheatBeer = ProductEntity.builder()
                .name("Wheat Beer")
                .description("Пшеничное пиво с освежающим вкусом, нотами банана и гвоздики.")
                .price(new java.math.BigDecimal("170.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1584225064785-c62a8b43d148?w=400&h=300&fit=crop")
                .build();
        productRepository.save(wheatBeer);

        log.info("Products initialized: 5 beer sorts");
    }

    private ProductDto toDTO(ProductEntity entity) {
        return ProductDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .isActive(entity.getIsActive())
                .imageUrl(entity.getImageUrl())
                .build();
    }

    private ProductEntity toEntity(ProductDto dto) {
        return ProductEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .isActive(dto.getIsActive())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}
