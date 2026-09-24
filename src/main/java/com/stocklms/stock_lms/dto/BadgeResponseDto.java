package com.stocklms.stock_lms.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing an awarded badge.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponseDto {

    private Long id;
    private Long userId;
    private String badgeType;
    private LocalDateTime awardedAt;
}
