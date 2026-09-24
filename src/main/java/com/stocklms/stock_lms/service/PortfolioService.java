package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.PortfolioPatchRequestDto;
import com.stocklms.stock_lms.dto.PortfolioResponseDto;
import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service managing user investment portfolios and cash balances.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioResponseDto getPortfolioById(Long id) {
        log.info("Fetching portfolio with id: {}", id);
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", id));
        return toResponseDto(portfolio);
    }

    public PortfolioResponseDto getPortfolioByUserId(Long userId) {
        log.info("Fetching portfolio for user id: {}", userId);
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for user id: " + userId));
        return toResponseDto(portfolio);
    }

    public PortfolioResponseDto patchPortfolio(Long id, PortfolioPatchRequestDto dto) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", id));

        if (dto.getCashBalance() != null) {
            if (dto.getCashBalance().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Negative cash balance rejected for portfolio id: {}", id);
                throw new BusinessRuleException("Cash balance cannot be negative");
            }
            portfolio.setCashBalance(dto.getCashBalance());
        }

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Patched portfolio id: {}", id);
        return toResponseDto(saved);
    }

    private PortfolioResponseDto toResponseDto(Portfolio portfolio) {
        return PortfolioResponseDto.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUserId())
                .cashBalance(portfolio.getCashBalance())
                .build();
    }
}
