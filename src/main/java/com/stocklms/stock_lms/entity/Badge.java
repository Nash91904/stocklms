package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an achievement badge awarded to a user for reaching milestones.
 */
@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who earned the badge (lazy loaded, write-only from JSON)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Type or identifier of badge (e.g., FIRST_TRADE, DIVERSIFIED_5, STREAK_7)
    @NotBlank(message = "Badge type is required")
    @Size(max = 50, message = "Badge type cannot exceed 50 characters")
    @Column(name = "badge_type", nullable = false, length = 50)
    private String badgeType;

    // Timestamp when the badge was earned
    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;

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
