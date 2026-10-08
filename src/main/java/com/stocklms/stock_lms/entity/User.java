package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a registered user in the system.
 * Table is named 'users' to avoid SQL reserved keyword 'user'.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique user email for identification and login
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    // Hashed password (never stored in plain text)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Inferred risk profile (e.g. CONSERVATIVE, MODERATE, AGGRESSIVE)
    @Column(name = "risk_profile", length = 50)
    private String riskProfile;

    // Financial literacy score computed algorithmically from quiz & trading behavior
    @Column(name = "literacy_score")
    private Integer literacyScore;

    // Timestamp when the account was registered
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // One-to-One inverse side to Portfolio (cascaded, lazy loaded)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Portfolio portfolio;

    // One-to-One inverse side to Streak (cascaded, lazy loaded)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Streak streak;

    // Immutable trade audit records (no cascade delete because trades are immutable)
    @OneToMany(mappedBy = "user")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Trade> trades = new ArrayList<>();

    // Quiz attempts taken by the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    // Badges awarded to the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Badge> badges = new ArrayList<>();

    // Many-to-Many watchlist linking User and Stock via user_watchlist join table
    @ManyToMany
    @JoinTable(
            name = "user_watchlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "stock_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Stock> watchlist = new HashSet<>();

    // Set createdAt automatically before saving if not provided
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
