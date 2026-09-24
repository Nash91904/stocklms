package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.QuizAttemptRequestDto;
import com.stocklms.stock_lms.dto.QuizAttemptResponseDto;
import com.stocklms.stock_lms.service.QuizAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for recording and viewing user quiz attempts.
 */
@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizAttemptResponseDto recordQuizAttempt(@Valid @RequestBody QuizAttemptRequestDto dto) {
        return quizAttemptService.recordQuizAttempt(dto);
    }

    @GetMapping("/user/{userId}")
    public List<QuizAttemptResponseDto> getQuizAttemptsByUserPath(@PathVariable Long userId) {
        return quizAttemptService.getQuizAttemptsByUser(userId);
    }

    @GetMapping
    public List<QuizAttemptResponseDto> getQuizAttemptsByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return quizAttemptService.getQuizAttemptsByUser(userId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'userId' is required");
    }
}
