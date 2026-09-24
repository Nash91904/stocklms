package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Streak entity.
 */
@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    // Retrieve user's streak tracker by user ID
    Optional<Streak> findByUserId(Long userId);

    // Check if user has an existing streak record
    boolean existsByUserId(Long userId);
}
