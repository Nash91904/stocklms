package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.QuizAttemptRequestDto;
import com.stocklms.stock_lms.dto.QuizAttemptResponseDto;
import com.stocklms.stock_lms.entity.QuizAttempt;
import com.stocklms.stock_lms.repository.LessonRepository;
import com.stocklms.stock_lms.repository.QuizAttemptRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user quiz submissions and scores.
 */
@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public QuizAttemptResponseDto recordQuizAttempt(QuizAttemptRequestDto dto) {
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + dto.getUserId());
        }
        if (!lessonRepository.existsById(dto.getLessonId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found with id: " + dto.getLessonId());
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .userId(dto.getUserId())
                .lessonId(dto.getLessonId())
                .score(dto.getScore())
                .timestamp(LocalDateTime.now())
                .build();

        return toResponseDto(quizAttemptRepository.save(attempt));
    }

    public List<QuizAttemptResponseDto> getQuizAttemptsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
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
