package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    // Stock associated with this price record (lazy loaded, write-only from JSON)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stock stock;

    // Stock price snapshot with 2 decimal places precision
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Trading volume at the snapshot time
    @NotNull(message = "Volume is required")
    @Min(value = 0, message = "Volume cannot be negative")
    @Column(nullable = false)
    private Long volume;

    // Timestamp when the price was recorded
    @Column(nullable = false)
    private LocalDateTime timestamp;

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
