package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
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

    // User who attempted the quiz (lazy loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Lesson the quiz belongs to (lazy loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Lesson lesson;

    // Score achieved on the quiz (e.g. 0 to 100)
    @Column(nullable = false)
    private Integer score;

    // Timestamp when the quiz was taken
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
