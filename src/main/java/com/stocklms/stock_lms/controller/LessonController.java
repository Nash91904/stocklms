package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.LessonRequestDto;
import com.stocklms.stock_lms.dto.LessonResponseDto;
import com.stocklms.stock_lms.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for LMS educational lessons.
 */
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    public List<LessonResponseDto> getAllLessons() {
        return lessonService.getAllLessons();
    }

    @GetMapping("/{id}")
    public LessonResponseDto getLessonById(@PathVariable Long id) {
        return lessonService.getLessonById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponseDto createLesson(@Valid @RequestBody LessonRequestDto dto) {
        return lessonService.createLesson(dto);
    }

    @PutMapping("/{id}")
    public LessonResponseDto updateLesson(@PathVariable Long id, @Valid @RequestBody LessonRequestDto dto) {
        return lessonService.updateLesson(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
    }
}
