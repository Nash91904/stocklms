package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
@Import(GlobalExceptionHandler.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioRepository portfolioRepository;

    @Test
    void getPortfolioById_success_returns200() throws Exception {
        Portfolio portfolio = Portfolio.builder()
                .id(1L)
                .user(User.builder().id(10L).build())
                .cashBalance(new BigDecimal("50000.00"))
                .build();

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));

        mockMvc.perform(get("/api/portfolios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.cashBalance").value(50000.00));
    }

    @Test
    void getPortfolioById_notFound_returns404() throws Exception {
        when(portfolioRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/portfolios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("Portfolio not found with id: 99"));
    }

    @Test
    void getPortfolioByUserId_success_returns200() throws Exception {
        Portfolio portfolio = Portfolio.builder()
                .id(1L)
                .user(User.builder().id(10L).build())
                .cashBalance(new BigDecimal("50000.00"))
                .build();

        when(portfolioRepository.findByUserId(10L)).thenReturn(Optional.of(portfolio));

        mockMvc.perform(get("/api/portfolios/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(10));
    }

    @Test
    void patchPortfolio_success_returns200() throws Exception {
        Portfolio existing = Portfolio.builder()
                .id(1L)
                .user(User.builder().id(10L).build())
                .cashBalance(new BigDecimal("50000.00"))
                .build();

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(i -> i.getArgument(0));

        String json = """
                {
                    "cashBalance": 75000.00
                }
                """;

        mockMvc.perform(patch("/api/portfolios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.cashBalance").value(75000.00));
    }

    @Test
    void patchPortfolio_negativeBalance_returns400() throws Exception {
        Portfolio existing = Portfolio.builder()
                .id(1L)
                .cashBalance(new BigDecimal("50000.00"))
                .build();

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(existing));

        String json = """
                {
                    "cashBalance": -100.00
                }
                """;

        mockMvc.perform(patch("/api/portfolios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Cash balance cannot be negative"));
    }
}
