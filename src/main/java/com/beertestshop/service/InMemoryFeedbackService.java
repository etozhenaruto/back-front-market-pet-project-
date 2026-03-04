package com.beertestshop.service;

import com.beertestshop.model.Feedback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис для управления отзывами с хранением данных в памяти.
 * Использует ConcurrentHashMap для потокобезопасности.
 */
@Slf4j
@Service
public class InMemoryFeedbackService {

    private final Map<Long, Feedback> feedbacks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Сохранить отзыв.
     */
    public Feedback save(Feedback feedback) {
        if (feedback.getId() == null) {
            feedback.setId(idGenerator.getAndIncrement());
        }
        if (feedback.getCreatedAt() == null) {
            feedback.setCreatedAt(LocalDateTime.now());
        }
        feedbacks.put(feedback.getId(), feedback);
        log.debug("Feedback saved: {}", feedback);
        return feedback;
    }

    /**
     * Найти отзыв по ID.
     */
    public Optional<Feedback> findById(Long id) {
        return Optional.ofNullable(feedbacks.get(id));
    }

    /**
     * Получить все отзывы.
     */
    public Map<Long, Feedback> findAll() {
        return Map.copyOf(feedbacks);
    }

    /**
     * Получить все отзывы как список (отсортированный по дате создания).
     */
    public List<Feedback> findAllAsList() {
        return new ArrayList<>(feedbacks.values());
    }

    /**
     * Удалить отзыв по ID.
     */
    public boolean delete(Long id) {
        return feedbacks.remove(id) != null;
    }

    /**
     * Найти отзывы по email автора.
     */
    public List<Feedback> findByAuthorEmail(String email) {
        return feedbacks.values().stream()
                .filter(f -> f.getAuthorEmail().equals(email))
                .toList();
    }

    /**
     * Получить количество отзывов.
     */
    public long count() {
        return feedbacks.size();
    }

    /**
     * Инициализировать тестовые данные.
     */
    public void initTestData() {
        log.info("Initializing feedbacks...");

        Feedback feedback1 = Feedback.builder()
                .authorName("Иван Петров")
                .authorEmail("ivan@example.com")
                .message("Отличное пиво! Особенно понравился India Pale Ale с цитрусовыми нотами. Обязательно закажу ещё.")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        Feedback feedback2 = Feedback.builder()
                .authorName("Мария Сидорова")
                .authorEmail("maria@example.com")
                .message("Быстрая доставка и хорошее качество. Wheat Beer просто превосходный, рекомендую всем любителям пшеничного пива.")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        save(feedback1);
        save(feedback2);

        log.info("Feedbacks initialized: {} items", 2);
    }
}
