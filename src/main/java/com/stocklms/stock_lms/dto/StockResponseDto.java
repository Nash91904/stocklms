package com.stocklms.stock_lms.dto;

import lombok.*;

/**
 * Response payload representing a stock equity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockResponseDto {

    private Long id;
    private String ticker;
    private String name;
    private String sector;
}
