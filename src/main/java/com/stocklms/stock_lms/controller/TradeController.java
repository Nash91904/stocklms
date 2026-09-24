package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.TradeRequestDto;
import com.stocklms.stock_lms.dto.TradeResponseDto;
import com.stocklms.stock_lms.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for executing and viewing stock trades.
 * Trades are immutable (audit ledger: insert and read only).
 */
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponseDto executeTrade(@Valid @RequestBody TradeRequestDto dto) {
        return tradeService.executeTrade(dto);
    }

    @GetMapping("/{id}")
    public TradeResponseDto getTradeById(@PathVariable Long id) {
        return tradeService.getTradeById(id);
    }

    @GetMapping("/user/{userId}")
    public List<TradeResponseDto> getTradesByUserId(@PathVariable Long userId) {
        return tradeService.getTradesByUserId(userId);
    }

    @GetMapping
    public List<TradeResponseDto> getTradesByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return tradeService.getTradesByUserId(userId);
        }
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Parameter 'userId' is required when listing trades");
    }
}
