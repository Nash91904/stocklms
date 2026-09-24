package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.LessonRequestDto;
import com.stocklms.stock_lms.dto.LessonResponseDto;
import com.stocklms.stock_lms.entity.Lesson;
import com.stocklms.stock_lms.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing LMS lessons and reading content.
 */
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public List<LessonResponseDto> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public LessonResponseDto getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found with id: " + id));
        return toResponseDto(lesson);
    }

    public LessonResponseDto createLesson(LessonRequestDto dto) {
        Lesson lesson = Lesson.builder()
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .unlockCondition(dto.getUnlockCondition() != null ? dto.getUnlockCondition().trim() : null)
                .build();

        return toResponseDto(lessonRepository.save(lesson));
    }

    public LessonResponseDto updateLesson(Long id, LessonRequestDto dto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found with id: " + id));

        lesson.setTitle(dto.getTitle().trim());
        lesson.setContent(dto.getContent().trim());
        lesson.setUnlockCondition(dto.getUnlockCondition() != null ? dto.getUnlockCondition().trim() : null);

        return toResponseDto(lessonRepository.save(lesson));
    }

    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found with id: " + id);
        }
        lessonRepository.deleteById(id);
    }

    private LessonResponseDto toResponseDto(Lesson lesson) {
        return LessonResponseDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .unlockCondition(lesson.getUnlockCondition())
                .build();
    }
}
