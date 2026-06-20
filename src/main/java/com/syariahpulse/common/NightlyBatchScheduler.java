package com.syariahpulse.common;

import com.syariahpulse.indicator.application.IndicatorCalculationService;
import com.syariahpulse.ranking.application.RankingService;
import com.syariahpulse.scoring.application.ScoringService;
import com.syariahpulse.stock.application.StockDataImportService;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NightlyBatchScheduler {

    private final StockDataImportService stockDataImportService;
    private final IndicatorCalculationService indicatorCalculationService;
    private final ScoringService scoringService;
    private final RankingService rankingService;
    private final DailyPriceRepository dailyPriceRepository;

    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "Asia/Jakarta")
    public void runNightlyBatch() {
        runBatch(LocalDate.now());
    }

    /**
     * Runs the full pipeline. The actual trading date used for indicators/scoring/ranking
     * is whatever the import step finds as the latest available bar — not necessarily
     * `requestedDate` — so a delayed data feed or a weekend/holiday manual run doesn't
     * silently produce empty results for a date with no data.
     */
    public void runBatch(LocalDate requestedDate) {
        log.info("=== Nightly batch started (requested date {}) ===", requestedDate);

        try {
            // Step 1: Import Daily Prices
            log.info("[1/4] Importing daily prices...");
            stockDataImportService.importDailyPrices(requestedDate);

            LocalDate tradingDate = dailyPriceRepository.findLatestTradingDate().orElse(requestedDate);
            log.info("Using latest available trading date: {}", tradingDate);

            // Step 2: Calculate Indicators
            log.info("[2/4] Calculating technical indicators...");
            indicatorCalculationService.calculateForDate(tradingDate);

            // Step 3: Calculate Scores
            log.info("[3/4] Calculating scores...");
            scoringService.scoreForDate(tradingDate);

            // Step 4: Rank Stocks
            log.info("[4/4] Ranking stocks...");
            rankingService.rankForDate(tradingDate);

            log.info("=== Nightly batch completed for {} ===", tradingDate);
        } catch (Exception e) {
            log.error("Nightly batch failed (requested date {}): {}", requestedDate, e.getMessage(), e);
        }
    }
}
