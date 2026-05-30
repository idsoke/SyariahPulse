package com.syariahpulse.indicator.application;

import com.syariahpulse.indicator.domain.TechnicalIndicator;
import com.syariahpulse.indicator.infrastructure.TechnicalIndicatorRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorCalculationService {

    private final StockRepository stockRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final TechnicalIndicatorRepository technicalIndicatorRepository;
    private final IndicatorCalculator indicatorCalculator;

    @Transactional
    public void calculateForDate(LocalDate date) {
        List<Stock> stocks = stockRepository.findByIsSyariahTrue();
        log.info("Calculating indicators for {} syariah stocks on {}", stocks.size(), date);

        for (Stock stock : stocks) {
            try {
                calculateForStock(stock, date);
            } catch (Exception e) {
                log.error("Failed to calculate indicators for stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
    }

    private void calculateForStock(Stock stock, LocalDate date) {
        List<DailyPrice> prices = dailyPriceRepository.findRecentByStockId(stock.getId(), 60);

        if (prices.size() < 20) {
            log.debug("Not enough price data for {}: {} records", stock.getSymbol(), prices.size());
            return;
        }

        List<BigDecimal> closes = prices.stream()
                .map(DailyPrice::getClose)
                .toList()
                .reversed();

        List<Long> volumes = prices.stream()
                .map(DailyPrice::getVolume)
                .toList()
                .reversed();

        BigDecimal rsi14 = indicatorCalculator.calculateRsi(closes, 14);
        BigDecimal ema20 = indicatorCalculator.calculateEma(closes, 20);
        BigDecimal ema50 = closes.size() >= 50 ? indicatorCalculator.calculateEma(closes, 50) : null;
        long avgVolume20 = indicatorCalculator.calculateAvgVolume(volumes, 20);

        TechnicalIndicator indicator = technicalIndicatorRepository
                .findByStockIdAndTradingDate(stock.getId(), date)
                .orElseGet(() -> TechnicalIndicator.builder()
                        .stock(stock)
                        .tradingDate(date)
                        .build());

        indicator.setRsi14(rsi14);
        indicator.setEma20(ema20);
        indicator.setEma50(ema50);
        indicator.setAvgVolume20(avgVolume20);

        technicalIndicatorRepository.save(indicator);
        log.debug("Saved indicators for {}: RSI={}, EMA20={}, EMA50={}, AvgVol={}",
                stock.getSymbol(), rsi14, ema20, ema50, avgVolume20);
    }
}
