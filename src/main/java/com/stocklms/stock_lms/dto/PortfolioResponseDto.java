package com.stocklms.stock_lms.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Response payload representing a user's portfolio and cash balance.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponseDto {

    private Long id;
    private Long userId;
    private BigDecimal cashBalance;
}
