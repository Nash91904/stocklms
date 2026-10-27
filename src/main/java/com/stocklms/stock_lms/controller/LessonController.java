package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Lesson;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.LessonRepository;
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

import java.util.List;

/**
 * REST controller for LMS educational lessons.
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Lessons", description = "CRUD operations for educational lessons")
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonRepository lessonRepository;

    @Operation(summary = "Get all lessons")
    @GetMapping
    public ResponseEntity<ResponseBean> getAllLessons() {
        log.info("Fetching all lessons");
        List<Lesson> lessons = lessonRepository.findAll();
        return ResponseEntity.ok(ResponseBean.success("200", "Success", lessons));
    }

    @Operation(summary = "Get a lesson by ID",
               responses = {@ApiResponse(responseCode = "404", description = "Lesson not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBean> getLessonById(@PathVariable Long id) {
        log.info("Fetching lesson with id: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", lesson));
    }

    @Operation(summary = "Create a new lesson",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                               {"title": "string", "content": "string", "unlockCondition": "MIN_LITERACY_0"}
                               """)
               )),
               responses = {@ApiResponse(responseCode = "201", description = "Lesson created"),
                            @ApiResponse(responseCode = "400", description = "Validation failure")})
    @PostMapping
    @Transactional
    public ResponseEntity<ResponseBean> createLesson(@Valid @RequestBody Lesson lesson) {
        log.info("Creating lesson: '{}'", lesson.getTitle());
        lesson.setTitle(lesson.getTitle().trim());
        lesson.setContent(lesson.getContent().trim());
        lesson.setUnlockCondition(lesson.getUnlockCondition() != null ? lesson.getUnlockCondition().trim() : null);

        Lesson saved = lessonRepository.save(lesson);
        log.info("Created lesson: '{}' (id={})", saved.getTitle(), saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", saved));
    }

    @Operation(summary = "Full update of a lesson",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                               {"title": "string", "content": "string", "unlockCondition": "MIN_LITERACY_30"}
                               """)
               )),
               responses = {@ApiResponse(responseCode = "200", description = "Lesson updated"),
                            @ApiResponse(responseCode = "404", description = "Lesson not found"),
                            @ApiResponse(responseCode = "400", description = "Validation failure")})
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> updateLesson(@PathVariable Long id, @Valid @RequestBody Lesson lesson) {
        log.info("Updating lesson with id: {}", id);
        Lesson existing = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        existing.setTitle(lesson.getTitle().trim());
        existing.setContent(lesson.getContent().trim());
        existing.setUnlockCondition(lesson.getUnlockCondition() != null ? lesson.getUnlockCondition().trim() : null);

        Lesson saved = lessonRepository.save(existing);
        log.info("Updated lesson id: {}", id);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", saved));
    }

    @Operation(summary = "Delete a lesson",
               responses = {@ApiResponse(responseCode = "200", description = "Lesson deleted"),
                            @ApiResponse(responseCode = "404", description = "Lesson not found")})
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> deleteLesson(@PathVariable Long id) {
        log.info("Deleting lesson with id: {}", id);
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson", id);
        }
        lessonRepository.deleteById(id);
        log.info("Deleted lesson id: {}", id);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", "Lesson with ID " + id + " deleted successfully."));
    }
}
