package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request payload for full update (PUT) of a user profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    // Optional: if provided, password will be re-hashed
    private String password;

    private String riskProfile;

    private Integer literacyScore;
}
