package com.stocklms.stock_lms.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload representing a historical stock price snapshot.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryResponseDto {

    private Long id;
    private Long stockId;
    private BigDecimal price;
    private Long volume;
    private LocalDateTime timestamp;
}
