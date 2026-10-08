package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an executed buy or sell transaction in the simulated market.
 * Rule: Trade is immutable (audit ledger, no update or delete).
 */
@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who executed the trade (lazy loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Stock that was traded (lazy loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stock stock;

    // Transaction type: BUY or SELL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TradeType type;

    // Number of shares transacted (positive integer)
    @Column(nullable = false)
    private Integer quantity;

    // Execution price per share at the time of trade
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Timestamp when the trade occurred
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
