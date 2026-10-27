package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PortfolioRepository;
import com.stocklms.stock_lms.responsebean.ResponseBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for inspecting and modifying portfolios.
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Portfolios", description = "View and patch user portfolio cash balances")
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;

    @Operation(summary = "Get a portfolio by portfolio ID",
               responses = {@ApiResponse(responseCode = "404", description = "Portfolio not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBean> getPortfolioById(@PathVariable Long id) {
        log.info("Fetching portfolio with id: {}", id);
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", id));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", portfolio));
    }

    @Operation(summary = "Get a portfolio by user ID",
               responses = {@ApiResponse(responseCode = "404", description = "Portfolio not found")})
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseBean> getPortfolioByUserId(@PathVariable Long userId) {
        log.info("Fetching portfolio for user id: {}", userId);
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for user id: " + userId));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", portfolio));
    }

    @Operation(summary = "Patch a portfolio (update cashBalance)",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                               {"cashBalance": 100000}
                               """)
               )),
               responses = {@ApiResponse(responseCode = "200", description = "Portfolio patched"),
                            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
                            @ApiResponse(responseCode = "400", description = "Negative cash balance not allowed")})
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> patchPortfolio(@PathVariable Long id, @RequestBody Portfolio portfolio) {
        log.info("Patching portfolio with id: {}", id);
        Portfolio existing = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", id));

        if (portfolio.getCashBalance() != null) {
            if (portfolio.getCashBalance().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Negative cash balance rejected for portfolio id: {}", id);
                throw new BusinessRuleException("Cash balance cannot be negative");
            }
            existing.setCashBalance(portfolio.getCashBalance());
        }

        Portfolio saved = portfolioRepository.save(existing);
        log.info("Patched portfolio id: {}", id);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", saved));
    }
}
