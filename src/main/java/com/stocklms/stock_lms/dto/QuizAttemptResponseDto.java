package com.stocklms.stock_lms.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a recorded quiz attempt.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResponseDto {

    private Long id;
    private Long userId;
    private Long lessonId;
    private Integer score;
    private LocalDateTime timestamp;
}
