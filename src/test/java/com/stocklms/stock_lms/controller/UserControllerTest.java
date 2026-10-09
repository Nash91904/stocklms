package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.dto.UserCreateRequestDto;
import com.stocklms.stock_lms.dto.UserPatchRequestDto;
import com.stocklms.stock_lms.dto.UserResponseDto;
import com.stocklms.stock_lms.exception.ConflictException;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_success_returns201() throws Exception {
        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .email("alex@example.com")
                .riskProfile("MODERATE")
                .literacyScore(50)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.createUser(any(UserCreateRequestDto.class))).thenReturn(response);

        String json = """
                {
                    "email": "alex@example.com",
                    "password": "strongPassword123",
                    "riskProfile": "MODERATE",
                    "literacyScore": 50
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alex@example.com"))
                .andExpect(jsonPath("$.riskProfile").value("MODERATE"));
    }

    @Test
    void createUser_validationFailure_returns400() throws Exception {
        // Invalid email format and password length < 6
        String invalidJson = """
                {
                    "email": "invalid-email-format",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verify(userService, never()).createUser(any());
    }

    @Test
    void getUserById_success_returns200() throws Exception {
        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .email("alex@example.com")
                .riskProfile("MODERATE")
                .literacyScore(50)
                .build();

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alex@example.com"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
    }

    @Test
    void patchUser_success_returns200() throws Exception {
        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .email("alex@example.com")
                .riskProfile("AGGRESSIVE")
                .literacyScore(85)
                .build();

        when(userService.patchUser(eq(1L), any(UserPatchRequestDto.class))).thenReturn(response);

        String json = """
                {
                    "riskProfile": "AGGRESSIVE",
                    "literacyScore": 85
                }
                """;

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.riskProfile").value("AGGRESSIVE"))
                .andExpect(jsonPath("$.literacyScore").value(85));
    }

    @Test
    void patchUser_validationFailure_returns400() throws Exception {
        // Negative literacy score violates @Min(0)
        String invalidJson = """
                {
                    "literacyScore": -10
                }
                """;

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.literacyScore").exists());

        verify(userService, never()).patchUser(any(), any());
    }

    @Test
    void patchUser_notFound_returns404() throws Exception {
        when(userService.patchUser(eq(99L), any(UserPatchRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("User", 99L));

        String json = """
                {
                    "riskProfile": "MODERATE"
                }
                """;

        mockMvc.perform(patch("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void deleteUser_success_returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_conflict_returns409() throws Exception {
        doThrow(new ConflictException("Cannot delete user with id 1 because they have existing trade records in the immutable ledger"))
                .when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void addToWatchlist_success_returns201() throws Exception {
        StockResponseDto stock = StockResponseDto.builder()
                .id(10L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .build();

        when(userService.addToWatchlist(1L, 10L)).thenReturn(stock);

        mockMvc.perform(post("/api/users/1/watchlist/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ticker").value("AAPL"));
    }

    @Test
    void addToWatchlist_alreadyExists_returns409() throws Exception {
        when(userService.addToWatchlist(1L, 10L))
                .thenThrow(new ConflictException("Stock AAPL is already in the watchlist of user 1"));

        mockMvc.perform(post("/api/users/1/watchlist/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void addToWatchlist_notFound_returns404() throws Exception {
        when(userService.addToWatchlist(99L, 10L))
                .thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(post("/api/users/99/watchlist/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void removeFromWatchlist_success_returns204() throws Exception {
        doNothing().when(userService).removeFromWatchlist(1L, 10L);

        mockMvc.perform(delete("/api/users/1/watchlist/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFromWatchlist_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Stock with id 10 is not in the watchlist of user 1"))
                .when(userService).removeFromWatchlist(1L, 10L);

        mockMvc.perform(delete("/api/users/1/watchlist/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void getWatchlist_success_returns200() throws Exception {
        StockResponseDto stock = StockResponseDto.builder()
                .id(10L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .build();

        when(userService.getWatchlist(1L)).thenReturn(List.of(stock));

        mockMvc.perform(get("/api/users/1/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].ticker").value("AAPL"));
    }
}
