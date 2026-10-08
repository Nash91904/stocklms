package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.QuizAttemptRequestDto;
import com.stocklms.stock_lms.dto.QuizAttemptResponseDto;
import com.stocklms.stock_lms.entity.Lesson;
import com.stocklms.stock_lms.entity.QuizAttempt;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.LessonRepository;
import com.stocklms.stock_lms.repository.QuizAttemptRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user quiz submissions and scores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public QuizAttemptResponseDto recordQuizAttempt(QuizAttemptRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        Lesson lesson = lessonRepository.findById(dto.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", dto.getLessonId()));

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .lesson(lesson)
                .score(dto.getScore())
                .timestamp(LocalDateTime.now())
                .build();

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        log.info("Recorded quiz attempt – user={} lesson={} score={}", dto.getUserId(), dto.getLessonId(), dto.getScore());
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponseDto> getQuizAttemptsByUser(Long userId) {
        log.info("Fetching quiz attempts for user id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        return quizAttemptRepository.findByUserId(userId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private QuizAttemptResponseDto toResponseDto(QuizAttempt attempt) {
        return QuizAttemptResponseDto.builder()
                .id(attempt.getId())
                .userId(attempt.getUser() != null ? attempt.getUser().getId() : null)
                .lessonId(attempt.getLesson() != null ? attempt.getLesson().getId() : null)
                .score(attempt.getScore())
                .timestamp(attempt.getTimestamp())
                .build();
    }
}
