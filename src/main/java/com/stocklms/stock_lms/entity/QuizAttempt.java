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

    // Foreign key to User (stored as Long for Week 1)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Foreign key to Lesson (stored as Long for Week 1)
    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    // Score achieved on the quiz (e.g. 0 to 100)
    @Column(nullable = false)
    private Integer score;

    // Timestamp when the quiz was taken
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
