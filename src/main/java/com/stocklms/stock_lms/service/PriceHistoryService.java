package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.PriceHistoryRequestDto;
import com.stocklms.stock_lms.dto.PriceHistoryResponseDto;
import com.stocklms.stock_lms.entity.PriceHistory;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing historical stock price snapshots.
 * Rule: PriceHistory is immutable (insert and read only, no update or delete).
 */
@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final StockRepository stockRepository;

    public PriceHistoryResponseDto recordPrice(PriceHistoryRequestDto dto) {
        if (!stockRepository.existsById(dto.getStockId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found with id: " + dto.getStockId());
        }

        PriceHistory priceHistory = PriceHistory.builder()
                .stockId(dto.getStockId())
                .price(dto.getPrice())
                .volume(dto.getVolume())
                .timestamp(LocalDateTime.now())
                .build();

        return toResponseDto(priceHistoryRepository.save(priceHistory));
    }

    public List<PriceHistoryResponseDto> getPricesByStock(Long stockId) {
        if (!stockRepository.existsById(stockId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found with id: " + stockId);
        }

        return priceHistoryRepository.findByStockIdOrderByTimestampDesc(stockId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private PriceHistoryResponseDto toResponseDto(PriceHistory priceHistory) {
        return PriceHistoryResponseDto.builder()
                .id(priceHistory.getId())
                .stockId(priceHistory.getStockId())
                .price(priceHistory.getPrice())
                .volume(priceHistory.getVolume())
                .timestamp(priceHistory.getTimestamp())
                .build();
    }
}
