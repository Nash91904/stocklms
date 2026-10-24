package com.stocklms.stock_lms.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records a user's attempt and score on a lesson quiz.
 */
@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who attempted the quiz (lazy loaded, write-only from JSON)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Lesson the quiz belongs to (lazy loaded, write-only from JSON)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Lesson lesson;

    // Score achieved on the quiz (e.g. 0 to 100)
    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 100, message = "Score cannot exceed 100")
    @Column(nullable = false)
    private Integer score;

    // Timestamp when the quiz was taken
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
    public Long getLessonId() {
        return lesson != null ? lesson.getId() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setLessonId(Long lessonId) {
        if (lessonId != null) {
            this.lesson = Lesson.builder().id(lessonId).build();
        }
    }
}
