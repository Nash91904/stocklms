package com.stocklms.stock_lms.dto;

import lombok.*;

/**
 * Response payload representing an educational lesson.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponseDto {

    private Long id;
    private String title;
    private String content;
    private String unlockCondition;
}
