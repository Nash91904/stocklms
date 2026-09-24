package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.QuizAttemptRequestDto;
import com.stocklms.stock_lms.dto.QuizAttemptResponseDto;
import com.stocklms.stock_lms.entity.QuizAttempt;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.LessonRepository;
import com.stocklms.stock_lms.repository.QuizAttemptRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public QuizAttemptResponseDto recordQuizAttempt(QuizAttemptRequestDto dto) {
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResourceNotFoundException("User", dto.getUserId());
        }
        if (!lessonRepository.existsById(dto.getLessonId())) {
            throw new ResourceNotFoundException("Lesson", dto.getLessonId());
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .userId(dto.getUserId())
                .lessonId(dto.getLessonId())
                .score(dto.getScore())
                .timestamp(LocalDateTime.now())
                .build();

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        log.info("Recorded quiz attempt – user={} lesson={} score={}", dto.getUserId(), dto.getLessonId(), dto.getScore());
        return toResponseDto(saved);
    }

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
                .userId(attempt.getUserId())
                .lessonId(attempt.getLessonId())
                .score(attempt.getScore())
                .timestamp(attempt.getTimestamp())
                .build();
    }
}
