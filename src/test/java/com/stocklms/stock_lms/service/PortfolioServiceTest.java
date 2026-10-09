package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.PortfolioPatchRequestDto;
import com.stocklms.stock_lms.dto.PortfolioResponseDto;
import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = Portfolio.builder()
                .id(1L)
                .user(User.builder().id(10L).build())
                .cashBalance(new BigDecimal("100000.00"))
                .build();
    }

    @Test
    void getPortfolioById_success() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));

        PortfolioResponseDto response = portfolioService.getPortfolioById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals(new BigDecimal("100000.00"), response.getCashBalance());
    }

    @Test
    void getPortfolioById_notFound_throwsResourceNotFoundException() {
        when(portfolioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> portfolioService.getPortfolioById(99L));
    }

    @Test
    void getPortfolioByUserId_success() {
        when(portfolioRepository.findByUserId(10L)).thenReturn(Optional.of(portfolio));

        PortfolioResponseDto response = portfolioService.getPortfolioByUserId(10L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getUserId());
    }

    @Test
    void getPortfolioByUserId_notFound_throwsResourceNotFoundException() {
        when(portfolioRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> portfolioService.getPortfolioByUserId(99L));
    }

    @Test
    void patchPortfolio_success() {
        PortfolioPatchRequestDto request = PortfolioPatchRequestDto.builder()
                .cashBalance(new BigDecimal("75000.00"))
                .build();

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioResponseDto response = portfolioService.patchPortfolio(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("75000.00"), response.getCashBalance());
        verify(portfolioRepository).save(portfolio);
    }

    @Test
    void patchPortfolio_notFound_throwsResourceNotFoundException() {
        PortfolioPatchRequestDto request = PortfolioPatchRequestDto.builder()
                .cashBalance(new BigDecimal("50000.00"))
                .build();

        when(portfolioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> portfolioService.patchPortfolio(99L, request));
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void patchPortfolio_negativeBalance_throwsBusinessRuleException() {
        PortfolioPatchRequestDto request = PortfolioPatchRequestDto.builder()
                .cashBalance(new BigDecimal("-100.00"))
                .build();

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> portfolioService.patchPortfolio(1L, request));
        assertEquals("Cash balance cannot be negative", ex.getMessage());
        verify(portfolioRepository, never()).save(any());
    }
}
