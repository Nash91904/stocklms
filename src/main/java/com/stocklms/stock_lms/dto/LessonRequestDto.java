package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request payload for creating or updating a lesson.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String unlockCondition;
}
