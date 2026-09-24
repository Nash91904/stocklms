package com.stocklms.stock_lms.dto;

import com.stocklms.stock_lms.entity.TradeType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response payload representing an executed trade.
 * Trade records are immutable.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeResponseDto {

    private Long id;
    private Long userId;
    private Long stockId;
    private TradeType type;
    private Integer quantity;
    private BigDecimal price;
    private LocalDateTime timestamp;
}
