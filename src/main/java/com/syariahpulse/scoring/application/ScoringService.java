package com.syariahpulse.scoring.application;

import com.syariahpulse.indicator.domain.TechnicalIndicator;
import com.syariahpulse.indicator.infrastructure.TechnicalIndicatorRepository;
import com.syariahpulse.scoring.domain.StockScore;
import com.syariahpulse.scoring.infrastructure.StockScoreRepository;
import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.domain.Stock;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import com.syariahpulse.stock.infrastructure.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private static final BigDecimal PRICE_THRESHOLD = BigDecimal.valueOf(300);
    private static final BigDecimal RSI_LOW = BigDecimal.valueOf(40);
    private static final BigDecimal RSI_HIGH = BigDecimal.valueOf(70);

    private final StockRepository stockRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final TechnicalIndicatorRepository technicalIndicatorRepository;
    private final StockScoreRepository stockScoreRepository;

    @Transactional
    public void scoreForDate(LocalDate date) {
        List<Stock> stocks = stockRepository.findByIsSyariahTrue();
        log.info("Scoring {} syariah stocks for {}", stocks.size(), date);

        for (Stock stock : stocks) {
            try {
                scoreStock(stock, date);
            } catch (Exception e) {
                log.error("Failed to score stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
    }

    private void scoreStock(Stock stock, LocalDate date) {
        Optional<DailyPrice> priceOpt = dailyPriceRepository.findByStockIdAndTradingDate(stock.getId(), date);
        Optional<TechnicalIndicator> indicatorOpt = technicalIndicatorRepository.findByStockIdAndTradingDate(stock.getId(), date);

        if (priceOpt.isEmpty() || indicatorOpt.isEmpty()) {
            return;
        }

        DailyPrice price = priceOpt.get();
        TechnicalIndicator indicator = indicatorOpt.get();

        int priceScore = 0;
        int volumeScore = 0;
        int rsiScore = 0;
        int ema20Score = 0;
        int trendScore = 0;

        // Rule 1: Price < 300 → +20
        if (price.getClose().compareTo(PRICE_THRESHOLD) < 0) {
            priceScore = 20;
        }

        // Rule 2: Volume > 2x AvgVolume20 → +30
        if (indicator.getAvgVolume20() != null && indicator.getAvgVolume20() > 0
                && price.getVolume() > 2L * indicator.getAvgVolume20()) {
            volumeScore = 30;
        }

        // Rule 3: RSI between 40 and 70 → +20
        if (indicator.getRsi14() != null
                && indicator.getRsi14().compareTo(RSI_LOW) >= 0
                && indicator.getRsi14().compareTo(RSI_HIGH) <= 0) {
            rsiScore = 20;
        }

        // Rule 4: Close > EMA20 → +15
        if (indicator.getEma20() != null
                && price.getClose().compareTo(indicator.getEma20()) > 0) {
            ema20Score = 15;
        }

        // Rule 5: EMA20 > EMA50 → +15
        if (indicator.getEma20() != null && indicator.getEma50() != null
                && indicator.getEma20().compareTo(indicator.getEma50()) > 0) {
            trendScore = 15;
        }

        int totalScore = priceScore + volumeScore + rsiScore + ema20Score + trendScore;

        StockScore stockScore = stockScoreRepository
                .findByStockIdAndScoringDate(stock.getId(), date)
                .orElseGet(() -> StockScore.builder()
                        .stock(stock)
                        .scoringDate(date)
                        .build());

        stockScore.setScore(totalScore);
        stockScore.setPriceScore(priceScore);
        stockScore.setVolumeScore(volumeScore);
        stockScore.setRsiScore(rsiScore);
        stockScore.setEma20Score(ema20Score);
        stockScore.setTrendScore(trendScore);

        stockScoreRepository.save(stockScore);
    }
}
