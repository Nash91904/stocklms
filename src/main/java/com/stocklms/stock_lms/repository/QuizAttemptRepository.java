package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for QuizAttempt entity.
 */
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Retrieve all quiz attempts made by a specific user
    List<QuizAttempt> findByUserId(Long userId);

    // Retrieve attempts by user for a specific lesson
    List<QuizAttempt> findByUserIdAndLessonId(Long userId, Long lessonId);
}
