package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
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

    // Owning side of One-to-One relationship with User (lazy loaded, write-only from JSON)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Current consecutive days of activity
    @Min(value = 0, message = "Current streak cannot be negative")
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    // Highest consecutive days record achieved by user
    @Min(value = 0, message = "Longest streak cannot be negative")
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak;

    // Date when user was last active on the platform
    @PastOrPresent(message = "Last active date cannot be in the future")
    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    // Available streak freezes to protect streak on missed days
    @Min(value = 0, message = "Freeze count cannot be negative")
    @Column(name = "freeze_count", nullable = false)
    private Integer freezeCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setUserId(Long userId) {
        if (userId != null) {
            this.user = User.builder().id(userId).build();
        }
    }

    /**
     * Records a day of activity and updates the streak counters.
     * - Same day as lastActiveDate  → no-op (already counted today)
     * - Exactly one day after       → extend the streak
     * - Any gap larger than one day → reset streak to 1
     * freezeCount is intentionally left untouched here.
     */
    public void recordActivity(LocalDate today) {
        if (today.equals(lastActiveDate)) {
            return; // already recorded today
        }
        if (lastActiveDate != null && lastActiveDate.plusDays(1).equals(today)) {
            currentStreak = currentStreak + 1;
        } else {
            currentStreak = 1;
        }
        lastActiveDate = today;
        longestStreak = Math.max(longestStreak, currentStreak);
    }
}
