package com.beertestshop.controller;

import com.beertestshop.dto.FeedbackDto;
import com.beertestshop.exception.ResourceNotFoundException;
import com.beertestshop.model.Feedback;
import com.beertestshop.service.InMemoryFeedbackService;
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

import java.time.LocalDateTime;
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

    private final InMemoryFeedbackService feedbackService;

    /**
     * Получить все отзывы.
     */
    @GetMapping
    @Operation(summary = "Получить все отзывы", description = "Возвращает список всех отзывов")
    public ResponseEntity<List<FeedbackDto>> getAllFeedback() {
        log.debug("Getting all feedback");

        List<FeedbackDto> feedbackList = feedbackService.findAllAsList().stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Получить отзыв по ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить отзыв по ID", description = "Возвращает отзыв по идентификатору")
    public ResponseEntity<FeedbackDto> getFeedbackById(@PathVariable Long id) {
        log.debug("Getting feedback by id: {}", id);

        Feedback feedback = feedbackService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));

        return ResponseEntity.ok(toDto(feedback));
    }

    /**
     * Создать новый отзыв (PUBLIC).
     */
    @PostMapping
    @Operation(summary = "Создать отзыв", description = "Создает новый отзыв (доступно всем)")
    public ResponseEntity<FeedbackDto> createFeedback(@Valid @RequestBody FeedbackDto feedbackDto) {
        log.info("Creating new feedback from: {}", feedbackDto.getAuthorName());

        Feedback feedback = Feedback.builder()
                .authorName(feedbackDto.getAuthorName())
                .authorEmail(feedbackDto.getAuthorEmail())
                .message(feedbackDto.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        Feedback created = feedbackService.save(feedback);
        log.info("Feedback created successfully with id: {}", created.getId());

        return ResponseEntity.ok(toDto(created));
    }

    /**
     * Обновить отзыв (ADMIN).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить отзыв", description = "Обновляет отзыв (только ADMIN)")
    public ResponseEntity<FeedbackDto> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackDto feedbackDto) {
        log.info("Updating feedback with id: {}", id);

        Feedback existingFeedback = feedbackService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));

        existingFeedback.setAuthorName(feedbackDto.getAuthorName());
        existingFeedback.setAuthorEmail(feedbackDto.getAuthorEmail());
        existingFeedback.setMessage(feedbackDto.getMessage());

        Feedback updated = feedbackService.save(existingFeedback);
        log.info("Feedback updated successfully: {}", updated.getId());

        return ResponseEntity.ok(toDto(updated));
    }

    /**
     * Удалить отзыв (ADMIN).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв по ID (только ADMIN)")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        log.info("Deleting feedback with id: {}", id);

        if (!feedbackService.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Feedback", id);
        }

        feedbackService.delete(id);
        log.info("Feedback deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Конвертировать Feedback в FeedbackDto.
     */
    private FeedbackDto toDto(Feedback feedback) {
        return FeedbackDto.builder()
                .id(feedback.getId())
                .authorName(feedback.getAuthorName())
                .authorEmail(feedback.getAuthorEmail())
                .message(feedback.getMessage())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
