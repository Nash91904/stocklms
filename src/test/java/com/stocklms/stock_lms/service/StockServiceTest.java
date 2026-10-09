package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.StockRequestDto;
import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ConflictException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PriceHistoryRepository;
import com.stocklms.stock_lms.repository.StockRepository;
import com.stocklms.stock_lms.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @InjectMocks
    private StockService stockService;

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = Stock.builder()
                .id(1L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .sector("Technology")
                .usersWatching(new HashSet<>())
                .build();
    }

    @Test
    void getAllStocks_success() {
        when(stockRepository.findAll()).thenReturn(List.of(stock));

        List<StockResponseDto> result = stockService.getAllStocks();

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
    }

    @Test
    void getStockById_success() {
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

        StockResponseDto result = stockService.getStockById(1L);

        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
    }

    @Test
    void getStockById_notFound_throwsResourceNotFoundException() {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockService.getStockById(99L));
    }

    @Test
    void createStock_success() {
        StockRequestDto request = StockRequestDto.builder()
                .ticker("MSFT")
                .name("Microsoft Corporation")
                .sector("Technology")
                .build();

        when(stockRepository.existsByTicker("MSFT")).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> {
            Stock s = i.getArgument(0);
            s.setId(2L);
            return s;
        });

        StockResponseDto response = stockService.createStock(request);

        assertNotNull(response);
        assertEquals("MSFT", response.getTicker());
        assertEquals("Microsoft Corporation", response.getName());
    }

    @Test
    void createStock_duplicateTicker_throwsBusinessRuleException() {
        StockRequestDto request = StockRequestDto.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .build();

        when(stockRepository.existsByTicker("AAPL")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> stockService.createStock(request));
        verify(stockRepository, never()).save(any());
    }

    @Test
    void deleteStock_success() {
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(tradeRepository.existsByStockId(1L)).thenReturn(false);
        when(priceHistoryRepository.existsByStockId(1L)).thenReturn(false);

        stockService.deleteStock(1L);

        verify(stockRepository).delete(stock);
    }

    @Test
    void deleteStock_referencedByTrades_throwsConflictException() {
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(tradeRepository.existsByStockId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> stockService.deleteStock(1L));
        verify(stockRepository, never()).delete(any());
    }

    @Test
    void deleteStock_referencedByPriceHistory_throwsConflictException() {
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(tradeRepository.existsByStockId(1L)).thenReturn(false);
        when(priceHistoryRepository.existsByStockId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> stockService.deleteStock(1L));
        verify(stockRepository, never()).delete(any());
    }
}
