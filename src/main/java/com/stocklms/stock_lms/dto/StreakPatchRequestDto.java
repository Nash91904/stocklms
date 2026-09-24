package com.stocklms.stock_lms.dto;

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

    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActiveDate;
    private Integer freezeCount;
}
