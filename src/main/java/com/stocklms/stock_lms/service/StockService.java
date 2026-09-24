package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.StockRequestDto;
import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public List<StockResponseDto> getAllStocks() {
        log.info("Fetching all stocks");
        return stockRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public StockResponseDto getStockById(Long id) {
        log.info("Fetching stock with id: {}", id);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));
        return toResponseDto(stock);
    }

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

    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stock", id);
        }
        stockRepository.deleteById(id);
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
