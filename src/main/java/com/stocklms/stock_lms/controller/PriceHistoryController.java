package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.PriceHistoryRequestDto;
import com.stocklms.stock_lms.dto.PriceHistoryResponseDto;
import com.stocklms.stock_lms.service.PriceHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for recording and querying stock price snapshots.
 * Records are immutable (insert and read only).
 */
@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PriceHistoryResponseDto recordPrice(@Valid @RequestBody PriceHistoryRequestDto dto) {
        return priceHistoryService.recordPrice(dto);
    }

    @GetMapping("/stock/{stockId}")
    public List<PriceHistoryResponseDto> getPricesByStockPath(@PathVariable Long stockId) {
        return priceHistoryService.getPricesByStock(stockId);
    }

    @GetMapping
    public List<PriceHistoryResponseDto> getPricesByStockQuery(@RequestParam(required = false) Long stockId) {
        if (stockId != null) {
            return priceHistoryService.getPricesByStock(stockId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'stockId' is required");
    }
}
