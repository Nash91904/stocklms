package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.LessonRequestDto;
import com.stocklms.stock_lms.dto.LessonResponseDto;
import com.stocklms.stock_lms.entity.Lesson;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing LMS lessons and reading content.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public List<LessonResponseDto> getAllLessons() {
        log.info("Fetching all lessons");
        return lessonRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public LessonResponseDto getLessonById(Long id) {
        log.info("Fetching lesson with id: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        return toResponseDto(lesson);
    }

    public LessonResponseDto createLesson(LessonRequestDto dto) {
        Lesson lesson = Lesson.builder()
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .unlockCondition(dto.getUnlockCondition() != null ? dto.getUnlockCondition().trim() : null)
                .build();

        Lesson saved = lessonRepository.save(lesson);
        log.info("Created lesson: '{}' (id={})", saved.getTitle(), saved.getId());
        return toResponseDto(saved);
    }

    public LessonResponseDto updateLesson(Long id, LessonRequestDto dto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        lesson.setTitle(dto.getTitle().trim());
        lesson.setContent(dto.getContent().trim());
        lesson.setUnlockCondition(dto.getUnlockCondition() != null ? dto.getUnlockCondition().trim() : null);

        Lesson saved = lessonRepository.save(lesson);
        log.info("Updated lesson id: {}", id);
        return toResponseDto(saved);
    }

    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson", id);
        }
        lessonRepository.deleteById(id);
        log.info("Deleted lesson id: {}", id);
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
