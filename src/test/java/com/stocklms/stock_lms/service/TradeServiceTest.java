package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.TradeRequestDto;
import com.stocklms.stock_lms.dto.TradeResponseDto;
import com.stocklms.stock_lms.entity.*;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @InjectMocks
    private TradeService tradeService;

    private User testUser;
    private Stock testStock;
    private Portfolio portfolio;
    private PriceHistory priceHistory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("trader@example.com")
                .build();

        testStock = Stock.builder()
                .id(10L)
                .ticker("AAPL")
                .name("Apple Inc.")
                .build();

        portfolio = Portfolio.builder()
                .id(1L)
                .user(testUser)
                .cashBalance(new BigDecimal("1000.00"))
                .build();

        priceHistory = PriceHistory.builder()
                .id(1L)
                .stock(testStock)
                .price(new BigDecimal("100.00"))
                .volume(5000L)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void executeTrade_buy_success() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(1L)
                .stockId(10L)
                .type(TradeType.BUY)
                .quantity(5)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(testStock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L))
                .thenReturn(List.of(priceHistory));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        TradeResponseDto response = tradeService.executeTrade(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(TradeType.BUY, response.getType());
        assertEquals(5, response.getQuantity());
        assertEquals(new BigDecimal("100.00"), response.getPrice());
        assertEquals(new BigDecimal("500.00"), portfolio.getCashBalance());
        verify(portfolioRepository).save(portfolio);
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void executeTrade_sell_success() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(1L)
                .stockId(10L)
                .type(TradeType.SELL)
                .quantity(3)
                .build();

        Trade previousBuy = Trade.builder()
                .id(1L)
                .user(testUser)
                .stock(testStock)
                .type(TradeType.BUY)
                .quantity(5)
                .price(new BigDecimal("90.00"))
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(testStock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L))
                .thenReturn(List.of(priceHistory));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.findByUserIdAndStockIdOrderByTimestampDesc(1L, 10L))
                .thenReturn(List.of(previousBuy));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> {
            Trade t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        TradeResponseDto response = tradeService.executeTrade(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(TradeType.SELL, response.getType());
        assertEquals(3, response.getQuantity());
        assertEquals(new BigDecimal("1300.00"), portfolio.getCashBalance());
        verify(portfolioRepository).save(portfolio);
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void executeTrade_userNotFound_throwsResourceNotFoundException() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(99L)
                .stockId(10L)
                .type(TradeType.BUY)
                .quantity(1)
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tradeService.executeTrade(request));
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void executeTrade_stockNotFound_throwsResourceNotFoundException() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(1L)
                .stockId(999L)
                .type(TradeType.BUY)
                .quantity(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stockRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tradeService.executeTrade(request));
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void executeTrade_noPriceHistory_throwsBusinessRuleException() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(1L)
                .stockId(10L)
                .type(TradeType.BUY)
                .quantity(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(testStock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessRuleException.class, () -> tradeService.executeTrade(request));
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void executeTrade_portfolioNotFound_throwsResourceNotFoundException() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(1L)
                .stockId(10L)
                .type(TradeType.BUY)
                .quantity(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(testStock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L))
                .thenReturn(List.of(priceHistory));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tradeService.executeTrade(request));
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void executeTrade_insufficientBalance_throwsBusinessRuleException() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(1L)
                .stockId(10L)
                .type(TradeType.BUY)
                .quantity(15) // 15 * 100 = 1500 > 1000 cash balance
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(testStock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L))
                .thenReturn(List.of(priceHistory));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> tradeService.executeTrade(request));
        assertTrue(ex.getMessage().contains("Insufficient cash balance"));
        verify(tradeRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void executeTrade_insufficientHoldings_throwsBusinessRuleException() {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(1L)
                .stockId(10L)
                .type(TradeType.SELL)
                .quantity(10) // Tries to sell 10, but only owns 4
                .build();

        Trade previousBuy = Trade.builder()
                .id(1L)
                .user(testUser)
                .stock(testStock)
                .type(TradeType.BUY)
                .quantity(4)
                .price(new BigDecimal("90.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stockRepository.findById(10L)).thenReturn(Optional.of(testStock));
        when(priceHistoryRepository.findByStockIdOrderByTimestampDesc(10L))
                .thenReturn(List.of(priceHistory));
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(portfolio));
        when(tradeRepository.findByUserIdAndStockIdOrderByTimestampDesc(1L, 10L))
                .thenReturn(List.of(previousBuy));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> tradeService.executeTrade(request));
        assertTrue(ex.getMessage().contains("Insufficient stock holdings"));
        verify(tradeRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void getTradeById_success() {
        Trade trade = Trade.builder()
                .id(50L)
                .user(testUser)
                .stock(testStock)
                .type(TradeType.BUY)
                .quantity(2)
                .price(new BigDecimal("100.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(tradeRepository.findById(50L)).thenReturn(Optional.of(trade));

        TradeResponseDto dto = tradeService.getTradeById(50L);
        assertNotNull(dto);
        assertEquals(50L, dto.getId());
    }

    @Test
    void getTradeById_notFound_throwsResourceNotFoundException() {
        when(tradeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tradeService.getTradeById(999L));
    }

    @Test
    void getTradesByUserId_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(tradeRepository.findByUserIdOrderByTimestampDesc(1L))
                .thenReturn(List.of(
                        Trade.builder().id(1L).user(testUser).stock(testStock).type(TradeType.BUY).quantity(2).price(new BigDecimal("50")).build()
                ));

        List<TradeResponseDto> result = tradeService.getTradesByUserId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getTradesByUserId_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> tradeService.getTradesByUserId(99L));
    }
}
