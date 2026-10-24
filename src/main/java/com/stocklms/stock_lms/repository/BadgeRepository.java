package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Badge entity.
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // Retrieve all badges awarded to a specific user
    @Query("SELECT b FROM Badge b WHERE b.user.id = :userId")
    List<Badge> findByUserId(@Param("userId") Long userId);

    // Check if user already holds a specific badge
    @Query("SELECT COUNT(b) > 0 FROM Badge b WHERE b.user.id = :userId AND b.badgeType = :badgeType")
    boolean existsByUserIdAndBadgeType(@Param("userId") Long userId, @Param("badgeType") String badgeType);
}
