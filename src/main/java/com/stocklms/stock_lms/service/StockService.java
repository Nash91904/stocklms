package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.StockRequestDto;
import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing stock equities.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public List<StockResponseDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public StockResponseDto getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found with id: " + id));
        return toResponseDto(stock);
    }

    public StockResponseDto createStock(StockRequestDto dto) {
        String tickerUpper = dto.getTicker().trim().toUpperCase();
        if (stockRepository.existsByTicker(tickerUpper)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock ticker already exists: " + tickerUpper);
        }

        Stock stock = Stock.builder()
                .ticker(tickerUpper)
                .name(dto.getName().trim())
                .sector(dto.getSector() != null ? dto.getSector().trim() : null)
                .build();

        return toResponseDto(stockRepository.save(stock));
    }

    public StockResponseDto updateStock(Long id, StockRequestDto dto) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found with id: " + id));

        String tickerUpper = dto.getTicker().trim().toUpperCase();
        if (!stock.getTicker().equalsIgnoreCase(tickerUpper) && stockRepository.existsByTicker(tickerUpper)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock ticker already exists: " + tickerUpper);
        }

        stock.setTicker(tickerUpper);
        stock.setName(dto.getName().trim());
        stock.setSector(dto.getSector() != null ? dto.getSector().trim() : null);

        return toResponseDto(stockRepository.save(stock));
    }

    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found with id: " + id);
        }
        stockRepository.deleteById(id);
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
