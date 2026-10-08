package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.StockRequestDto;
import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ConflictException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
import com.stocklms.stock_lms.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing stock equities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final TradeRepository tradeRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Transactional(readOnly = true)
    public List<StockResponseDto> getAllStocks() {
        log.info("Fetching all stocks");
        return stockRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockResponseDto getStockById(Long id) {
        log.info("Fetching stock with id: {}", id);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));
        return toResponseDto(stock);
    }

    @Transactional
    public StockResponseDto createStock(StockRequestDto dto) {
        String tickerUpper = dto.getTicker().trim().toUpperCase();
        if (stockRepository.existsByTicker(tickerUpper)) {
            log.warn("Duplicate stock ticker rejected: {}", tickerUpper);
            throw new BusinessRuleException("Stock ticker already exists: " + tickerUpper);
        }

        Stock stock = Stock.builder()
                .ticker(tickerUpper)
                .name(dto.getName().trim())
                .sector(dto.getSector() != null ? dto.getSector().trim() : null)
                .build();

        Stock saved = stockRepository.save(stock);
        log.info("Created stock: {} (id={})", saved.getTicker(), saved.getId());
        return toResponseDto(saved);
    }

    @Transactional
    public StockResponseDto updateStock(Long id, StockRequestDto dto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));

        String tickerUpper = dto.getTicker().trim().toUpperCase();
        if (!stock.getTicker().equalsIgnoreCase(tickerUpper) && stockRepository.existsByTicker(tickerUpper)) {
            log.warn("Duplicate stock ticker on update rejected: {}", tickerUpper);
            throw new BusinessRuleException("Stock ticker already exists: " + tickerUpper);
        }

        stock.setTicker(tickerUpper);
        stock.setName(dto.getName().trim());
        stock.setSector(dto.getSector() != null ? dto.getSector().trim() : null);

        Stock saved = stockRepository.save(stock);
        log.info("Updated stock: {} (id={})", saved.getTicker(), id);
        return toResponseDto(saved);
    }

    @Transactional
    public void deleteStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));

        if (tradeRepository.existsByStockId(id)) {
            log.warn("Cannot delete stock id {} referenced by immutable trade records", id);
            throw new ConflictException("Cannot delete stock with id " + id + " because it is referenced in immutable trade records");
        }

        if (priceHistoryRepository.existsByStockId(id)) {
            log.warn("Cannot delete stock id {} referenced by immutable price history records", id);
            throw new ConflictException("Cannot delete stock with id " + id + " because it is referenced in immutable price history records");
        }

        // Clean up watchlist references from any users watching this stock
        if (stock.getUsersWatching() != null) {
            for (User user : stock.getUsersWatching()) {
                user.getWatchlist().remove(stock);
            }
            stock.getUsersWatching().clear();
        }

        stockRepository.delete(stock);
        log.info("Deleted stock with id: {}", id);
    }

    private StockResponseDto toResponseDto(Stock stock) {
        return StockResponseDto.builder()
                .id(stock.getId())
                .ticker(stock.getTicker())
                .name(stock.getName())
                .sector(stock.getSector())
                .build();
    }
}
