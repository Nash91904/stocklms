package com.stocklms.stock_lms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying JPA entity mappings and relationship structures:
 * - One-to-One: User <-> Portfolio, User <-> Streak
 * - Many-to-One / One-to-Many: Trade -> User/Stock, PriceHistory -> Stock, QuizAttempt -> User/Lesson, Badge -> User
 * - Many-to-Many: User <-> Stock watchlist
 * Contains NO references to any service or DTO class.
 */
class EntityMappingTest {

    private User sampleUser;
    private Stock sampleStock;

    @BeforeEach
    void setUp() {
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
    @DisplayName("One-to-One: Portfolio and Streak correctly reference User owning and inverse sides")
    void testOneToOne_userPortfolioAndStreakMapping() {
        Portfolio portfolio = Portfolio.builder()
                .id(11L)
                .user(sampleUser)
                .cashBalance(new BigDecimal("100000.00"))
                .build();
        sampleUser.setPortfolio(portfolio);

        Streak streak = Streak.builder()
                .id(22L)
                .user(sampleUser)
                .currentStreak(5)
                .longestStreak(10)
                .lastActiveDate(LocalDate.now())
                .freezeCount(1)
                .build();
        sampleUser.setStreak(streak);

        // Verify Portfolio 1:1 mapping and flat getters
        assertEquals(sampleUser, portfolio.getUser());
        assertEquals(sampleUser.getId(), portfolio.getUserId());
        assertEquals(portfolio, sampleUser.getPortfolio());
        assertEquals(new BigDecimal("100000.00"), portfolio.getCashBalance());

        // Verify Streak 1:1 mapping and flat getters
        assertEquals(sampleUser, streak.getUser());
        assertEquals(sampleUser.getId(), streak.getUserId());
        assertEquals(streak, sampleUser.getStreak());
        assertEquals(5, streak.getCurrentStreak());
        assertEquals(10, streak.getLongestStreak());
    }

    // ── Many-to-One / One-to-Many: Trade, PriceHistory, QuizAttempt, Badge ────

    @Test
    @DisplayName("Many-to-One / One-to-Many: Trade correctly links to User and Stock with flat getters")
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
        assertEquals(sampleUser.getId(), trade.getUserId());
        assertEquals(sampleStock.getId(), trade.getStockId());
        assertEquals(1, sampleUser.getTrades().size());
        assertEquals(1, sampleStock.getTrades().size());
        assertEquals(trade, sampleUser.getTrades().get(0));
        assertEquals(trade, sampleStock.getTrades().get(0));
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
        assertEquals(sampleStock.getId(), history.getStockId());
        assertEquals(1, sampleStock.getPriceHistory().size());
        assertEquals(new BigDecimal("150.25"), sampleStock.getPriceHistory().get(0).getPrice());
    }

    @Test
    @DisplayName("Many-to-One / One-to-Many: QuizAttempt links to User and Lesson")
    void testManyToOne_quizAttemptLinksToUserAndLesson() {
        Lesson lesson = Lesson.builder()
                .id(5L)
                .title("Intro to Stocks")
                .content("Basic content")
                .quizAttempts(new ArrayList<>())
                .build();

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
        assertEquals(sampleUser.getId(), attempt.getUserId());
        assertEquals(lesson.getId(), attempt.getLessonId());
        assertEquals(1, sampleUser.getQuizAttempts().size());
        assertEquals(1, lesson.getQuizAttempts().size());
    }

    @Test
    @DisplayName("Many-to-One / One-to-Many: Badge links to User")
    void testManyToOne_badgeLinksToUser() {
        Badge badge = Badge.builder()
                .id(40L)
                .user(sampleUser)
                .badgeType("FIRST_TRADE")
                .awardedAt(LocalDateTime.now())
                .build();

        sampleUser.getBadges().add(badge);

        assertEquals(sampleUser, badge.getUser());
        assertEquals(sampleUser.getId(), badge.getUserId());
        assertEquals("FIRST_TRADE", badge.getBadgeType());
        assertEquals(1, sampleUser.getBadges().size());
    }

    // ── Many-to-Many: Watchlist Operations ───────────────────────────────────

    @Test
    @DisplayName("Many-to-Many: User <-> Stock watchlist associations and duplicates handling")
    void testManyToMany_watchlistLifecycle() {
        // 1. Add stock to watchlist
        boolean added = sampleUser.getWatchlist().add(sampleStock);
        sampleStock.getUsersWatching().add(sampleUser);

        assertTrue(added);
        assertTrue(sampleUser.getWatchlist().contains(sampleStock));
        assertTrue(sampleStock.getUsersWatching().contains(sampleUser));
        assertEquals(1, sampleUser.getWatchlist().size());

        // 2. Duplicate addition to Set returns false and maintains size 1
        boolean addedAgain = sampleUser.getWatchlist().add(sampleStock);
        assertFalse(addedAgain);
        assertEquals(1, sampleUser.getWatchlist().size());

        // 3. Remove stock from watchlist
        boolean removed = sampleUser.getWatchlist().remove(sampleStock);
        sampleStock.getUsersWatching().remove(sampleUser);

        assertTrue(removed);
        assertFalse(sampleUser.getWatchlist().contains(sampleStock));
        assertFalse(sampleStock.getUsersWatching().contains(sampleUser));
        assertEquals(0, sampleUser.getWatchlist().size());
    }
}
