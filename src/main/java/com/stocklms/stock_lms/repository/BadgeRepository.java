package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Badge entity.
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // Retrieve all badges awarded to a specific user
    List<Badge> findByUserId(Long userId);

    // Check if user already holds a specific badge
    boolean existsByUserIdAndBadgeType(Long userId, String badgeType);
}
