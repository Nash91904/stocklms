package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.LessonRequestDto;
import com.stocklms.stock_lms.dto.LessonResponseDto;
import com.stocklms.stock_lms.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for LMS educational lessons.
 */
@Tag(name = "Lessons", description = "CRUD operations for educational lessons")
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @Operation(summary = "Get all lessons")
    @GetMapping
    public List<LessonResponseDto> getAllLessons() {
        return lessonService.getAllLessons();
    }

    @Operation(summary = "Get a lesson by ID",
               responses = {@ApiResponse(responseCode = "404", description = "Lesson not found")})
    @GetMapping("/{id}")
    public LessonResponseDto getLessonById(@PathVariable Long id) {
        return lessonService.getLessonById(id);
    }

    @Operation(summary = "Create a new lesson",
               responses = {@ApiResponse(responseCode = "201", description = "Lesson created")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponseDto createLesson(@Valid @RequestBody LessonRequestDto dto) {
        return lessonService.createLesson(dto);
    }

    @Operation(summary = "Full update of a lesson",
               responses = {@ApiResponse(responseCode = "404", description = "Lesson not found")})
    @PutMapping("/{id}")
    public LessonResponseDto updateLesson(@PathVariable Long id, @Valid @RequestBody LessonRequestDto dto) {
        return lessonService.updateLesson(id, dto);
    }

    @Operation(summary = "Delete a lesson",
               responses = {@ApiResponse(responseCode = "204", description = "Deleted"),
                            @ApiResponse(responseCode = "404", description = "Lesson not found")})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
    }
}
