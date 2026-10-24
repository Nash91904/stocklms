package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PortfolioRepository portfolioRepository;

    @MockitoBean
    private StreakRepository streakRepository;

    @MockitoBean
    private TradeRepository tradeRepository;

    @MockitoBean
    private StockRepository stockRepository;

    @MockitoBean
    private QuizAttemptRepository quizAttemptRepository;

    @MockitoBean
    private BadgeRepository badgeRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_success_returns201() throws Exception {
        User savedUser = User.builder()
                .id(1L)
                .email("alex@example.com")
                .passwordHash("hashedPass")
                .riskProfile("MODERATE")
                .literacyScore(50)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(passwordEncoder.encode("strongPassword123")).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

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
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("alex@example.com"))
                .andExpect(jsonPath("$.data.riskProfile").value("MODERATE"));

        // Verify Portfolio & Streak auto-provisioning
        verify(portfolioRepository).save(argThat(p ->
                p.getUser() != null && p.getCashBalance().compareTo(new BigDecimal("100000.00")) == 0
        ));
        verify(streakRepository).save(argThat(s ->
                s.getUser() != null && s.getCurrentStreak() == 0 && s.getLongestStreak() == 0
        ));
    }

    @Test
    void createUser_duplicateEmail_returns400() throws Exception {
        when(userRepository.existsByEmail("alex@example.com")).thenReturn(true);

        String json = """
                {
                    "email": "alex@example.com",
                    "password": "strongPassword123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("User with email already exists: alex@example.com"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_validationFailure_returns400() throws Exception {
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
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Validation failed"))
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists());

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_success_returns200() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("alex@example.com")
                .riskProfile("MODERATE")
                .literacyScore(50)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("alex@example.com"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("User not found with id: 99"));
    }

    @Test
    void patchUser_success_returns200() throws Exception {
        User existing = User.builder()
                .id(1L)
                .email("alex@example.com")
                .riskProfile("MODERATE")
                .literacyScore(50)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

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
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.riskProfile").value("AGGRESSIVE"))
                .andExpect(jsonPath("$.data.literacyScore").value(85));
    }

    @Test
    void patchUser_validationFailure_returns400() throws Exception {
        User existing = User.builder().id(1L).email("alex@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        String invalidJson = """
                {
                    "literacyScore": -10
                }
                """;

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Literacy score cannot be negative"));
    }

    @Test
    void patchUser_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String json = """
                {
                    "riskProfile": "MODERATE"
                }
                """;

        mockMvc.perform(patch("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    void deleteUser_success_returns200() throws Exception {
        User user = User.builder().id(1L).email("alex@example.com").watchlist(new HashSet<>()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tradeRepository.existsByUserId(1L)).thenReturn(false);
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(streakRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(quizAttemptRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(badgeRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data").value("User with ID 1 deleted successfully."));

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_conflict_returns409() throws Exception {
        User user = User.builder().id(1L).email("alex@example.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tradeRepository.existsByUserId(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("409"))
                .andExpect(jsonPath("$.errorDesc").value("Cannot delete user with id 1 because they have existing trade records in the immutable ledger"));

        verify(userRepository, never()).delete(any());
    }

    @Test
    void addToWatchlist_success_returns201() throws Exception {
        User user = User.builder().id(1L).watchlist(new HashSet<>()).build();
        Stock stock = Stock.builder().id(10L).ticker("AAPL").name("Apple Inc.").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));

        mockMvc.perform(post("/api/users/1/watchlist/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.ticker").value("AAPL"));

        verify(userRepository).save(user);
    }

    @Test
    void addToWatchlist_alreadyExists_returns409() throws Exception {
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        User user = User.builder().id(1L).watchlist(new HashSet<>(List.of(stock))).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));

        mockMvc.perform(post("/api/users/1/watchlist/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("409"))
                .andExpect(jsonPath("$.errorDesc").value("Stock AAPL is already in the watchlist of user 1"));
    }

    @Test
    void addToWatchlist_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/99/watchlist/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    void removeFromWatchlist_success_returns200() throws Exception {
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        User user = User.builder().id(1L).watchlist(new HashSet<>(List.of(stock))).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));

        mockMvc.perform(delete("/api/users/1/watchlist/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data").value("Stock with ID 10 removed from watchlist successfully."));

        verify(userRepository).save(user);
    }

    @Test
    void removeFromWatchlist_notFound_returns404() throws Exception {
        User user = User.builder().id(1L).watchlist(new HashSet<>()).build();
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));

        mockMvc.perform(delete("/api/users/1/watchlist/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("Stock with id 10 is not in the watchlist of user 1"));
    }

    @Test
    void getWatchlist_success_returns200() throws Exception {
        Stock stock = Stock.builder().id(10L).ticker("AAPL").name("Apple Inc.").build();
        User user = User.builder().id(1L).watchlist(new HashSet<>(List.of(stock))).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].ticker").value("AAPL"));
    }
}
