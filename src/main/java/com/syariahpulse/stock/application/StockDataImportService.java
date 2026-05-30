package com.syariahpulse.stock.application;

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

/**
 * Stub import service — replace with actual IDX/data-provider integration.
 * Inserts sample ISSI stocks so the nightly batch has data to process.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataImportService {

    private final StockRepository stockRepository;
    private final DailyPriceRepository dailyPriceRepository;

    @Transactional
    public void importDailyPrices(LocalDate date) {
        log.info("Importing daily prices for {}", date);
        ensureSampleStocksExist();
        // Real implementation: call IDX API or market data provider here
    }

    private void ensureSampleStocksExist() {
        if (stockRepository.count() > 0) return;

        List<Stock> samples = List.of(
                Stock.builder().symbol("DILD").companyName("Intiland Development Tbk").sector("Property").isSyariah(true).build(),
                Stock.builder().symbol("BSDE").companyName("Bumi Serpong Damai Tbk").sector("Property").isSyariah(true).build(),
                Stock.builder().symbol("CPIN").companyName("Charoen Pokphand Indonesia Tbk").sector("Food").isSyariah(true).build(),
                Stock.builder().symbol("TLKM").companyName("Telekomunikasi Indonesia Tbk").sector("Telecom").isSyariah(true).build(),
                Stock.builder().symbol("ANTM").companyName("Aneka Tambang Tbk").sector("Mining").isSyariah(true).build()
        );
        stockRepository.saveAll(samples);
        log.info("Inserted {} sample syariah stocks", samples.size());
    }
}
