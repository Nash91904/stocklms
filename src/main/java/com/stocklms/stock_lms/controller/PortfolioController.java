package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.PortfolioPatchRequestDto;
import com.stocklms.stock_lms.dto.PortfolioResponseDto;
import com.stocklms.stock_lms.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for inspecting and modifying portfolios.
 */
@Tag(name = "Portfolios", description = "View and patch user portfolio cash balances")
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "Get a portfolio by portfolio ID",
               responses = {@ApiResponse(responseCode = "404", description = "Portfolio not found")})
    @GetMapping("/{id}")
    public PortfolioResponseDto getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolioById(id);
    }

    @Operation(summary = "Get a portfolio by user ID",
               responses = {@ApiResponse(responseCode = "404", description = "Portfolio not found")})
    @GetMapping("/user/{userId}")
    public PortfolioResponseDto getPortfolioByUserId(@PathVariable Long userId) {
        return portfolioService.getPortfolioByUserId(userId);
    }

    @Operation(summary = "Patch a portfolio (update cashBalance)",
               responses = {@ApiResponse(responseCode = "404", description = "Portfolio not found"),
                            @ApiResponse(responseCode = "400", description = "Negative cash balance not allowed")})
    @PatchMapping("/{id}")
    public PortfolioResponseDto patchPortfolio(@PathVariable Long id, @Valid @RequestBody PortfolioPatchRequestDto dto) {
        return portfolioService.patchPortfolio(id, dto);
    }
}
