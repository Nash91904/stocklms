package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    // Set createdAt automatically before saving if not provided
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
