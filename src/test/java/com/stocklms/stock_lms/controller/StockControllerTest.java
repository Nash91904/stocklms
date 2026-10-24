package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.GlobalExceptionHandler;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
import com.stocklms.stock_lms.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@Import(GlobalExceptionHandler.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockRepository stockRepository;

    @MockitoBean
    private TradeRepository tradeRepository;

    @MockitoBean
    private PriceHistoryRepository priceHistoryRepository;

    @Test
    void getAllStocks_returns200() throws Exception {
        Stock stock = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .build();

        when(stockRepository.findAll()).thenReturn(List.of(stock));

        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$.data[0].name").value("Apple Inc."));
    }

    @Test
    void getStockById_success_returns200() throws Exception {
        Stock stock = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .build();

        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

        mockMvc.perform(get("/api/stocks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.ticker").value("AAPL"));
    }

    @Test
    void getStockById_notFound_returns404() throws Exception {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stocks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.errorDesc").value("Stock not found with id: 99"));
    }

    @Test
    void createStock_success_returns201() throws Exception {
        Stock saved = Stock.builder()
                .id(1L)
                .ticker("MSFT")
                .name("Microsoft Corporation")
                .sector("Technology")
                .build();

        when(stockRepository.existsByTicker("MSFT")).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenReturn(saved);

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
                .andExpect(jsonPath("$.errorCode").value("201"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.ticker").value("MSFT"));
    }

    @Test
    void createStock_duplicateTicker_returns400() throws Exception {
        when(stockRepository.existsByTicker("AAPL")).thenReturn(true);

        String json = """
                {
                    "ticker": "AAPL",
                    "name": "Apple Inc.",
                    "sector": "Technology"
                }
                """;

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Stock ticker already exists: AAPL"));

        verify(stockRepository, never()).save(any());
    }

    @Test
    void createStock_validationFailure_returns400() throws Exception {
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
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Validation failed"))
                .andExpect(jsonPath("$.data.ticker").exists())
                .andExpect(jsonPath("$.data.name").exists());

        verify(stockRepository, never()).save(any());
    }

    @Test
    void updateStock_success_returns200() throws Exception {
        Stock existing = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Old Name")
                .sector("Technology")
                .build();

        when(stockRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        String json = """
                {
                    "ticker": "AAPL",
                    "name": "Apple Inc.",
                    "sector": "Technology"
                }
                """;

        mockMvc.perform(put("/api/stocks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data.name").value("Apple Inc."));
    }

    @Test
    void updateStock_notFound_returns404() throws Exception {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        String json = """
                {
                    "ticker": "AAPL",
                    "name": "Apple Inc."
                }
                """;

        mockMvc.perform(put("/api/stocks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    void deleteStock_success_returns200() throws Exception {
        Stock stock = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .usersWatching(new HashSet<>())
                .build();

        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(tradeRepository.existsByStockId(1L)).thenReturn(false);
        when(priceHistoryRepository.existsByStockId(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/stocks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.errorDesc").value("Success"))
                .andExpect(jsonPath("$.data").value("Stock with ID 1 deleted successfully."));

        verify(stockRepository).delete(stock);
    }

    @Test
    void deleteStock_notFound_returns404() throws Exception {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/stocks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"));
    }

    @Test
    void deleteStock_conflict_trades_returns409() throws Exception {
        Stock stock = Stock.builder().id(1L).ticker("AAPL").build();
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(tradeRepository.existsByStockId(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/stocks/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("409"))
                .andExpect(jsonPath("$.errorDesc").value("Cannot delete stock with id 1 because it is referenced in immutable trade records"));

        verify(stockRepository, never()).delete(any());
    }

    @Test
    void deleteStock_conflict_priceHistory_returns409() throws Exception {
        Stock stock = Stock.builder().id(1L).ticker("AAPL").build();
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(tradeRepository.existsByStockId(1L)).thenReturn(false);
        when(priceHistoryRepository.existsByStockId(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/stocks/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("409"))
                .andExpect(jsonPath("$.errorDesc").value("Cannot delete stock with id 1 because it is referenced in immutable price history records"));

        verify(stockRepository, never()).delete(any());
    }

    @Test
    void malformedJson_returns400() throws Exception {
        String malformed = "{ticker: broken";

        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.errorDesc").value("Malformed or unreadable request body"));
    }
}
