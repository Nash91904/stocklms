package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    // Hashed password (never exposed via JSON)
    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Plain text password received only on registration/update, never serialized
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Inferred risk profile (e.g. CONSERVATIVE, MODERATE, AGGRESSIVE)
    @Column(name = "risk_profile", length = 50)
    private String riskProfile;

    // Financial literacy score computed algorithmically from quiz & trading behavior
    @Min(value = 0, message = "Literacy score cannot be negative")
    @Column(name = "literacy_score")
    private Integer literacyScore;

    // Timestamp when the account was registered
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // One-to-One inverse side to Portfolio (cascaded, lazy loaded)
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Portfolio portfolio;

    // One-to-One inverse side to Streak (cascaded, lazy loaded)
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Streak streak;

    // Immutable trade audit records (no cascade delete because trades are immutable)
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Trade> trades = new ArrayList<>();

    // Quiz attempts taken by the user
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    // Badges awarded to the user
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Badge> badges = new ArrayList<>();

    // Many-to-Many watchlist linking User and Stock via user_watchlist join table
    @JsonIgnore
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
