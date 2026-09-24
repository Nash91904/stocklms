package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request payload for partial update (PATCH) of portfolio cash balance.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioPatchRequestDto {

    @DecimalMin(value = "0.00", message = "Cash balance cannot be negative")
    private BigDecimal cashBalance;
}
