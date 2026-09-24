package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Lesson entity.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Find lesson by its unique title
    Optional<Lesson> findByTitle(String title);
}
