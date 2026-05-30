package com.syariahpulse.common;

import com.syariahpulse.indicator.application.IndicatorCalculationService;
import com.syariahpulse.ranking.application.RankingService;
import com.syariahpulse.scoring.application.ScoringService;
import com.syariahpulse.stock.application.StockDataImportService;
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

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void runNightlyBatch() {
        LocalDate today = LocalDate.now();
        log.info("=== Nightly batch started for {} ===", today);

        try {
            // Step 1: Import Daily Prices
            log.info("[1/4] Importing daily prices...");
            stockDataImportService.importDailyPrices(today);

            // Step 2: Calculate Indicators
            log.info("[2/4] Calculating technical indicators...");
            indicatorCalculationService.calculateForDate(today);

            // Step 3: Calculate Scores
            log.info("[3/4] Calculating scores...");
            scoringService.scoreForDate(today);

            // Step 4: Rank Stocks
            log.info("[4/4] Ranking stocks...");
            rankingService.rankForDate(today);

            log.info("=== Nightly batch completed for {} ===", today);
        } catch (Exception e) {
            log.error("Nightly batch failed for {}: {}", today, e.getMessage(), e);
        }
    }
}
