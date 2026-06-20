package com.syariahpulse.stock.application;

import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.domain.Stock;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import com.syariahpulse.stock.infrastructure.StockRepository;
import com.syariahpulse.stock.infrastructure.YahooFinanceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Imports daily OHLCV data from Yahoo Finance (.JK tickers) for each
 * syariah stock. Backfills history on first run so indicators (EMA50,
 * RSI14, AvgVolume20) have enough data points, then pulls a short
 * incremental range on later runs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataImportService {

    private static final int MIN_HISTORY_DAYS = 60;
    private static final String BACKFILL_RANGE = "6mo";
    private static final String INCREMENTAL_RANGE = "5d";
    private static final long REQUEST_DELAY_MS = 300;

    private final StockRepository stockRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final YahooFinanceClient yahooFinanceClient;

    public void importDailyPrices(LocalDate date) {
        List<Stock> stocks = stockRepository.findByIsSyariahTrue();
        log.info("Importing daily prices for {} syariah stocks on {}", stocks.size(), date);

        for (Stock stock : stocks) {
            try {
                importForStock(stock);
            } catch (Exception e) {
                log.error("Failed to import prices for {}: {}", stock.getSymbol(), e.getMessage());
            }
            sleepBetweenRequests();
        }
    }

    private void sleepBetweenRequests() {
        try {
            Thread.sleep(REQUEST_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    void importForStock(Stock stock) {
        long existingCount = dailyPriceRepository.countByStockId(stock.getId());
        String range = existingCount < MIN_HISTORY_DAYS ? BACKFILL_RANGE : INCREMENTAL_RANGE;

        List<YahooFinanceClient.DailyOhlcv> history = yahooFinanceClient.fetchDailyHistory(stock.getSymbol(), range);
        if (history.isEmpty()) {
            log.warn("No data returned from Yahoo Finance for {}", stock.getSymbol());
            return;
        }

        int saved = 0;
        for (YahooFinanceClient.DailyOhlcv bar : history) {
            if (dailyPriceRepository.findByStockIdAndTradingDate(stock.getId(), bar.tradingDate()).isPresent()) {
                continue;
            }
            dailyPriceRepository.save(DailyPrice.builder()
                    .stock(stock)
                    .tradingDate(bar.tradingDate())
                    .open(bar.open())
                    .high(bar.high())
                    .low(bar.low())
                    .close(bar.close())
                    .volume(bar.volume())
                    .build());
            saved++;
        }
        log.info("Imported {} new daily price record(s) for {}", saved, stock.getSymbol());
    }
}
