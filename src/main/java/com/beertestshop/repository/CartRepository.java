package com.beertestshop.repository;

import com.beertestshop.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с корзинами.
 */
@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    @Query("SELECT c FROM CartEntity c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<CartEntity> findByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
