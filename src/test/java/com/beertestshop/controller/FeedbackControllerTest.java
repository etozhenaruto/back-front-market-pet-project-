package com.beertestshop.controller;

import com.beertestshop.dto.FeedbackDto;
import com.beertestshop.service.FeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для FeedbackController - проверка всех эндпоинтов управления отзывами.
 * 
 * Тестовые кейсы:
 * - Получение всех отзывов
 * - Получение отзыва по ID
 * - Создание нового отзыва
 * - Обновление существующего отзыва
 * - Удаление отзыва
 * - Валидация данных (имя, email, текст сообщения)
 * - Обработка ошибок (отзыв не найден)
 */
@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeedbackService feedbackService;

    private FeedbackDto testFeedback;

    @BeforeEach
    void setUp() {
        testFeedback = FeedbackDto.builder()
                .id(1L)
                .authorName("Иван Петров")
                .authorEmail("ivan@example.com")
                .message("Отличный продукт! Очень понравилось.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== GET /api/v1/feedback ====================

    @Test
    @DisplayName("TC-101: Получение всех отзывов - успех")
    void getAllFeedback_Success() throws Exception {
        // Arrange
        List<FeedbackDto> feedbacks = Arrays.asList(testFeedback);
        given(feedbackService.findAll()).willReturn(feedbacks);

        // Act & Assert
        mockMvc.perform(get("/api/v1/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].authorName").value("Иван Петров"))
                .andExpect(jsonPath("$[0].authorEmail").value("ivan@example.com"));

        // Verify
        verify(feedbackService, times(1)).findAll();
    }

    @Test
    @DisplayName("TC-102: Получение всех отзывов - пустой список")
    void getAllFeedback_EmptyList() throws Exception {
        // Arrange
        given(feedbackService.findAll()).willReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== GET /api/v1/feedback/{id} ====================

    @Test
    @DisplayName("TC-103: Получение отзыва по ID - успех")
    void getFeedbackById_Success() throws Exception {
        // Arrange
        given(feedbackService.findById(1L)).willReturn(Optional.of(testFeedback));

        // Act & Assert
        mockMvc.perform(get("/api/v1/feedback/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.authorName").value("Иван Петров"))
                .andExpect(jsonPath("$.message").value("Отличный продукт! Очень понравилось."));
    }

    @Test
    @DisplayName("TC-104: Получение отзыва по ID - отзыв не найден")
    void getFeedbackById_NotFound() throws Exception {
        // Arrange
        given(feedbackService.findById(999L)).willReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/feedback/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/v1/feedback ====================

    @Test
    @DisplayName("TC-105: Создание отзыва - успех")
    void createFeedback_Success() throws Exception {
        // Arrange
        FeedbackDto createdFeedback = FeedbackDto.builder()
                .id(1L)
                .authorName("Новый пользователь")
                .authorEmail("new@example.com")
                .message("Отличный магазин! Быстрая доставка.")
                .createdAt(LocalDateTime.now())
                .build();
        given(feedbackService.save(any(FeedbackDto.class))).willReturn(createdFeedback);

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdFeedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.authorName").value("Новый пользователь"))
                .andExpect(jsonPath("$.authorEmail").value("new@example.com"));

        // Verify
        verify(feedbackService, times(1)).save(any(FeedbackDto.class));
    }

    @Test
    @DisplayName("TC-106: Создание отзыва - валидация: пустое имя")
    void createFeedback_Validation_EmptyName() throws Exception {
        // Arrange
        FeedbackDto invalidFeedback = FeedbackDto.builder()
                .authorName("")
                .authorEmail("test@example.com")
                .message("Good feedback")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeedback)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-107: Создание отзыва - валидация: пустой email")
    void createFeedback_Validation_EmptyEmail() throws Exception {
        // Arrange
        FeedbackDto invalidFeedback = FeedbackDto.builder()
                .authorName("Test User")
                .authorEmail("")
                .message("Good feedback")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeedback)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-108: Создание отзыва - валидация: неверный формат email")
    void createFeedback_Validation_InvalidEmail() throws Exception {
        // Arrange
        FeedbackDto invalidFeedback = FeedbackDto.builder()
                .authorName("Test User")
                .authorEmail("invalid-email")
                .message("Good feedback")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeedback)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-109: Создание отзыва - валидация: короткий текст (< 10 символов)")
    void createFeedback_Validation_ShortMessage() throws Exception {
        // Arrange
        FeedbackDto invalidFeedback = FeedbackDto.builder()
                .authorName("Test User")
                .authorEmail("test@example.com")
                .message("Коротко") // Менее 10 символов
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeedback)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-110: Создание отзыва - валидация: пустое сообщение")
    void createFeedback_Validation_EmptyMessage() throws Exception {
        // Arrange
        FeedbackDto invalidFeedback = FeedbackDto.builder()
                .authorName("Test User")
                .authorEmail("test@example.com")
                .message("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeedback)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/v1/feedback/{id} ====================

    @Test
    @DisplayName("TC-111: Обновление отзыва - успех")
    void updateFeedback_Success() throws Exception {
        // Arrange
        FeedbackDto updatedFeedback = FeedbackDto.builder()
                .id(1L)
                .authorName("Обновленное имя")
                .authorEmail("updated@example.com")
                .message("Обновленный отзыв о продукте")
                .createdAt(LocalDateTime.now())
                .build();
        given(feedbackService.update(eq(1L), any(FeedbackDto.class))).willReturn(updatedFeedback);

        // Act & Assert
        mockMvc.perform(put("/api/v1/feedback/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFeedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.authorName").value("Обновленное имя"))
                .andExpect(jsonPath("$.authorEmail").value("updated@example.com"));
    }

    @Test
    @DisplayName("TC-112: Обновление отзыва - отзыв не найден")
    void updateFeedback_NotFound() throws Exception {
        // Arrange
        FeedbackDto feedback = FeedbackDto.builder()
                .id(999L)
                .authorName("Not Found")
                .authorEmail("notfound@example.com")
                .message("Not found message")
                .build();
        given(feedbackService.update(eq(999L), any(FeedbackDto.class)))
                .willThrow(new RuntimeException("Feedback not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/feedback/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/v1/feedback/{id} ====================

    @Test
    @DisplayName("TC-113: Удаление отзыва - успех")
    void deleteFeedback_Success() throws Exception {
        // Arrange
        given(feedbackService.delete(1L)).willReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/feedback/1"))
                .andExpect(status().isNoContent());

        // Verify
        verify(feedbackService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("TC-114: Удаление отзыва - отзыв не найден")
    void deleteFeedback_NotFound() throws Exception {
        // Arrange
        given(feedbackService.delete(999L)).willReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/feedback/999"))
                .andExpect(status().isNotFound());
    }
}
