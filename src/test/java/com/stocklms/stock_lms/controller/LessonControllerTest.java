package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Lesson;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
@Import(GlobalExceptionHandler.class)
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonRepository lessonRepository;

    @Test
    void getAllLessons_returns200() throws Exception {
        Lesson lesson = Lesson.builder()
                .id(1L)
                .title("Intro to Trading")
                .content("Lesson content")
                .build();

        when(lessonRepository.findAll()).thenReturn(List.of(lesson));

        mockMvc.perform(get("/api/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Intro to Trading"));
    }

    @Test
    void getLessonById_success_returns200() throws Exception {
        Lesson lesson = Lesson.builder()
                .id(1L)
                .title("Intro to Trading")
                .content("Lesson content")
                .build();

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

        mockMvc.perform(get("/api/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getLessonById_notFound_returns404() throws Exception {
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lessons/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("Lesson not found with id: 99"));
    }

    @Test
    void createLesson_success_returns201() throws Exception {
        Lesson saved = Lesson.builder()
                .id(1L)
                .title("Diversification")
                .content("Don't put all eggs in one basket")
                .build();

        when(lessonRepository.save(any(Lesson.class))).thenReturn(saved);

        String json = """
                {
                    "title": "Diversification",
                    "content": "Don't put all eggs in one basket"
                }
                """;

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Diversification"));
    }

    @Test
    void createLesson_validationFailure_returns400() throws Exception {
        String invalidJson = """
                {
                    "title": "",
                    "content": ""
                }
                """;

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Validation failed"))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.content").exists());

        verify(lessonRepository, never()).save(any());
    }

    @Test
    void deleteLesson_success_returns200() throws Exception {
        when(lessonRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data").value("Lesson with ID 1 deleted successfully."));

        verify(lessonRepository).deleteById(1L);
    }
}
