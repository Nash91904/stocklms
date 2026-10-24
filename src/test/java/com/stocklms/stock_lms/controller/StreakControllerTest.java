package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.StreakRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StreakController.class)
@Import(GlobalExceptionHandler.class)
class StreakControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StreakRepository streakRepository;

    @Test
    void getStreakById_success_returns200() throws Exception {
        Streak streak = Streak.builder()
                .id(1L)
                .user(User.builder().id(5L).build())
                .currentStreak(3)
                .longestStreak(7)
                .freezeCount(2)
                .build();

        when(streakRepository.findById(1L)).thenReturn(Optional.of(streak));

        mockMvc.perform(get("/api/streaks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(5))
                .andExpect(jsonPath("$.data.currentStreak").value(3));
    }

    @Test
    void getStreakById_notFound_returns404() throws Exception {
        when(streakRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/streaks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("Streak not found with id: 99"));
    }

    @Test
    void getStreakByUserId_success_returns200() throws Exception {
        Streak streak = Streak.builder()
                .id(1L)
                .user(User.builder().id(5L).build())
                .currentStreak(3)
                .build();

        when(streakRepository.findByUserId(5L)).thenReturn(Optional.of(streak));

        mockMvc.perform(get("/api/streaks/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void patchStreak_success_returns200() throws Exception {
        Streak existing = Streak.builder()
                .id(1L)
                .currentStreak(3)
                .longestStreak(5)
                .freezeCount(1)
                .build();

        when(streakRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(streakRepository.save(any(Streak.class))).thenAnswer(i -> i.getArgument(0));

        String json = """
                {
                    "currentStreak": 4,
                    "longestStreak": 6,
                    "freezeCount": 0
                }
                """;

        mockMvc.perform(patch("/api/streaks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.currentStreak").value(4))
                .andExpect(jsonPath("$.data.longestStreak").value(6));
    }
}
