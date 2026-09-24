package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request payload for submitting a quiz attempt.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Lesson ID is required")
    private Long lessonId;

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 100, message = "Score cannot exceed 100")
    private Integer score;
}
