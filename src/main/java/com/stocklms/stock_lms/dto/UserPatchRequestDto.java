package com.stocklms.stock_lms.dto;

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

    private String riskProfile;
    private Integer literacyScore;
}
