package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.PriceHistory;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PriceHistoryController.class)
@Import(GlobalExceptionHandler.class)
class PriceHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceHistoryRepository priceHistoryRepository;

    @MockitoBean
    private StockRepository stockRepository;

    @Test
    void recordPrice_success_returns201() throws Exception {
        Stock stock = Stock.builder().id(10L).ticker("AAPL").build();
        PriceHistory saved = PriceHistory.builder()
                .id(1L)
                .stock(stock)
                .price(new BigDecimal("182.50"))
                .volume(5000000L)
                .timestamp(LocalDateTime.now())
                .build();

        when(stockRepository.findById(10L)).thenReturn(Optional.of(stock));
        when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(saved);

        String json = """
                {
                    "stock": {"id": 10},
                    "price": 182.50,
                    "volume": 5000000
                }
                """;

        mockMvc.perform(post("/api/price-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stockId").value(10))
                .andExpect(jsonPath("$.data.price").value(182.50));
    }

    @Test
    void recordPrice_stockNotFound_returns404() throws Exception {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        String json = """
                {
                    "stock": {"id": 99},
                    "price": 182.50,
                    "volume": 5000000
                }
                """;

        mockMvc.perform(post("/api/price-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("Stock not found with id: 99"));
    }

    @Test
    void getPricesByStockPath_success_returns200() throws Exception {
        PriceHistory p = PriceHistory.builder()
                .id(1L)
                .price(new BigDecimal("182.50"))
                .volume(5000000L)
                .build();

        when(stockRepository.existsById(10L)).thenReturn(true);
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/price-history/stock/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getPricesByStockQuery_success_returns200() throws Exception {
        PriceHistory p = PriceHistory.builder()
                .id(1L)
                .price(new BigDecimal("182.50"))
                .volume(5000000L)
                .build();

        when(stockRepository.existsById(10L)).thenReturn(true);
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/price-history?stockId=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getPricesByStockQuery_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/price-history"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Parameter 'stockId' is required"));
    }
}
