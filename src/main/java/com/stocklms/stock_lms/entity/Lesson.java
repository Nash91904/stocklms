package com.stocklms.stock_lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Educational module/lesson in the Learning Management System (LMS).
 */
@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    // Primary key with auto-increment ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lesson title (e.g., "Introduction to Diversification")
    @Column(nullable = false, length = 150)
    private String title;

    // Educational content formatted as markdown or plain text
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Condition required to unlock this lesson (e.g. MIN_LITERACY_30, PRE_REQ_LESSON_1)
    @Column(name = "unlock_condition", length = 100)
    private String unlockCondition;

    // Quiz attempts associated with this lesson
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuizAttempt> quizAttempts = new ArrayList<>();
}
