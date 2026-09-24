package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.TradeRequestDto;
import com.stocklms.stock_lms.dto.TradeResponseDto;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for executing and viewing stock trades.
 * Trades are immutable (audit ledger: insert and read only).
 */
@Tag(name = "Trades", description = "Execute BUY/SELL trades and view trade history")
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @Operation(summary = "Execute a BUY or SELL trade",
               responses = {@ApiResponse(responseCode = "201", description = "Trade executed"),
                            @ApiResponse(responseCode = "400", description = "Insufficient balance/holdings or bad input"),
                            @ApiResponse(responseCode = "404", description = "User or stock not found")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponseDto executeTrade(@Valid @RequestBody TradeRequestDto dto) {
        return tradeService.executeTrade(dto);
    }

    @Operation(summary = "Get a trade by ID",
               responses = {@ApiResponse(responseCode = "404", description = "Trade not found")})
    @GetMapping("/{id}")
    public TradeResponseDto getTradeById(@PathVariable Long id) {
        return tradeService.getTradeById(id);
    }

    @Operation(summary = "Get all trades for a specific user")
    @GetMapping("/user/{userId}")
    public List<TradeResponseDto> getTradesByUserId(@PathVariable Long userId) {
        return tradeService.getTradesByUserId(userId);
    }

    @Operation(summary = "Get trades filtered by userId query param",
               description = "Pass ?userId=<id> to filter. The userId param is required.")
    @GetMapping
    public List<TradeResponseDto> getTradesByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return tradeService.getTradesByUserId(userId);
        }
        throw new BusinessRuleException("Query parameter 'userId' is required when listing trades");
    }
}
