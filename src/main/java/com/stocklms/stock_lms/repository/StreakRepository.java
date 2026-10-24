package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Streak entity.
 */
@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    // Retrieve user's streak tracker by user ID
    @Query("SELECT s FROM Streak s WHERE s.user.id = :userId")
    Optional<Streak> findByUserId(@Param("userId") Long userId);

    // Check if user has an existing streak record
    @Query("SELECT COUNT(s) > 0 FROM Streak s WHERE s.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
