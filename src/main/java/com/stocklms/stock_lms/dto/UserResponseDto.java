package com.stocklms.stock_lms.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a user.
 * Sensitive data such as passwordHash is strictly excluded.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String riskProfile;
    private Integer literacyScore;
    private LocalDateTime createdAt;
}
