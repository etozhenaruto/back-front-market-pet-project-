package com.beertestshop.repository;

import com.beertestshop.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с отзывами.
 */
@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
}
