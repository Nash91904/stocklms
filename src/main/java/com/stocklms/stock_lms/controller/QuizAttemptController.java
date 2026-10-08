package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.QuizAttemptRequestDto;
import com.stocklms.stock_lms.dto.QuizAttemptResponseDto;
import com.stocklms.stock_lms.service.QuizAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for recording and viewing user quiz attempts.
 */
@Tag(name = "Quiz Attempts", description = "Record and view user quiz attempts")
@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @Operation(summary = "Record a quiz attempt",
               responses = {@ApiResponse(responseCode = "201", description = "Quiz attempt recorded"),
                            @ApiResponse(responseCode = "404", description = "User or lesson not found")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizAttemptResponseDto recordQuizAttempt(@Valid @RequestBody QuizAttemptRequestDto dto) {
        return quizAttemptService.recordQuizAttempt(dto);
    }

    @Operation(summary = "Get quiz attempts by user ID path",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/user/{userId}")
    public List<QuizAttemptResponseDto> getQuizAttemptsByUserPath(@PathVariable Long userId) {
        return quizAttemptService.getQuizAttemptsByUser(userId);
    }

    @Operation(summary = "Get quiz attempts by user ID query parameter",
               responses = {@ApiResponse(responseCode = "400", description = "Parameter 'userId' is required"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping
    public List<QuizAttemptResponseDto> getQuizAttemptsByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return quizAttemptService.getQuizAttemptsByUser(userId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'userId' is required");
    }
}
