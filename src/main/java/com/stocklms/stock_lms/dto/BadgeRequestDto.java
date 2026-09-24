package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request payload for awarding an achievement badge to a user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Badge type is required")
    private String badgeType;
}
