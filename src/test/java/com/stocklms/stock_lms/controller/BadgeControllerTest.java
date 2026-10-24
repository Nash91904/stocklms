package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Badge;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.BadgeRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BadgeController.class)
@Import(GlobalExceptionHandler.class)
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BadgeRepository badgeRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void awardBadge_success_returns201() throws Exception {
        User user = User.builder().id(1L).build();
        Badge saved = Badge.builder()
                .id(1L)
                .user(user)
                .badgeType("FIRST_TRADE")
                .awardedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(badgeRepository.save(any(Badge.class))).thenReturn(saved);

        String json = """
                {
                    "user": {"id": 1},
                    "badgeType": "FIRST_TRADE"
                }
                """;

        mockMvc.perform(post("/api/badges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.badgeType").value("FIRST_TRADE"));
    }

    @Test
    void awardBadge_userNotFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String json = """
                {
                    "user": {"id": 99},
                    "badgeType": "FIRST_TRADE"
                }
                """;

        mockMvc.perform(post("/api/badges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("User not found with id: 99"));
    }

    @Test
    void getBadgesByUserPath_success_returns200() throws Exception {
        Badge badge = Badge.builder().id(1L).badgeType("FIRST_TRADE").build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(badgeRepository.findByUserId(1L)).thenReturn(List.of(badge));

        mockMvc.perform(get("/api/badges/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }
}
