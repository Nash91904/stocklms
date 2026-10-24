package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.PriceHistory;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
import com.stocklms.stock_lms.responsebean.ResponseBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for recording and querying stock price snapshots.
 * Records are immutable (insert and read only).
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Price History", description = "Record and query stock price snapshots")
@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryRepository priceHistoryRepository;
    private final StockRepository stockRepository;

    @Operation(summary = "Record a stock price snapshot",
               responses = {@ApiResponse(responseCode = "201", description = "Price recorded"),
                            @ApiResponse(responseCode = "404", description = "Stock not found")})
    @PostMapping
    @Transactional
    public ResponseEntity<ResponseBean> recordPrice(@Valid @RequestBody PriceHistory priceHistory) {
        Long stockId = priceHistory.getStock() != null ? priceHistory.getStock().getId() : null;
        if (stockId == null) {
            log.warn("Price recording rejected: Stock ID is required");
            throw new BusinessRuleException("Stock ID is required");
        }

        log.info("Recording price {} for stock id: {}", priceHistory.getPrice(), stockId);
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", stockId));

        priceHistory.setStock(stock);
        if (priceHistory.getTimestamp() == null) {
            priceHistory.setTimestamp(LocalDateTime.now());
        }

        PriceHistory saved = priceHistoryRepository.save(priceHistory);
        log.info("Recorded price {} for stock id: {}", saved.getPrice(), stockId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", saved));
    }

    @Operation(summary = "Get price history by stock ID path",
               responses = {@ApiResponse(responseCode = "404", description = "Stock not found")})
    @GetMapping("/stock/{stockId}")
    public ResponseEntity<ResponseBean> getPricesByStockPath(@PathVariable Long stockId) {
        log.info("Fetching price history for stock id: {}", stockId);
        if (!stockRepository.existsById(stockId)) {
            throw new ResourceNotFoundException("Stock", stockId);
        }

        List<PriceHistory> prices = priceHistoryRepository.findByStockIdOrderByTimestampDesc(stockId);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", prices));
    }

    @Operation(summary = "Get price history by stock ID query parameter",
               responses = {@ApiResponse(responseCode = "400", description = "Parameter 'stockId' is required"),
                            @ApiResponse(responseCode = "404", description = "Stock not found")})
    @GetMapping
    public ResponseEntity<ResponseBean> getPricesByStockQuery(@RequestParam(required = false) Long stockId) {
        if (stockId != null) {
            return getPricesByStockPath(stockId);
        }
        log.warn("Querying prices rejected: Parameter 'stockId' is required");
        throw new BusinessRuleException("Parameter 'stockId' is required");
    }
}
