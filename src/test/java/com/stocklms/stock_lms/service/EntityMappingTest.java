package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.dto.UserCreateRequestDto;
import com.stocklms.stock_lms.dto.UserResponseDto;
import com.stocklms.stock_lms.entity.*;
import com.stocklms.stock_lms.exception.ConflictException;
import com.stocklms.stock_lms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying JPA entity mappings and relationship business rules:
 * - One-to-One: User <-> Portfolio, User <-> Streak
 * - Many-to-One / One-to-Many: Trade -> User/Stock, PriceHistory -> Stock, etc.
 * - Many-to-Many: User <-> Stock watchlist
 * - Immutable ledger delete conflict protection (HTTP 409)
 */
@ExtendWith(MockitoExtension.class)
class EntityMappingTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StreakRepository streakRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    private StockService stockService;

    private User sampleUser;
    private Stock sampleStock;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                portfolioRepository,
                streakRepository,
                tradeRepository,
                stockRepository,
                quizAttemptRepository,
                badgeRepository,
                passwordEncoder
        );

        stockService = new StockService(
                stockRepository,
                tradeRepository,
                priceHistoryRepository
        );

        sampleUser = User.builder()
                .id(1L)
                .email("learner@stocklms.com")
                .passwordHash("hashedPass")
                .riskProfile("MODERATE")
                .literacyScore(75)
                .createdAt(LocalDateTime.now())
                .watchlist(new HashSet<>())
                .trades(new ArrayList<>())
                .quizAttempts(new ArrayList<>())
                .badges(new ArrayList<>())
                .build();

        sampleStock = Stock.builder()
                .id(10L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .priceHistory(new ArrayList<>())
                .trades(new ArrayList<>())
                .usersWatching(new HashSet<>())
                .build();
    }

    // ── One-to-One: User <-> Portfolio & User <-> Streak ─────────────────────

    @Test
    @DisplayName("One-to-One: Registering a user initializes Portfolio and Streak linked to the user")
    void testOneToOne_userCreationInitializesPortfolioAndStreak() {
        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .email("new@stocklms.com")
                .password("plainSecret")
                .build();

        when(userRepository.existsByEmail("new@stocklms.com")).thenReturn(false);
        when(passwordEncoder.encode("plainSecret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponseDto created = userService.createUser(request);
        assertNotNull(created);

        // Verify Portfolio is created with owning reference to the saved user
        verify(portfolioRepository).save(argThat(p ->
                p.getUser() != null && p.getUser().getId().equals(2L) &&
                p.getCashBalance().compareTo(new BigDecimal("100000.00")) == 0
        ));

        // Verify Streak is created with owning reference to the saved user
        verify(streakRepository).save(argThat(s ->
                s.getUser() != null && s.getUser().getId().equals(2L) &&
                s.getCurrentStreak() == 0 && s.getLongestStreak() == 0
        ));
    }

    @Test
    @DisplayName("One-to-One: Deleting a user cleans up linked Portfolio and Streak")
    void testOneToOne_userDeletionCleansUpPortfolioAndStreak() {
        Portfolio portfolio = Portfolio.builder().id(11L).user(sampleUser).build();
        Streak streak = Streak.builder().id(22L).user(sampleUser).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(tradeRepository.existsByUserId(1L)).thenReturn(false);
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
        when(streakRepository.findByUserId(1L)).thenReturn(Optional.of(streak));
        when(quizAttemptRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(badgeRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        userService.deleteUser(1L);

        verify(portfolioRepository).delete(portfolio);
        verify(streakRepository).delete(streak);
        verify(userRepository).delete(sampleUser);
    }

    // ── Many-to-One / One-to-Many: Trade, PriceHistory, QuizAttempt, Badge ────

    @Test
    @DisplayName("Many-to-One / One-to-Many: Trade correctly links to User and Stock")
    void testManyToOne_tradeLinksToUserAndStock() {
        Trade trade = Trade.builder()
                .id(100L)
                .user(sampleUser)
                .stock(sampleStock)
                .type(TradeType.BUY)
                .quantity(10)
                .price(new BigDecimal("150.00"))
                .timestamp(LocalDateTime.now())
                .build();

        sampleUser.getTrades().add(trade);
        sampleStock.getTrades().add(trade);

        assertEquals(sampleUser, trade.getUser());
        assertEquals(sampleStock, trade.getStock());
        assertEquals(1, sampleUser.getTrades().size());
        assertEquals(1, sampleStock.getTrades().size());
        assertEquals(trade, sampleUser.getTrades().get(0));
    }

    @Test
    @DisplayName("Many-to-One / One-to-Many: PriceHistory links to Stock and populates inverse list")
    void testManyToOne_priceHistoryLinksToStock() {
        PriceHistory history = PriceHistory.builder()
                .id(50L)
                .stock(sampleStock)
                .price(new BigDecimal("150.25"))
                .volume(12000L)
                .timestamp(LocalDateTime.now())
                .build();

        sampleStock.getPriceHistory().add(history);

        assertEquals(sampleStock, history.getStock());
        assertEquals(1, sampleStock.getPriceHistory().size());
        assertEquals(new BigDecimal("150.25"), sampleStock.getPriceHistory().get(0).getPrice());
    }

    @Test
    @DisplayName("Many-to-One / One-to-Many: QuizAttempt links to User and Lesson")
    void testManyToOne_quizAttemptLinksToUserAndLesson() {
        Lesson lesson = Lesson.builder().id(5L).title("Intro to Stocks").quizAttempts(new ArrayList<>()).build();
        QuizAttempt attempt = QuizAttempt.builder()
                .id(30L)
                .user(sampleUser)
                .lesson(lesson)
                .score(90)
                .timestamp(LocalDateTime.now())
                .build();

        sampleUser.getQuizAttempts().add(attempt);
        lesson.getQuizAttempts().add(attempt);

        assertEquals(sampleUser, attempt.getUser());
        assertEquals(lesson, attempt.getLesson());
        assertEquals(1, sampleUser.getQuizAttempts().size());
        assertEquals(1, lesson.getQuizAttempts().size());
    }

    // ── Many-to-Many: Watchlist Operations ───────────────────────────────────

    @Test
    @DisplayName("Many-to-Many: Add stock to watchlist, reject duplicate with ConflictException, remove stock")
    void testManyToMany_watchlistLifecycle() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(sampleStock));

        // 1. Add stock
        StockResponseDto added = userService.addToWatchlist(1L, 10L);
        assertNotNull(added);
        assertEquals("AAPL", added.getTicker());
        assertTrue(sampleUser.getWatchlist().contains(sampleStock));
        verify(userRepository, times(1)).save(sampleUser);

        // 2. Add duplicate throws ConflictException
        assertThrows(ConflictException.class, () -> userService.addToWatchlist(1L, 10L));

        // 3. Get watchlist returns the stock
        List<StockResponseDto> watchlist = userService.getWatchlist(1L);
        assertEquals(1, watchlist.size());
        assertEquals("AAPL", watchlist.get(0).getTicker());

        // 4. Remove stock
        userService.removeFromWatchlist(1L, 10L);
        assertFalse(sampleUser.getWatchlist().contains(sampleStock));
        verify(userRepository, times(2)).save(sampleUser);
    }

    // ── Delete Conflict Rules (HTTP 409 Protection for Immutable Ledgers) ────

    @Test
    @DisplayName("Delete Conflict: Deleting user with existing trades throws ConflictException")
    void testDeleteConflict_userWithTradesThrowsConflictException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(tradeRepository.existsByUserId(1L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> userService.deleteUser(1L));
        assertTrue(ex.getMessage().contains("existing trade records"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete Conflict: Deleting stock referenced by trades throws ConflictException")
    void testDeleteConflict_stockWithTradesThrowsConflictException() {
        when(stockRepository.findById(10L)).thenReturn(Optional.of(sampleStock));
        when(tradeRepository.existsByStockId(10L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> stockService.deleteStock(10L));
        assertTrue(ex.getMessage().contains("immutable trade records"));
        verify(stockRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete Conflict: Deleting stock referenced by price history throws ConflictException")
    void testDeleteConflict_stockWithPriceHistoryThrowsConflictException() {
        when(stockRepository.findById(10L)).thenReturn(Optional.of(sampleStock));
        when(tradeRepository.existsByStockId(10L)).thenReturn(false);
        when(priceHistoryRepository.existsByStockId(10L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> stockService.deleteStock(10L));
        assertTrue(ex.getMessage().contains("immutable price history"));
        verify(stockRepository, never()).delete(any());
    }
}
