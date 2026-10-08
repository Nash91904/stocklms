package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.PriceHistoryRequestDto;
import com.stocklms.stock_lms.dto.PriceHistoryResponseDto;
import com.stocklms.stock_lms.entity.PriceHistory;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing historical stock price snapshots.
 * Rule: PriceHistory is immutable (insert and read only, no update or delete).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final StockRepository stockRepository;

    @Transactional
    public PriceHistoryResponseDto recordPrice(PriceHistoryRequestDto dto) {
        Stock stock = stockRepository.findById(dto.getStockId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock", dto.getStockId()));

        PriceHistory priceHistory = PriceHistory.builder()
                .stock(stock)
                .price(dto.getPrice())
                .volume(dto.getVolume())
                .timestamp(LocalDateTime.now())
                .build();

        PriceHistory saved = priceHistoryRepository.save(priceHistory);
        log.info("Recorded price {} for stock id: {}", dto.getPrice(), dto.getStockId());
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PriceHistoryResponseDto> getPricesByStock(Long stockId) {
        log.info("Fetching price history for stock id: {}", stockId);
        if (!stockRepository.existsById(stockId)) {
            throw new ResourceNotFoundException("Stock", stockId);
        }

        return priceHistoryRepository.findByStockIdOrderByTimestampDesc(stockId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private PriceHistoryResponseDto toResponseDto(PriceHistory priceHistory) {
        return PriceHistoryResponseDto.builder()
                .id(priceHistory.getId())
                .stockId(priceHistory.getStock() != null ? priceHistory.getStock().getId() : null)
                .price(priceHistory.getPrice())
                .volume(priceHistory.getVolume())
                .timestamp(priceHistory.getTimestamp())
                .build();
    }
}
