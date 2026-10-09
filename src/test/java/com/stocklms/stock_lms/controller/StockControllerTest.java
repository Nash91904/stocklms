package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.StockRequestDto;
import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@Import(GlobalExceptionHandler.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    void getAllStocks_returns200() throws Exception {
        StockResponseDto stock = StockResponseDto.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .build();

        when(stockService.getAllStocks()).thenReturn(List.of(stock));

        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].name").value("Apple Inc."));
    }

    @Test
    void getStockById_success_returns200() throws Exception {
        StockResponseDto stock = StockResponseDto.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .build();

        when(stockService.getStockById(1L)).thenReturn(stock);

        mockMvc.perform(get("/api/stocks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ticker").value("AAPL"));
    }

    @Test
    void getStockById_notFound_returns404() throws Exception {
        when(stockService.getStockById(99L)).thenThrow(new ResourceNotFoundException("Stock", 99L));

        mockMvc.perform(get("/api/stocks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Stock not found with id: 99"));
    }

    @Test
    void createStock_success_returns201() throws Exception {
        StockResponseDto response = StockResponseDto.builder()
                .id(1L)
                .ticker("MSFT")
                .name("Microsoft Corporation")
                .sector("Technology")
                .build();

        when(stockService.createStock(any(StockRequestDto.class))).thenReturn(response);

        String json = """
                {
                    "ticker": "MSFT",
                    "name": "Microsoft Corporation",
                    "sector": "Technology"
                }
                """;

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ticker").value("MSFT"));
    }

    @Test
    void createStock_validationFailure_returns400() throws Exception {
        // Missing required ticker and blank name
        String invalidJson = """
                {
                    "ticker": "",
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.ticker").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verify(stockService, never()).createStock(any());
    }

    @Test
    void updateStock_notFound_returns404() throws Exception {
        when(stockService.updateStock(eq(99L), any(StockRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Stock", 99L));

        String json = """
                {
                    "ticker": "AAPL",
                    "name": "Apple Inc."
                }
                """;

        mockMvc.perform(put("/api/stocks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStock_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Stock", 99L)).when(stockService).deleteStock(99L);

        mockMvc.perform(delete("/api/stocks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStock_conflict_returns409() throws Exception {
        doThrow(new com.stocklms.stock_lms.exception.ConflictException("Cannot delete stock with id 1 because it is referenced in immutable trade records"))
                .when(stockService).deleteStock(1L);

        mockMvc.perform(delete("/api/stocks/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }
}
