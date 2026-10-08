package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.TradeRequestDto;
import com.stocklms.stock_lms.dto.TradeResponseDto;
import com.stocklms.stock_lms.entity.TradeType;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.service.TradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
@Import(GlobalExceptionHandler.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeService tradeService;

    @Test
    void executeTrade_success_returns201() throws Exception {
        TradeResponseDto response = TradeResponseDto.builder()
                .id(1L)
                .userId(1L)
                .stockId(10L)
                .type(TradeType.BUY)
                .quantity(5)
                .price(new BigDecimal("150.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(tradeService.executeTrade(any(TradeRequestDto.class))).thenReturn(response);

        String json = """
                {
                    "userId": 1,
                    "stockId": 10,
                    "type": "BUY",
                    "quantity": 5
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.type").value("BUY"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void executeTrade_validationFailure_returns400() throws Exception {
        // Missing required fields and invalid quantity (< 1)
        String invalidJson = """
                {
                    "userId": null,
                    "stockId": null,
                    "quantity": 0
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.userId").exists())
                .andExpect(jsonPath("$.fieldErrors.stockId").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());

        verify(tradeService, never()).executeTrade(any());
    }

    @Test
    void executeTrade_notFound_returns404() throws Exception {
        when(tradeService.executeTrade(any(TradeRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("User", 99L));

        String json = """
                {
                    "userId": 99,
                    "stockId": 10,
                    "type": "BUY",
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("User not found with id: 99"));
    }

    @Test
    void getTradeById_success_returns200() throws Exception {
        TradeResponseDto response = TradeResponseDto.builder()
                .id(1L)
                .userId(1L)
                .stockId(10L)
                .type(TradeType.BUY)
                .quantity(5)
                .price(new BigDecimal("150.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(tradeService.getTradeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/trades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void getTradeById_notFound_returns404() throws Exception {
        when(tradeService.getTradeById(99L)).thenThrow(new ResourceNotFoundException("Trade", 99L));

        mockMvc.perform(get("/api/trades/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void getTradesByUserId_success_returns200() throws Exception {
        when(tradeService.getTradesByUserId(1L)).thenReturn(List.of(
                TradeResponseDto.builder().id(1L).userId(1L).stockId(10L).type(TradeType.BUY).quantity(2).build()
        ));

        mockMvc.perform(get("/api/trades/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
