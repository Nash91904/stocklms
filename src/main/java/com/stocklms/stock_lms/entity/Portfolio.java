package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a user's simulated trading portfolio and cash balance.
 */
@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to User (one portfolio per user, stored as Long for Week 1)
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // Available simulated cash balance for buying stocks
    @Column(name = "cash_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal cashBalance;
}
