package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a stock/equity available in the simulated market.
 */
@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stock ticker symbol (e.g., AAPL, MSFT, GOOGL), unique and uppercase
    @Column(nullable = false, unique = true, length = 10)
    private String ticker;

    // Company full name
    @Column(nullable = false, length = 150)
    private String name;

    // Industry sector (e.g., Technology, Financial Services, Healthcare)
    @Column(length = 60)
    private String sector;
}
