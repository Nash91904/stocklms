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

    // User who earned the badge (lazy loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Type or identifier of badge (e.g., FIRST_TRADE, DIVERSIFIED_5, STREAK_7)
    @Column(name = "badge_type", nullable = false, length = 50)
    private String badgeType;

    // Timestamp when the badge was earned
    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;
}
