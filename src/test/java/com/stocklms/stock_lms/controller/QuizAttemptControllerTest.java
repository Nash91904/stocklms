package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Lesson;
import com.stocklms.stock_lms.entity.QuizAttempt;
import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizAttemptController.class)
@Import(GlobalExceptionHandler.class)
class QuizAttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizAttemptRepository quizAttemptRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LessonRepository lessonRepository;

    @MockitoBean
    private StreakRepository streakRepository;

    @MockitoBean
    private BadgeRepository badgeRepository;

    // ── Helper: default pass-through stubs for streak/badge/literacy ─────────

    private void stubDefaultExtras(Long userId) {
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(badgeRepository.existsByUserIdAndBadgeType(eq(userId), anyString())).thenReturn(false);
    }

    // ── Existing tests (updated with streak/badge mock stubs) ───────────────

    @Test
    void recordQuizAttempt_success_returns201() throws Exception {
        User user = User.builder().id(1L).literacyScore(0).build();
        Lesson lesson = Lesson.builder().id(5L).build(); // no unlock condition
        QuizAttempt saved = QuizAttempt.builder()
                .id(10L).user(user).lesson(lesson).score(85)
                .timestamp(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(5L)).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(saved);
        when(quizAttemptRepository.findByUserIdAndLessonId(1L, 5L)).thenReturn(List.of(saved));
        stubDefaultExtras(1L);

        String json = """
                {
                    "user": {"id": 1},
                    "lesson": {"id": 5},
                    "score": 85
                }
                """;

        mockMvc.perform(post("/api/quiz-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.lessonId").value(5))
                .andExpect(jsonPath("$.data.score").value(85));
    }

    @Test
    void recordQuizAttempt_userNotFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String json = """
                {
                    "user": {"id": 99},
                    "lesson": {"id": 5},
                    "score": 85
                }
                """;

        mockMvc.perform(post("/api/quiz-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("User not found with id: 99"));
    }

    @Test
    void getQuizAttemptsByUserPath_success_returns200() throws Exception {
        QuizAttempt attempt = QuizAttempt.builder().id(1L).score(90).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(quizAttemptRepository.findByUserId(1L)).thenReturn(List.of(attempt));

        mockMvc.perform(get("/api/quiz-attempts/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    // ── New tests ────────────────────────────────────────────────────────────

    @Test
    void recordQuizAttempt_lockedLesson_returns400() throws Exception {
        // Lesson requires literacy >= 30, user has 20
        User user = User.builder().id(1L).literacyScore(20).build();
        Lesson lesson = Lesson.builder().id(7L).unlockCondition("MIN_LITERACY_30").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(7L)).thenReturn(Optional.of(lesson));

        String json = """
                {"user":{"id":1},"lesson":{"id":7},"score":80}
                """;

        mockMvc.perform(post("/api/quiz-attempts").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Lesson is locked: requires literacy score 30"));

        verify(quizAttemptRepository, never()).save(any());
    }

    @Test
    void recordQuizAttempt_unlockedLesson_noConditionIsOpen() throws Exception {
        // Lesson with null condition is always open regardless of score
        User user = User.builder().id(1L).literacyScore(0).build();
        Lesson lesson = Lesson.builder().id(8L).unlockCondition(null).build();
        QuizAttempt saved = QuizAttempt.builder().id(20L).user(user).lesson(lesson).score(50)
                .timestamp(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(8L)).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(saved);
        when(quizAttemptRepository.findByUserIdAndLessonId(1L, 8L)).thenReturn(List.of(saved));
        stubDefaultExtras(1L);

        String json = """
                {"user":{"id":1},"lesson":{"id":8},"score":50}
                """;

        mockMvc.perform(post("/api/quiz-attempts").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void recordQuizAttempt_literacyIncreased_onFirstPass() throws Exception {
        // First passing attempt: literacyScore should go from 10 to 20
        User user = User.builder().id(2L).literacyScore(10).build();
        Lesson lesson = Lesson.builder().id(5L).build();
        QuizAttempt saved = QuizAttempt.builder().id(30L).user(user).lesson(lesson).score(80)
                .timestamp(LocalDateTime.now()).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(5L)).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(saved);
        // findByUserIdAndLessonId returns only the newly saved attempt (id=30)
        when(quizAttemptRepository.findByUserIdAndLessonId(2L, 5L)).thenReturn(List.of(saved));
        stubDefaultExtras(2L);

        String json = """
                {"user":{"id":2},"lesson":{"id":5},"score":80}
                """;

        mockMvc.perform(post("/api/quiz-attempts").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        // literacyScore updated: 10 + 10 = 20
        verify(userRepository).save(argThat(u -> u.getLiteracyScore() == 20));
    }

    @Test
    void recordQuizAttempt_literacyNotIncreased_onSecondPass() throws Exception {
        // Second passing attempt for same lesson: no literacy change
        User user = User.builder().id(3L).literacyScore(20).build();
        Lesson lesson = Lesson.builder().id(5L).build();
        QuizAttempt saved = QuizAttempt.builder().id(40L).user(user).lesson(lesson).score(75)
                .timestamp(LocalDateTime.now()).build();
        // A previous passing attempt exists (id=10, score=80 >= 70)
        QuizAttempt prevPass = QuizAttempt.builder().id(10L).user(user).lesson(lesson).score(80)
                .timestamp(LocalDateTime.now().minusDays(1)).build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(5L)).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(saved);
        when(quizAttemptRepository.findByUserIdAndLessonId(3L, 5L)).thenReturn(List.of(prevPass, saved));
        stubDefaultExtras(3L);

        String json = """
                {"user":{"id":3},"lesson":{"id":5},"score":75}
                """;

        mockMvc.perform(post("/api/quiz-attempts").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        // userRepository.save must NOT be called for literacy update
        verify(userRepository, never()).save(any());
    }
}
