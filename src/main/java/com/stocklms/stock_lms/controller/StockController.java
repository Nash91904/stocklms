package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.StockRequestDto;
import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing stocks.
 */
@Tag(name = "Stocks", description = "CRUD operations for stock equities")
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Get all stocks")
    @GetMapping
    public List<StockResponseDto> getAllStocks() {
        return stockService.getAllStocks();
    }

    @Operation(summary = "Get a stock by ID",
               responses = {@ApiResponse(responseCode = "404", description = "Stock not found")})
    @GetMapping("/{id}")
    public StockResponseDto getStockById(@PathVariable Long id) {
        return stockService.getStockById(id);
    }

    @Operation(summary = "Create a new stock",
               responses = {@ApiResponse(responseCode = "201", description = "Stock created")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponseDto createStock(@Valid @RequestBody StockRequestDto dto) {
        return stockService.createStock(dto);
    }

    @Operation(summary = "Full update of a stock",
               responses = {@ApiResponse(responseCode = "404", description = "Stock not found")})
    @PutMapping("/{id}")
    public StockResponseDto updateStock(@PathVariable Long id, @Valid @RequestBody StockRequestDto dto) {
        return stockService.updateStock(id, dto);
    }

    @Operation(summary = "Delete a stock",
               responses = {@ApiResponse(responseCode = "204", description = "Deleted"),
                            @ApiResponse(responseCode = "404", description = "Stock not found")})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
    }
}
