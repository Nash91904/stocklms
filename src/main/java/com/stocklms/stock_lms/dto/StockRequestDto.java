package com.stocklms.stock_lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request payload for creating or updating a stock.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRequestDto {

    @NotBlank(message = "Ticker symbol is required")
    @Size(max = 10, message = "Ticker symbol cannot exceed 10 characters")
    private String ticker;

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String name;

    @Size(max = 60, message = "Sector cannot exceed 60 characters")
    private String sector;
}
