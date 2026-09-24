package com.stocklms.stock_lms.dto;

import com.stocklms.stock_lms.entity.TradeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request payload to execute a stock trade.
 * Price is automatically fetched from the latest PriceHistory.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Stock ID is required")
    private Long stockId;

    @NotNull(message = "Trade type is required (BUY or SELL)")
    private TradeType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1 share")
    private Integer quantity;
}
