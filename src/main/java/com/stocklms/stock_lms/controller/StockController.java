package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.StockRequestDto;
import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing stocks.
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StockResponseDto> getAllStocks() {
        return stockService.getAllStocks();
    }

    @GetMapping("/{id}")
    public StockResponseDto getStockById(@PathVariable Long id) {
        return stockService.getStockById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponseDto createStock(@Valid @RequestBody StockRequestDto dto) {
        return stockService.createStock(dto);
    }

    @PutMapping("/{id}")
    public StockResponseDto updateStock(@PathVariable Long id, @Valid @RequestBody StockRequestDto dto) {
        return stockService.updateStock(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
    }
}
