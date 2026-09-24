package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.PortfolioPatchRequestDto;
import com.stocklms.stock_lms.dto.PortfolioResponseDto;
import com.stocklms.stock_lms.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for inspecting and modifying portfolios.
 */
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/{id}")
    public PortfolioResponseDto getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolioById(id);
    }

    @GetMapping("/user/{userId}")
    public PortfolioResponseDto getPortfolioByUserId(@PathVariable Long userId) {
        return portfolioService.getPortfolioByUserId(userId);
    }

    @PatchMapping("/{id}")
    public PortfolioResponseDto patchPortfolio(@PathVariable Long id, @Valid @RequestBody PortfolioPatchRequestDto dto) {
        return portfolioService.patchPortfolio(id, dto);
    }
}
