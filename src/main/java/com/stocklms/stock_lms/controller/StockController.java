package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ConflictException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
import com.stocklms.stock_lms.repository.TradeRepository;
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

import java.util.List;

/**
 * REST controller for managing stocks.
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Stocks", description = "CRUD operations for stock equities")
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;
    private final TradeRepository tradeRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Operation(summary = "Get all stocks")
    @GetMapping
    public ResponseEntity<ResponseBean> getAllStocks() {
        log.info("Fetching all stocks");
        List<Stock> stocks = stockRepository.findAll();
        return ResponseEntity.ok(ResponseBean.success("200", "Success", stocks));
    }

    @Operation(summary = "Get a stock by ID",
               responses = {@ApiResponse(responseCode = "404", description = "Stock not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBean> getStockById(@PathVariable Long id) {
        log.info("Fetching stock with id: {}", id);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", stock));
    }

    @Operation(summary = "Create a new stock",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                               {"ticker": "string", "name": "string", "sector": "string"}
                               """)
               )),
               responses = {@ApiResponse(responseCode = "201", description = "Stock created"),
                            @ApiResponse(responseCode = "400", description = "Validation or business rule failure")})
    @PostMapping
    @Transactional
    public ResponseEntity<ResponseBean> createStock(@Valid @RequestBody Stock stock) {
        log.info("Creating stock: {}", stock.getTicker());
        String tickerUpper = stock.getTicker().trim().toUpperCase();
        if (stockRepository.existsByTicker(tickerUpper)) {
            log.warn("Duplicate stock ticker rejected: {}", tickerUpper);
            throw new BusinessRuleException("Stock ticker already exists: " + tickerUpper);
        }

        stock.setTicker(tickerUpper);
        stock.setName(stock.getName().trim());
        stock.setSector(stock.getSector() != null ? stock.getSector().trim() : null);

        Stock saved = stockRepository.save(stock);
        log.info("Created stock: {} (id={})", saved.getTicker(), saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", saved));
    }

    @Operation(summary = "Full update of a stock",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                               {"ticker": "string", "name": "string", "sector": "string"}
                               """)
               )),
               responses = {@ApiResponse(responseCode = "200", description = "Stock updated"),
                            @ApiResponse(responseCode = "404", description = "Stock not found"),
                            @ApiResponse(responseCode = "400", description = "Validation or business rule failure")})
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> updateStock(@PathVariable Long id, @Valid @RequestBody Stock stock) {
        log.info("Updating stock with id: {}", id);
        Stock existing = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", id));

        String tickerUpper = stock.getTicker().trim().toUpperCase();
        if (!existing.getTicker().equalsIgnoreCase(tickerUpper) && stockRepository.existsByTicker(tickerUpper)) {
            log.warn("Duplicate stock ticker on update rejected: {}", tickerUpper);
            throw new BusinessRuleException("Stock ticker already exists: " + tickerUpper);
        }

        existing.setTicker(tickerUpper);
        existing.setName(stock.getName().trim());
        existing.setSector(stock.getSector() != null ? stock.getSector().trim() : null);

        Stock saved = stockRepository.save(existing);
        log.info("Updated stock: {} (id={})", saved.getTicker(), id);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", saved));
    }

    @Operation(summary = "Delete a stock",
               responses = {@ApiResponse(responseCode = "200", description = "Stock deleted"),
                            @ApiResponse(responseCode = "404", description = "Stock not found"),
                            @ApiResponse(responseCode = "409", description = "Referenced by immutable records")})
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> deleteStock(@PathVariable Long id) {
        log.info("Deleting stock with id: {}", id);
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
        return ResponseEntity.ok(ResponseBean.success("200", "Success", "Stock with ID " + id + " deleted successfully."));
    }
}
