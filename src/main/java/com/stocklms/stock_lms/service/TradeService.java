package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.TradeRequestDto;
import com.stocklms.stock_lms.dto.TradeResponseDto;
import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.entity.PriceHistory;
import com.stocklms.stock_lms.entity.Trade;
import com.stocklms.stock_lms.entity.TradeType;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing stock trading transactions.
 * Rules:
 * 1. Trade execution updates portfolio cash and saves trade in ONE @Transactional method.
 * 2. Trades are immutable (no update or delete).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    /**
     * Executes a BUY or SELL order at the latest market price.
     * Atomically validates balance/holdings and updates portfolio cash in one transaction.
     */
    @Transactional
    public TradeResponseDto executeTrade(TradeRequestDto dto) {
        // Validate user exists
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResourceNotFoundException("User", dto.getUserId());
        }

        // Validate stock exists
        if (!stockRepository.existsById(dto.getStockId())) {
            throw new ResourceNotFoundException("Stock", dto.getStockId());
        }

        // Fetch latest price snapshot for this stock
        List<PriceHistory> prices = priceHistoryRepository.findByStockIdOrderByTimestampDesc(dto.getStockId());
        if (prices.isEmpty()) {
            log.warn("No price history available for stock id: {}", dto.getStockId());
            throw new BusinessRuleException("No price history available for stock id: " + dto.getStockId());
        }
        BigDecimal currentPrice = prices.get(0).getPrice();

        // Fetch user portfolio
        Portfolio portfolio = portfolioRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Portfolio not found for user id: " + dto.getUserId()));

        BigDecimal totalTradeAmount = currentPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));

        if (dto.getType() == TradeType.BUY) {
            // Check sufficient cash balance
            if (portfolio.getCashBalance().compareTo(totalTradeAmount) < 0) {
                log.warn("Insufficient cash for user {} – required: {}, available: {}",
                        dto.getUserId(), totalTradeAmount, portfolio.getCashBalance());
                throw new BusinessRuleException(
                        "Insufficient cash balance. Required: $" + totalTradeAmount
                        + ", Available: $" + portfolio.getCashBalance());
            }
            // Deduct cash balance
            portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalTradeAmount));

        } else if (dto.getType() == TradeType.SELL) {
            // Compute user's net stock holdings: sum(BUY) - sum(SELL)
            List<Trade> userTrades = tradeRepository
                    .findByUserIdAndStockIdOrderByTimestampDesc(dto.getUserId(), dto.getStockId());
            int currentHolding = userTrades.stream()
                    .mapToInt(t -> t.getType() == TradeType.BUY ? t.getQuantity() : -t.getQuantity())
                    .sum();

            if (currentHolding < dto.getQuantity()) {
                log.warn("Insufficient holdings for user {} on stock {} – owned: {}, tried to sell: {}",
                        dto.getUserId(), dto.getStockId(), currentHolding, dto.getQuantity());
                throw new BusinessRuleException(
                        "Insufficient stock holdings. Currently owned: " + currentHolding
                        + " shares, cannot sell " + dto.getQuantity());
            }
            // Credit cash balance
            portfolio.setCashBalance(portfolio.getCashBalance().add(totalTradeAmount));

        } else {
            throw new BusinessRuleException("Invalid trade type: " + dto.getType());
        }

        // Save updated portfolio cash balance
        portfolioRepository.save(portfolio);

        // Record immutable trade in ledger
        Trade trade = Trade.builder()
                .userId(dto.getUserId())
                .stockId(dto.getStockId())
                .type(dto.getType())
                .quantity(dto.getQuantity())
                .price(currentPrice)
                .timestamp(LocalDateTime.now())
                .build();

        Trade saved = tradeRepository.save(trade);
        log.info("Trade executed – user={} stock={} type={} qty={} price={}",
                dto.getUserId(), dto.getStockId(), dto.getType(), dto.getQuantity(), currentPrice);
        return toResponseDto(saved);
    }

    public TradeResponseDto getTradeById(Long id) {
        log.info("Fetching trade with id: {}", id);
        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", id));
        return toResponseDto(trade);
    }

    public List<TradeResponseDto> getTradesByUserId(Long userId) {
        log.info("Fetching trades for user id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return tradeRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private TradeResponseDto toResponseDto(Trade trade) {
        return TradeResponseDto.builder()
                .id(trade.getId())
                .userId(trade.getUserId())
                .stockId(trade.getStockId())
                .type(trade.getType())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .timestamp(trade.getTimestamp())
                .build();
    }
}
