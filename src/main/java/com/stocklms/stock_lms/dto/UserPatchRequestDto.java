package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request payload for partial update (PATCH) of user attributes.
 * Only non-null fields provided will be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPatchRequestDto {

    @Size(max = 50, message = "Risk profile cannot exceed 50 characters")
    private String riskProfile;

    @Min(value = 0, message = "Literacy score cannot be negative")
    private Integer literacyScore;
}
