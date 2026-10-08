package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;

/**
 * Request payload for partial update (PATCH) of user streak attributes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreakPatchRequestDto {

    @Min(value = 0, message = "Current streak cannot be negative")
    private Integer currentStreak;

    @Min(value = 0, message = "Longest streak cannot be negative")
    private Integer longestStreak;

    @PastOrPresent(message = "Last active date cannot be in the future")
    private LocalDate lastActiveDate;

    @Min(value = 0, message = "Freeze count cannot be negative")
    private Integer freezeCount;
}
