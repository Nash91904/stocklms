package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.*;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.*;
import com.stocklms.stock_lms.responsebean.ResponseBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for recording and viewing user quiz attempts.
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Quiz Attempts", description = "Record and view user quiz attempts")
@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final StreakRepository streakRepository;
    private final BadgeRepository badgeRepository;

    @Operation(summary = "Record a quiz attempt",
               responses = {@ApiResponse(responseCode = "201", description = "Quiz attempt recorded"),
                            @ApiResponse(responseCode = "400", description = "Validation error or lesson locked"),
                            @ApiResponse(responseCode = "404", description = "User or lesson not found")})
    @PostMapping
    @Transactional
    public ResponseEntity<ResponseBean> recordQuizAttempt(@Valid @RequestBody QuizAttempt attempt) {
        Long userId = attempt.getUser() != null ? attempt.getUser().getId() : null;
        Long lessonId = attempt.getLesson() != null ? attempt.getLesson().getId() : null;

        if (userId == null) {
            log.warn("Quiz attempt rejected: User ID is required");
            throw new BusinessRuleException("User ID is required");
        }
        if (lessonId == null) {
            log.warn("Quiz attempt rejected: Lesson ID is required");
            throw new BusinessRuleException("Lesson ID is required");
        }

        // Score range guard (belt-and-suspenders on top of @Min/@Max bean validation)
        if (attempt.getScore() == null || attempt.getScore() < 0 || attempt.getScore() > 100) {
            log.warn("Quiz attempt rejected: score {} is outside 0-100", attempt.getScore());
            throw new BusinessRuleException("Score must be between 0 and 100");
        }

        log.info("Recording quiz attempt – user={} lesson={} score={}", userId, lessonId, attempt.getScore());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        // Unlock condition check: MIN_LITERACY_<n>
        String condition = lesson.getUnlockCondition();
        if (condition != null && condition.startsWith("MIN_LITERACY_")) {
            int required = Integer.parseInt(condition.substring("MIN_LITERACY_".length()));
            int userScore = user.getLiteracyScore() != null ? user.getLiteracyScore() : 0;
            if (userScore < required) {
                log.warn("Lesson {} locked for user {} – requires literacy {}, has {}",
                        lessonId, userId, required, userScore);
                throw new BusinessRuleException(
                        "Lesson is locked: requires literacy score " + required);
            }
        }

        attempt.setUser(user);
        attempt.setLesson(lesson);
        if (attempt.getTimestamp() == null) {
            attempt.setTimestamp(LocalDateTime.now());
        }

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        log.info("Recorded quiz attempt successfully: id={}", saved.getId());

        // --- Streak: record activity for today ---
        Streak streak = streakRepository.findByUserId(userId).orElse(null);
        if (streak != null) {
            streak.recordActivity(LocalDate.now());
            streakRepository.save(streak);
        }

        // --- Badge: award FIRST_QUIZ once ---
        if (!badgeRepository.existsByUserIdAndBadgeType(userId, "FIRST_QUIZ")) {
            Badge badge = Badge.builder()
                    .user(user)
                    .badgeType("FIRST_QUIZ")
                    .awardedAt(LocalDateTime.now())
                    .build();
            badgeRepository.save(badge);
            log.info("Awarded FIRST_QUIZ badge to user {}", userId);
        }

        // --- Literacy: +20 (capped at 100) on first passing attempt for this lesson ---
        if (saved.getScore() >= 70) {
            // Count passing previous attempts for this lesson, excluding the one just saved
            List<QuizAttempt> previousAttempts =
                    quizAttemptRepository.findByUserIdAndLessonId(userId, lessonId);
            long passingPrevious = previousAttempts.stream()
                    .filter(a -> !a.getId().equals(saved.getId()) && a.getScore() >= 70)
                    .count();

            if (passingPrevious == 0) {
                int current = user.getLiteracyScore() != null ? user.getLiteracyScore() : 0;
                user.setLiteracyScore(Math.min(100, current + 20));
                userRepository.save(user);
                log.info("User {} literacy score updated to {} after first pass on lesson {}",
                        userId, user.getLiteracyScore(), lessonId);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", saved));
    }

    @Operation(summary = "Get quiz attempts by user ID path",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseBean> getQuizAttemptsByUserPath(@PathVariable Long userId) {
        log.info("Fetching quiz attempts for user id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", attempts));
    }

    @Operation(summary = "Get quiz attempts by user ID query parameter",
               responses = {@ApiResponse(responseCode = "400", description = "Parameter 'userId' is required"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping
    public ResponseEntity<ResponseBean> getQuizAttemptsByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return getQuizAttemptsByUserPath(userId);
        }
        log.warn("Querying quiz attempts rejected: Parameter 'userId' is required");
        throw new BusinessRuleException("Parameter 'userId' is required");
    }
}
