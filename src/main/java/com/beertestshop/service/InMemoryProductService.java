package com.beertestshop.service;

import com.beertestshop.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис для управления продуктами с хранением данных в памяти.
 * Использует ConcurrentHashMap для потокобезопасности.
 */
@Slf4j
@Service
public class InMemoryProductService {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Сохранить продукт.
     */
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        products.put(product.getId(), product);
        log.debug("Product saved: {}", product);
        return product;
    }

    /**
     * Найти продукт по ID.
     */
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    /**
     * Получить все продукты.
     */
    public Map<Long, Product> findAll() {
        return Map.copyOf(products);
    }

    /**
     * Получить все активные продукты.
     */
    public List<Product> findAllActive() {
        return products.values().stream()
                .filter(Product::getIsActive)
                .toList();
    }

    /**
     * Удалить продукт по ID.
     */
    public boolean delete(Long id) {
        return products.remove(id) != null;
    }

    /**
     * Обновить количество продукта.
     */
    public Product updateQuantity(Long id, int quantity) {
        return findById(id).map(product -> {
            product.setQuantity(quantity);
            return save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Уменьшить количество продукта (при покупке).
     */
    public synchronized Product decreaseQuantity(Long id, int amount) {
        return findById(id).map(product -> {
            int newQuantity = product.getQuantity() - amount;
            if (newQuantity < 0) {
                throw new RuntimeException("Not enough quantity for product: " + product.getName());
            }
            product.setQuantity(newQuantity);
            return save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Активировать/деактивировать продукт.
     */
    public Product setActive(Long id, boolean isActive) {
        return findById(id).map(product -> {
            product.setIsActive(isActive);
            return save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Инициализировать тестовые данные - 5 сортов пива.
     */
    public void initTestData() {
        log.info("Initializing products...");

        List<Product> beers = new ArrayList<>();

        beers.add(Product.builder()
                .name("Light Lager")
                .description("Легкий светлый лагер с мягким вкусом и низкой горечью. Идеален для жаркого дня.")
                .price(new BigDecimal("150.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&h=300&fit=crop")
                .build());

        beers.add(Product.builder()
                .name("Amber Ale")
                .description("Янтарный эль с карамельными нотками и умеренной хмелевой горечью.")
                .price(new BigDecimal("180.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1566633806327-68e152aaf26d?w=400&h=300&fit=crop")
                .build());

        beers.add(Product.builder()
                .name("India Pale Ale")
                .description("IPA с ярким хмелевым ароматом, цитрусовыми нотами и выраженной горечью.")
                .price(new BigDecimal("220.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1535958636474-b021ee8876a3?w=400&h=300&fit=crop")
                .build());

        beers.add(Product.builder()
                .name("Dark Stout")
                .description("Тёмный стаут с кофейными и шоколадными оттенками, плотным телом.")
                .price(new BigDecimal("200.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1518176258769-f227c798150e?w=400&h=300&fit=crop")
                .build());

        beers.add(Product.builder()
                .name("Wheat Beer")
                .description("Пшеничное пиво с освежающим вкусом, нотами банана и гвоздики.")
                .price(new BigDecimal("170.00"))
                .quantity(2)
                .isActive(true)
                .imageUrl("https://images.unsplash.com/photo-1584225064785-c62a8b43d148?w=400&h=300&fit=crop")
                .build());

        beers.forEach(this::save);
        log.info("Products initialized: {} beer sorts", beers.size());
    }
}
