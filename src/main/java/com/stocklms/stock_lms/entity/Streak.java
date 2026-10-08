package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Tracks daily user engagement streak (Duolingo-style mechanic).
 * Driven automatically by platform activity without manual check-ins.
 */
@Entity
@Table(name = "streaks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Streak {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side of One-to-One relationship with User (lazy loaded)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Current consecutive days of activity
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    // Highest consecutive days record achieved by user
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak;

    // Date when user was last active on the platform
    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    // Available streak freezes to protect streak on missed days
    @Column(name = "freeze_count", nullable = false)
    private Integer freezeCount;
}
