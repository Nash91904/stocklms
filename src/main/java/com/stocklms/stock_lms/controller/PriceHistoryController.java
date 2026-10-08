package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.PriceHistoryRequestDto;
import com.stocklms.stock_lms.dto.PriceHistoryResponseDto;
import com.stocklms.stock_lms.service.PriceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Price History", description = "Record and query stock price snapshots")
@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    @Operation(summary = "Record a stock price snapshot",
               responses = {@ApiResponse(responseCode = "201", description = "Price recorded"),
                            @ApiResponse(responseCode = "404", description = "Stock not found")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PriceHistoryResponseDto recordPrice(@Valid @RequestBody PriceHistoryRequestDto dto) {
        return priceHistoryService.recordPrice(dto);
    }

    @Operation(summary = "Get price history by stock ID path",
               responses = {@ApiResponse(responseCode = "404", description = "Stock not found")})
    @GetMapping("/stock/{stockId}")
    public List<PriceHistoryResponseDto> getPricesByStockPath(@PathVariable Long stockId) {
        return priceHistoryService.getPricesByStock(stockId);
    }

    @Operation(summary = "Get price history by stock ID query parameter",
               responses = {@ApiResponse(responseCode = "400", description = "Parameter 'stockId' is required"),
                            @ApiResponse(responseCode = "404", description = "Stock not found")})
    @GetMapping
    public List<PriceHistoryResponseDto> getPricesByStockQuery(@RequestParam(required = false) Long stockId) {
        if (stockId != null) {
            return priceHistoryService.getPricesByStock(stockId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'stockId' is required");
    }
}
