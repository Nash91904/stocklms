package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    // User who executed the trade (lazy loaded, write-only from JSON)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Stock that was traded (lazy loaded, write-only from JSON)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stock stock;

    // Transaction type: BUY or SELL
    @NotNull(message = "Trade type is required (BUY or SELL)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TradeType type;

    // Number of shares transacted (positive integer)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1 share")
    @Column(nullable = false)
    private Integer quantity;

    // Execution price per share at the time of trade
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Timestamp when the trade occurred
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setUserId(Long userId) {
        if (userId != null) {
            this.user = User.builder().id(userId).build();
        }
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getStockId() {
        return stock != null ? stock.getId() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setStockId(Long stockId) {
        if (stockId != null) {
            this.stock = Stock.builder().id(stockId).build();
        }
    }
}
