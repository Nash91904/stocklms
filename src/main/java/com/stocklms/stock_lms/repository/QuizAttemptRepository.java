package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for QuizAttempt entity.
 */
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Retrieve all quiz attempts made by a specific user
    @Query("SELECT q FROM QuizAttempt q WHERE q.user.id = :userId")
    List<QuizAttempt> findByUserId(@Param("userId") Long userId);

    // Retrieve attempts by user for a specific lesson
    @Query("SELECT q FROM QuizAttempt q WHERE q.user.id = :userId AND q.lesson.id = :lessonId")
    List<QuizAttempt> findByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId);
}
