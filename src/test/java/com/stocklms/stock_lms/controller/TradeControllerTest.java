package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.*;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
@Import(GlobalExceptionHandler.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeRepository tradeRepository;

    @MockitoBean
    private PortfolioRepository portfolioRepository;

    @MockitoBean
    private StockRepository stockRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PriceHistoryRepository priceHistoryRepository;

    @MockitoBean
    private StreakRepository streakRepository;

    @MockitoBean
    private BadgeRepository badgeRepository;

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void stubBuySuccess(User user, Stock stock, BigDecimal price, Portfolio portfolio) {
        PriceHistory ph = PriceHistory.builder().stock(stock).price(price).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(stock.getId()))
                .thenReturn(List.of(ph));
        when(portfolioRepository.findByUserId(user.getId())).thenReturn(Optional.of(portfolio));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(i -> {
            Trade t = i.getArgument(0);
            t.setId(100L);
            return t;
        });
        // badge / streak defaults: no streak, no existing badges
        when(streakRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(badgeRepository.existsByUserIdAndBadgeType(eq(user.getId()), anyString())).thenReturn(false);
    }

    // ── Existing tests (updated with streak/badge mock stubs) ───────────────

    @Test
    void executeTrade_BUY_success_returns201() throws Exception {
        User user = User.builder().id(1L).email("alex@example.com").build();
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        Portfolio portfolio = Portfolio.builder().user(user).cashBalance(new BigDecimal("10000.00")).build();
        stubBuySuccess(user, stock, new BigDecimal("150.00"), portfolio);

        String json = """
                {
                    "user": {"id": 1},
                    "stock": {"id": 10},
                    "type": "BUY",
                    "quantity": 5
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.stockId").value(10))
                .andExpect(jsonPath("$.data.type").value("BUY"))
                .andExpect(jsonPath("$.data.quantity").value(5))
                .andExpect(jsonPath("$.data.price").value(150.00));

        // Portfolio cash deducted: 10000 - (150 * 5) = 9250
        verify(portfolioRepository).save(argThat(p -> p.getCashBalance().compareTo(new BigDecimal("9250.00")) == 0));
    }

    @Test
    void executeTrade_BUY_insufficientCash_returns400() throws Exception {
        User user = User.builder().id(1L).build();
        Stock stock = Stock.builder().id(10L).build();
        PriceHistory price = PriceHistory.builder().stock(stock).price(new BigDecimal("150.00")).build();
        Portfolio portfolio = Portfolio.builder().user(user).cashBalance(new BigDecimal("100.00")).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(price));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));

        String json = """
                {
                    "user": {"id": 1},
                    "stock": {"id": 10},
                    "type": "BUY",
                    "quantity": 5
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value(org.hamcrest.Matchers.containsString("Insufficient cash balance")));

        verify(tradeRepository, never()).save(any());
    }

    @Test
    void executeTrade_SELL_success_returns201() throws Exception {
        User user = User.builder().id(1L).build();
        Stock stock = Stock.builder().id(10L).build();
        PriceHistory price = PriceHistory.builder().stock(stock).price(new BigDecimal("200.00")).build();
        Portfolio portfolio = Portfolio.builder().user(user).cashBalance(new BigDecimal("1000.00")).build();
        Trade pastBuy = Trade.builder().type(TradeType.BUY).quantity(10).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(price));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.findByUserIdAndStockIdOrderByTimestampDesc(1L, 10L)).thenReturn(List.of(pastBuy));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(i -> {
            Trade t = i.getArgument(0);
            t.setId(101L);
            return t;
        });
        when(streakRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(badgeRepository.existsByUserIdAndBadgeType(eq(1L), anyString())).thenReturn(false);

        String json = """
                {
                    "user": {"id": 1},
                    "stock": {"id": 10},
                    "type": "SELL",
                    "quantity": 4
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.type").value("SELL"))
                .andExpect(jsonPath("$.data.quantity").value(4));

        // Portfolio cash credited: 1000 + (200 * 4) = 1800
        verify(portfolioRepository).save(argThat(p -> p.getCashBalance().compareTo(new BigDecimal("1800.00")) == 0));
    }

    @Test
    void executeTrade_SELL_insufficientHoldings_returns400() throws Exception {
        User user = User.builder().id(1L).build();
        Stock stock = Stock.builder().id(10L).build();
        PriceHistory price = PriceHistory.builder().stock(stock).price(new BigDecimal("200.00")).build();
        Portfolio portfolio = Portfolio.builder().user(user).cashBalance(new BigDecimal("1000.00")).build();
        Trade pastBuy = Trade.builder().type(TradeType.BUY).quantity(2).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(price));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.findByUserIdAndStockIdOrderByTimestampDesc(1L, 10L)).thenReturn(List.of(pastBuy));

        String json = """
                {
                    "user": {"id": 1},
                    "stock": {"id": 10},
                    "type": "SELL",
                    "quantity": 5
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value(org.hamcrest.Matchers.containsString("Insufficient stock holdings")));

        verify(tradeRepository, never()).save(any());
    }

    @Test
    void executeTrade_noPriceHistory_returns400() throws Exception {
        User user = User.builder().id(1L).build();
        Stock stock = Stock.builder().id(10L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(Collections.emptyList());

        String json = """
                {
                    "user": {"id": 1},
                    "stock": {"id": 10},
                    "type": "BUY",
                    "quantity": 5
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("No price history available for stock id: 10"));
    }

    @Test
    void executeTrade_validationFailure_returns400() throws Exception {
        // Quantity is 0 (< 1) and missing trade type
        String invalidJson = """
                {
                    "quantity": 0
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Validation failed"))
                .andExpect(jsonPath("$.data.quantity").exists());

        verify(tradeRepository, never()).save(any());
    }

    @Test
    void executeTrade_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String json = """
                {
                    "user": {"id": 99},
                    "stock": {"id": 10},
                    "type": "BUY",
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("User not found with id: 99"));
    }

    @Test
    void getTradeById_success_returns200() throws Exception {
        Trade trade = Trade.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .stock(Stock.builder().id(10L).build())
                .type(TradeType.BUY)
                .quantity(5)
                .price(new BigDecimal("150.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(tradeRepository.findById(1L)).thenReturn(Optional.of(trade));

        mockMvc.perform(get("/api/trades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.stockId").value(10))
                .andExpect(jsonPath("$.data.quantity").value(5));
    }

    @Test
    void getTradeById_notFound_returns404() throws Exception {
        when(tradeRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trades/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("Trade not found with id: 99"));
    }

    @Test
    void getTradesByUserId_success_returns200() throws Exception {
        Trade trade = Trade.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .stock(Stock.builder().id(10L).build())
                .type(TradeType.BUY)
                .quantity(2)
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(tradeRepository.findByUserIdOrderByTimestampDesc(1L)).thenReturn(List.of(trade));

        mockMvc.perform(get("/api/trades/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getTradesByUserQuery_success_returns200() throws Exception {
        Trade trade = Trade.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .stock(Stock.builder().id(10L).build())
                .type(TradeType.BUY)
                .quantity(2)
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(tradeRepository.findByUserIdOrderByTimestampDesc(1L)).thenReturn(List.of(trade));

        mockMvc.perform(get("/api/trades?userId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getTradesByUserQuery_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Query parameter 'userId' is required when listing trades"));
    }

    // ── New: streak behaviour tests ─────────────────────────────────────────

    @Test
    void executeTrade_streakIncrement_consecutiveDay() throws Exception {
        User user = User.builder().id(2L).email("bob@example.com").build();
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        Portfolio portfolio = Portfolio.builder().user(user).cashBalance(new BigDecimal("5000.00")).build();

        // Streak with lastActiveDate = yesterday, currentStreak = 1
        Streak streak = Streak.builder()
                .id(1L).user(user)
                .currentStreak(1).longestStreak(1)
                .lastActiveDate(LocalDate.now().minusDays(1))
                .freezeCount(0).build();

        PriceHistory ph = PriceHistory.builder().stock(stock).price(new BigDecimal("100.00")).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(ph));
        when(portfolioRepository.findByUserId(2L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(i -> { Trade t = i.getArgument(0); t.setId(200L); return t; });
        when(streakRepository.findByUserId(2L)).thenReturn(Optional.of(streak));
        when(badgeRepository.existsByUserIdAndBadgeType(eq(2L), anyString())).thenReturn(false);

        String json = """
                {"user":{"id":2},"stock":{"id":10},"type":"BUY","quantity":1}
                """;

        mockMvc.perform(post("/api/trades").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        // Streak should have been updated to currentStreak=2 and saved
        verify(streakRepository).save(argThat(s -> s.getCurrentStreak() == 2));
    }

    @Test
    void executeTrade_streakReset_afterGap() throws Exception {
        User user = User.builder().id(3L).email("carol@example.com").build();
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        Portfolio portfolio = Portfolio.builder().user(user).cashBalance(new BigDecimal("5000.00")).build();

        // Streak with lastActiveDate = 5 days ago → gap → reset to 1
        Streak streak = Streak.builder()
                .id(2L).user(user)
                .currentStreak(5).longestStreak(5)
                .lastActiveDate(LocalDate.now().minusDays(5))
                .freezeCount(0).build();

        PriceHistory ph = PriceHistory.builder().stock(stock).price(new BigDecimal("100.00")).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(ph));
        when(portfolioRepository.findByUserId(3L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(i -> { Trade t = i.getArgument(0); t.setId(201L); return t; });
        when(streakRepository.findByUserId(3L)).thenReturn(Optional.of(streak));
        when(badgeRepository.existsByUserIdAndBadgeType(eq(3L), anyString())).thenReturn(false);

        String json = """
                {"user":{"id":3},"stock":{"id":10},"type":"BUY","quantity":1}
                """;

        mockMvc.perform(post("/api/trades").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        // currentStreak should be reset to 1
        verify(streakRepository).save(argThat(s -> s.getCurrentStreak() == 1));
    }

    @Test
    void executeTrade_firstTradeBadge_awardedOnce() throws Exception {
        User user = User.builder().id(4L).email("dave@example.com").build();
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        Portfolio portfolio = Portfolio.builder().user(user).cashBalance(new BigDecimal("5000.00")).build();

        PriceHistory ph = PriceHistory.builder().stock(stock).price(new BigDecimal("100.00")).build();
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(ph));
        when(portfolioRepository.findByUserId(4L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(i -> { Trade t = i.getArgument(0); t.setId(202L); return t; });
        when(streakRepository.findByUserId(4L)).thenReturn(Optional.empty());
        // Simulate FIRST_TRADE already awarded
        when(badgeRepository.existsByUserIdAndBadgeType(4L, "FIRST_TRADE")).thenReturn(true);
        when(badgeRepository.existsByUserIdAndBadgeType(4L, "STREAK_3")).thenReturn(false);

        String json = """
                {"user":{"id":4},"stock":{"id":10},"type":"BUY","quantity":1}
                """;

        mockMvc.perform(post("/api/trades").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        // FIRST_TRADE badge must NOT be saved again
        verify(badgeRepository, never()).save(argThat(b -> "FIRST_TRADE".equals(b.getBadgeType())));
    }
}
