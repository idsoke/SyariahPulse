package com.syariahpulse.scoring;

import com.syariahpulse.indicator.domain.TechnicalIndicator;
import com.syariahpulse.indicator.infrastructure.TechnicalIndicatorRepository;
import com.syariahpulse.scoring.application.ScoringService;
import com.syariahpulse.scoring.domain.StockScore;
import com.syariahpulse.scoring.infrastructure.StockScoreRepository;
import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.domain.Stock;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import com.syariahpulse.stock.infrastructure.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock StockRepository stockRepository;
    @Mock DailyPriceRepository dailyPriceRepository;
    @Mock TechnicalIndicatorRepository technicalIndicatorRepository;
    @Mock StockScoreRepository stockScoreRepository;

    @InjectMocks
    ScoringService scoringService;

    @Test
    void full_score_100_when_all_rules_pass() {
        LocalDate date = LocalDate.now();
        Stock stock = stock(1L, "DILD");

        DailyPrice price = dailyPrice(stock, date, BigDecimal.valueOf(200), 5_000_000L);
        TechnicalIndicator indicator = indicator(stock, date,
                BigDecimal.valueOf(55),   // RSI in [40,70]
                BigDecimal.valueOf(180),  // EMA20 < close
                BigDecimal.valueOf(160),  // EMA50 < EMA20
                2_000_000L               // AvgVol20, volume = 5M > 2x2M=4M
        );

        when(stockRepository.findByIsSyariahTrue()).thenReturn(List.of(stock));
        when(dailyPriceRepository.findByStockIdAndTradingDate(1L, date)).thenReturn(Optional.of(price));
        when(technicalIndicatorRepository.findByStockIdAndTradingDate(1L, date)).thenReturn(Optional.of(indicator));
        when(stockScoreRepository.findByStockIdAndScoringDate(1L, date)).thenReturn(Optional.empty());
        when(stockScoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scoringService.scoreForDate(date);

        ArgumentCaptor<StockScore> captor = ArgumentCaptor.forClass(StockScore.class);
        verify(stockScoreRepository).save(captor.capture());
        StockScore saved = captor.getValue();

        assertThat(saved.getScore()).isEqualTo(100);
        assertThat(saved.getPriceScore()).isEqualTo(20);
        assertThat(saved.getVolumeScore()).isEqualTo(30);
        assertThat(saved.getRsiScore()).isEqualTo(20);
        assertThat(saved.getEma20Score()).isEqualTo(15);
        assertThat(saved.getTrendScore()).isEqualTo(15);
    }

    @Test
    void score_0_when_price_above_300_and_no_other_signals() {
        LocalDate date = LocalDate.now();
        Stock stock = stock(2L, "BBCA");

        DailyPrice price = dailyPrice(stock, date, BigDecimal.valueOf(9000), 500_000L);
        TechnicalIndicator indicator = indicator(stock, date,
                BigDecimal.valueOf(80),    // RSI > 70 → rsiScore = 0
                BigDecimal.valueOf(9500),  // EMA20 > close → ema20Score = 0
                BigDecimal.valueOf(10000), // EMA50 > EMA20 → trendScore = 0
                1_000_000L                 // volume 500K < 2x1M → volumeScore = 0
        );

        when(stockRepository.findByIsSyariahTrue()).thenReturn(List.of(stock));
        when(dailyPriceRepository.findByStockIdAndTradingDate(2L, date)).thenReturn(Optional.of(price));
        when(technicalIndicatorRepository.findByStockIdAndTradingDate(2L, date)).thenReturn(Optional.of(indicator));
        when(stockScoreRepository.findByStockIdAndScoringDate(2L, date)).thenReturn(Optional.empty());
        when(stockScoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scoringService.scoreForDate(date);

        ArgumentCaptor<StockScore> captor = ArgumentCaptor.forClass(StockScore.class);
        verify(stockScoreRepository).save(captor.capture());
        assertThat(captor.getValue().getScore()).isEqualTo(0);
    }

    private Stock stock(Long id, String symbol) {
        return Stock.builder().id(id).symbol(symbol).companyName("Test").isSyariah(true).build();
    }

    private DailyPrice dailyPrice(Stock stock, LocalDate date, BigDecimal close, long volume) {
        return DailyPrice.builder()
                .stock(stock).tradingDate(date)
                .open(close).high(close).low(close).close(close).volume(volume)
                .build();
    }

    private TechnicalIndicator indicator(Stock stock, LocalDate date,
                                         BigDecimal rsi, BigDecimal ema20, BigDecimal ema50, long avgVol) {
        return TechnicalIndicator.builder()
                .stock(stock).tradingDate(date)
                .rsi14(rsi).ema20(ema20).ema50(ema50).avgVolume20(avgVol)
                .build();
    }
}
