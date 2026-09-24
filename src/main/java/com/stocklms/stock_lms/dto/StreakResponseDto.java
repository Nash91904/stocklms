package com.stocklms.stock_lms.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Response payload representing a user's daily engagement streak.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreakResponseDto {

    private Long id;
    private Long userId;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActiveDate;
    private Integer freezeCount;
}
