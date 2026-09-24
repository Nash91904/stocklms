package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Historical stock price record populated by automated data ingestion jobs.
 * Rule: PriceHistory is immutable (insert-only, no update or delete).
 */
@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to Stock (stored as Long for Week 1)
    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    // Stock price snapshot with 2 decimal places precision
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Trading volume at the snapshot time
    @Column(nullable = false)
    private Long volume;

    // Timestamp when the price was recorded
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
