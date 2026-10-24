package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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

    // Owning side of One-to-One relationship with User (lazy loaded, write-only from JSON)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Available simulated cash balance for buying stocks
    @NotNull(message = "Cash balance is required")
    @DecimalMin(value = "0.00", message = "Cash balance cannot be negative")
    @Column(name = "cash_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal cashBalance;

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
}
