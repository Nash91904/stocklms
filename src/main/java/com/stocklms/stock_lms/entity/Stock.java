package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @NotBlank(message = "Ticker symbol is required")
    @Size(max = 10, message = "Ticker symbol cannot exceed 10 characters")
    @Column(nullable = false, unique = true, length = 10)
    private String ticker;

    // Company full name
    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    // Industry sector (e.g., Technology, Financial Services, Healthcare)
    @Size(max = 60, message = "Sector cannot exceed 60 characters")
    @Column(length = 60)
    private String sector;

    // Historical price snapshots for this stock (immutable ledger)
    @JsonIgnore
    @OneToMany(mappedBy = "stock")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PriceHistory> priceHistory = new ArrayList<>();

    // Trades executed for this stock
    @JsonIgnore
    @OneToMany(mappedBy = "stock")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Trade> trades = new ArrayList<>();

    // Users who have added this stock to their watchlist (Many-to-Many inverse side)
    @JsonIgnore
    @ManyToMany(mappedBy = "watchlist")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> usersWatching = new HashSet<>();
}
