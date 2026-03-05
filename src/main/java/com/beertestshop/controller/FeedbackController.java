package com.beertestshop.controller;

import com.beertestshop.dto.FeedbackDto;
import com.beertestshop.service.FeedbackService;
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
 * REST контроллер для управления отзывами.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "API для управления отзывами")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Получить все отзывы.
     */
    @GetMapping
    @Operation(summary = "Получить все отзывы", description = "Возвращает список всех отзывов")
    public ResponseEntity<List<FeedbackDto>> getAllFeedback() {
        log.debug("Getting all feedback");
        List<FeedbackDto> feedbackList = feedbackService.findAll();
        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Получить отзыв по ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить отзыв по ID", description = "Возвращает отзыв по идентификатору")
    public ResponseEntity<FeedbackDto> getFeedbackById(@PathVariable Long id) {
        log.debug("Getting feedback by id: {}", id);
        return feedbackService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создать новый отзыв (PUBLIC).
     */
    @PostMapping
    @Operation(summary = "Создать отзыв", description = "Создает новый отзыв (доступно всем)")
    public ResponseEntity<FeedbackDto> createFeedback(@Valid @RequestBody FeedbackDto feedbackDto) {
        log.info("Creating new feedback from: {}", feedbackDto.getAuthorName());
        FeedbackDto created = feedbackService.save(feedbackDto);
        return ResponseEntity.ok(created);
    }

    /**
     * Обновить отзыв (ADMIN).
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить отзыв", description = "Обновляет отзыв (только ADMIN)")
    public ResponseEntity<FeedbackDto> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackDto feedbackDto) {
        log.info("Updating feedback with id: {}", id);
        try {
            FeedbackDto updated = feedbackService.update(id, feedbackDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удалить отзыв (ADMIN).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв по ID (только ADMIN)")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        log.info("Deleting feedback with id: {}", id);
        if (feedbackService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
