package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an achievement badge awarded to a user for reaching milestones.
 */
@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to User (stored as Long for Week 1)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Type or identifier of badge (e.g., FIRST_TRADE, DIVERSIFIED_5, STREAK_7)
    @Column(name = "badge_type", nullable = false, length = 50)
    private String badgeType;

    // Timestamp when the badge was earned
    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;
}
