package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
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
    @Column(nullable = false, unique = true, length = 10)
    private String ticker;

    // Company full name
    @Column(nullable = false, length = 150)
    private String name;

    // Industry sector (e.g., Technology, Financial Services, Healthcare)
    @Column(length = 60)
    private String sector;

    // Historical price snapshots for this stock (immutable ledger)
    @OneToMany(mappedBy = "stock")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PriceHistory> priceHistory = new ArrayList<>();

    // Trades executed for this stock
    @OneToMany(mappedBy = "stock")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Trade> trades = new ArrayList<>();

    // Users who have added this stock to their watchlist (Many-to-Many inverse side)
    @ManyToMany(mappedBy = "watchlist")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> usersWatching = new HashSet<>();
}
