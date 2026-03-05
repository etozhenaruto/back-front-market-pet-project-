package com.beertestshop.service;

import com.beertestshop.dto.FeedbackDto;
import com.beertestshop.entity.FeedbackEntity;
import com.beertestshop.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления отзывами с хранением данных в PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    /**
     * Получить все отзывы.
     */
    @Transactional(readOnly = true)
    public List<FeedbackDto> findAll() {
        log.debug("Getting all feedbacks");
        return feedbackRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получить отзыв по ID.
     */
    @Transactional(readOnly = true)
    public Optional<FeedbackDto> findById(Long id) {
        log.debug("Getting feedback by id: {}", id);
        return feedbackRepository.findById(id).map(this::toDTO);
    }

    /**
     * Сохранить отзыв.
     */
    @Transactional
    public FeedbackDto save(FeedbackDto feedbackDTO) {
        log.debug("Saving feedback from: {}", feedbackDTO.getAuthorName());
        FeedbackEntity entity = toEntity(feedbackDTO);
        // Устанавливаем createdAt если не установлено
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(java.time.LocalDateTime.now());
        }
        FeedbackEntity saved = feedbackRepository.save(entity);
        log.info("Feedback saved with id: {}", saved.getId());
        return toDTO(saved);
    }

    /**
     * Обновить отзыв.
     */
    @Transactional
    public FeedbackDto update(Long id, FeedbackDto feedbackDTO) {
        log.debug("Updating feedback with id: {}", id);
        return feedbackRepository.findById(id)
                .map(existing -> {
                    existing.setAuthorName(feedbackDTO.getAuthorName());
                    existing.setAuthorEmail(feedbackDTO.getAuthorEmail());
                    existing.setMessage(feedbackDTO.getMessage());
                    FeedbackEntity saved = feedbackRepository.save(existing);
                    return toDTO(saved);
                })
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
    }

    /**
     * Удалить отзыв по ID.
     */
    @Transactional
    public boolean delete(Long id) {
        log.debug("Deleting feedback with id: {}", id);
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Инициализировать тестовые данные.
     */
    @Transactional
    public void initTestData() {
        log.info("Initializing feedbacks...");

        if (feedbackRepository.count() > 0) {
            log.info("Feedbacks already exist, skipping initialization");
            return;
        }

        FeedbackEntity feedback1 = FeedbackEntity.builder()
                .authorName("Иван Петров")
                .authorEmail("ivan@example.com")
                .message("Отличное пиво! Особенно понравился India Pale Ale с цитрусовыми нотами. Обязательно закажу ещё.")
                .build();
        feedbackRepository.save(feedback1);

        FeedbackEntity feedback2 = FeedbackEntity.builder()
                .authorName("Мария Сидорова")
                .authorEmail("maria@example.com")
                .message("Быстрая доставка и хорошее качество. Wheat Beer просто превосходный, рекомендую всем любителям пшеничного пива.")
                .build();
        feedbackRepository.save(feedback2);

        log.info("Feedbacks initialized: 2 items");
    }

    private FeedbackDto toDTO(FeedbackEntity entity) {
        return FeedbackDto.builder()
                .id(entity.getId())
                .authorName(entity.getAuthorName())
                .authorEmail(entity.getAuthorEmail())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private FeedbackEntity toEntity(FeedbackDto dto) {
        return FeedbackEntity.builder()
                .id(dto.getId())
                .authorName(dto.getAuthorName())
                .authorEmail(dto.getAuthorEmail())
                .message(dto.getMessage())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
