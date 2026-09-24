package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request payload for recording a stock price snapshot.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryRequestDto {

    @NotNull(message = "Stock ID is required")
    private Long stockId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Volume is required")
    @Min(value = 0, message = "Volume cannot be negative")
    private Long volume;
}
