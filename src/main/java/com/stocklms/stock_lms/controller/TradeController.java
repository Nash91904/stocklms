package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.*;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.*;
import com.stocklms.stock_lms.responsebean.ResponseBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for executing and viewing stock trades.
 * Trades are immutable (audit ledger: insert and read only).
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Trades", description = "Execute BUY/SELL trades and view trade history")
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final StreakRepository streakRepository;
    private final BadgeRepository badgeRepository;

    @Operation(summary = "Execute a BUY or SELL trade",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                               {"user": {"id": 0}, "stock": {"id": 0}, "type": "BUY", "quantity": 1}
                               """)
               )),
               responses = {@ApiResponse(responseCode = "201", description = "Trade executed"),
                            @ApiResponse(responseCode = "400", description = "Insufficient balance/holdings or bad input"),
                            @ApiResponse(responseCode = "404", description = "User or stock not found")})
    @PostMapping
    @Transactional
    public ResponseEntity<ResponseBean> executeTrade(@Valid @RequestBody Trade trade) {
        Long userId = trade.getUser() != null ? trade.getUser().getId() : null;
        Long stockId = trade.getStock() != null ? trade.getStock().getId() : null;

        if (userId == null) {
            log.warn("Trade execution rejected: User ID is required");
            throw new BusinessRuleException("User ID is required");
        }
        if (stockId == null) {
            log.warn("Trade execution rejected: Stock ID is required");
            throw new BusinessRuleException("Stock ID is required");
        }

        log.info("Executing trade for user {} on stock {} (type={}, qty={})",
                userId, stockId, trade.getType(), trade.getQuantity());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", stockId));

        List<PriceHistory> prices = priceHistoryRepository.findByStockIdOrderByTimestampDesc(stockId);
        if (prices.isEmpty()) {
            log.warn("No price history available for stock id: {}", stockId);
            throw new BusinessRuleException("No price history available for stock id: " + stockId);
        }
        BigDecimal currentPrice = prices.get(0).getPrice();

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for user id: " + userId));

        BigDecimal totalTradeAmount = currentPrice.multiply(BigDecimal.valueOf(trade.getQuantity()));

        if (trade.getType() == TradeType.BUY) {
            if (portfolio.getCashBalance().compareTo(totalTradeAmount) < 0) {
                log.warn("Insufficient cash for user {} – required: {}, available: {}",
                        userId, totalTradeAmount, portfolio.getCashBalance());
                throw new BusinessRuleException(
                        "Insufficient cash balance. Required: $" + totalTradeAmount
                        + ", Available: $" + portfolio.getCashBalance());
            }
            portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalTradeAmount));
        } else if (trade.getType() == TradeType.SELL) {
            List<Trade> userTrades = tradeRepository
                    .findByUserIdAndStockIdOrderByTimestampDesc(userId, stockId);
            int currentHolding = userTrades.stream()
                    .mapToInt(t -> t.getType() == TradeType.BUY ? t.getQuantity() : -t.getQuantity())
                    .sum();

            if (currentHolding < trade.getQuantity()) {
                log.warn("Insufficient holdings for user {} on stock {} – owned: {}, tried to sell: {}",
                        userId, stockId, currentHolding, trade.getQuantity());
                throw new BusinessRuleException(
                        "Insufficient stock holdings. Currently owned: " + currentHolding
                        + " shares, cannot sell " + trade.getQuantity());
            }
            portfolio.setCashBalance(portfolio.getCashBalance().add(totalTradeAmount));
        } else {
            throw new BusinessRuleException("Invalid trade type: " + trade.getType());
        }

        portfolioRepository.save(portfolio);

        trade.setUser(user);
        trade.setStock(stock);
        trade.setPrice(currentPrice);
        trade.setTimestamp(LocalDateTime.now());

        Trade saved = tradeRepository.save(trade);
        log.info("Trade executed successfully: id={}", saved.getId());

        // --- Streak: record activity for today ---
        Streak streak = streakRepository.findByUserId(userId).orElse(null);
        if (streak != null) {
            streak.recordActivity(LocalDate.now());
            streakRepository.save(streak);

            // Award STREAK_3 badge when currentStreak >= 3 and not yet awarded
            if (streak.getCurrentStreak() >= 3
                    && !badgeRepository.existsByUserIdAndBadgeType(userId, "STREAK_3")) {
                Badge streakBadge = Badge.builder()
                        .user(user)
                        .badgeType("STREAK_3")
                        .awardedAt(LocalDateTime.now())
                        .build();
                badgeRepository.save(streakBadge);
                log.info("Awarded STREAK_3 badge to user {}", userId);
            }
        }

        // --- Badge: award FIRST_TRADE if the user has no badges yet ---
        if (!badgeRepository.existsByUserIdAndBadgeType(userId, "FIRST_TRADE")) {
            Badge badge = Badge.builder()
                    .user(user)
                    .badgeType("FIRST_TRADE")
                    .awardedAt(LocalDateTime.now())
                    .build();
            badgeRepository.save(badge);
            log.info("Awarded FIRST_TRADE badge to user {}", userId);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", saved));
    }

    @Operation(summary = "Get a trade by ID",
               responses = {@ApiResponse(responseCode = "404", description = "Trade not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBean> getTradeById(@PathVariable Long id) {
        log.info("Fetching trade with id: {}", id);
        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", id));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", trade));
    }

    @Operation(summary = "Get all trades for a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseBean> getTradesByUserId(@PathVariable Long userId) {
        log.info("Fetching trades for user id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        List<Trade> trades = tradeRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", trades));
    }

    @Operation(summary = "Get trades filtered by userId query param",
               description = "Pass ?userId=<id> to filter. The userId param is required.")
    @GetMapping
    public ResponseEntity<ResponseBean> getTradesByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return getTradesByUserId(userId);
        }
        log.warn("Listing trades rejected: missing userId parameter");
        throw new BusinessRuleException("Query parameter 'userId' is required when listing trades");
    }
}
