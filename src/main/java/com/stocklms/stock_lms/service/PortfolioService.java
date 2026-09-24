package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.PortfolioPatchRequestDto;
import com.stocklms.stock_lms.dto.PortfolioResponseDto;
import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/**
 * Service managing user investment portfolios and cash balances.
 */
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioResponseDto getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found with id: " + id));
        return toResponseDto(portfolio);
    }

    public PortfolioResponseDto getPortfolioByUserId(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found for user id: " + userId));
        return toResponseDto(portfolio);
    }

    public PortfolioResponseDto patchPortfolio(Long id, PortfolioPatchRequestDto dto) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found with id: " + id));

        if (dto.getCashBalance() != null) {
            if (dto.getCashBalance().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cash balance cannot be negative");
            }
            portfolio.setCashBalance(dto.getCashBalance());
        }

        return toResponseDto(portfolioRepository.save(portfolio));
    }

    private PortfolioResponseDto toResponseDto(Portfolio portfolio) {
        return PortfolioResponseDto.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUserId())
                .cashBalance(portfolio.getCashBalance())
                .build();
    }
}
