package com.syariahpulse.integration;

import com.syariahpulse.scoring.domain.StockScore;
import com.syariahpulse.scoring.infrastructure.StockScoreRepository;
import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.domain.Stock;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import com.syariahpulse.stock.infrastructure.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class StockApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("syariahpulse")
            .withUsername("syariahpulse")
            .withPassword("syariahpulse");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired StockRepository stockRepository;
    @Autowired DailyPriceRepository dailyPriceRepository;
    @Autowired StockScoreRepository stockScoreRepository;

    @Test
    void top_picks_returns_empty_when_no_data() throws Exception {
        mockMvc.perform(get("/api/top-picks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void top_picks_returns_scored_stock() throws Exception {
        LocalDate today = LocalDate.now();

        Stock stock = stockRepository.save(
                Stock.builder().symbol("DILD").companyName("Intiland Development").sector("Property").isSyariah(true).build()
        );

        dailyPriceRepository.save(
                DailyPrice.builder().stock(stock).tradingDate(today)
                        .open(BigDecimal.valueOf(200)).high(BigDecimal.valueOf(210))
                        .low(BigDecimal.valueOf(195)).close(BigDecimal.valueOf(200))
                        .volume(5_000_000L).build()
        );

        stockScoreRepository.save(
                StockScore.builder().stock(stock).scoringDate(today)
                        .score(85).priceScore(20).volumeScore(30)
                        .rsiScore(20).ema20Score(15).trendScore(0)
                        .rankPosition(1).build()
        );

        mockMvc.perform(get("/api/top-picks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("DILD"))
                .andExpect(jsonPath("$[0].score").value(85));
    }

    @Test
    void stock_detail_returns_404_for_unknown_symbol() throws Exception {
        mockMvc.perform(get("/api/stocks/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void stock_detail_returns_reasons() throws Exception {
        LocalDate today = LocalDate.now();

        Stock stock = stockRepository.save(
                Stock.builder().symbol("BSDE").companyName("Bumi Serpong Damai").sector("Property").isSyariah(true).build()
        );

        dailyPriceRepository.save(
                DailyPrice.builder().stock(stock).tradingDate(today)
                        .open(BigDecimal.valueOf(150)).high(BigDecimal.valueOf(160))
                        .low(BigDecimal.valueOf(145)).close(BigDecimal.valueOf(155))
                        .volume(3_000_000L).build()
        );

        stockScoreRepository.save(
                StockScore.builder().stock(stock).scoringDate(today)
                        .score(65).priceScore(20).volumeScore(30).rsiScore(0).ema20Score(15).trendScore(0)
                        .rankPosition(2).build()
        );

        mockMvc.perform(get("/api/stocks/BSDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BSDE"))
                .andExpect(jsonPath("$.score").value(65))
                .andExpect(jsonPath("$.reasons").isArray());
    }

    @Test
    void stock_history_returns_404_for_unknown_symbol() throws Exception {
        mockMvc.perform(get("/api/stocks/UNKNOWN/history"))
                .andExpect(status().isNotFound());
    }

    @Test
    void stock_history_returns_score_and_price_series() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Stock stock = stockRepository.save(
                Stock.builder().symbol("ADRO").companyName("Adaro Energy").sector("Energy").isSyariah(true).build()
        );

        dailyPriceRepository.save(
                DailyPrice.builder().stock(stock).tradingDate(yesterday)
                        .open(BigDecimal.valueOf(100)).high(BigDecimal.valueOf(105))
                        .low(BigDecimal.valueOf(98)).close(BigDecimal.valueOf(102))
                        .volume(1_000_000L).build()
        );
        dailyPriceRepository.save(
                DailyPrice.builder().stock(stock).tradingDate(today)
                        .open(BigDecimal.valueOf(102)).high(BigDecimal.valueOf(110))
                        .low(BigDecimal.valueOf(100)).close(BigDecimal.valueOf(108))
                        .volume(1_500_000L).build()
        );

        stockScoreRepository.save(
                StockScore.builder().stock(stock).scoringDate(yesterday)
                        .score(50).priceScore(20).volumeScore(0).rsiScore(20).ema20Score(10).trendScore(0).build()
        );
        stockScoreRepository.save(
                StockScore.builder().stock(stock).scoringDate(today)
                        .score(75).priceScore(20).volumeScore(30).rsiScore(10).ema20Score(15).trendScore(0)
                        .rankPosition(3).build()
        );

        mockMvc.perform(get("/api/stocks/ADRO/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("ADRO"))
                .andExpect(jsonPath("$.history.length()").value(2))
                .andExpect(jsonPath("$.history[0].date").value(today.toString()))
                .andExpect(jsonPath("$.history[0].score").value(75))
                .andExpect(jsonPath("$.history[0].price").value(108))
                .andExpect(jsonPath("$.history[0].rankPosition").value(3))
                .andExpect(jsonPath("$.history[1].date").value(yesterday.toString()))
                .andExpect(jsonPath("$.history[1].score").value(50))
                .andExpect(jsonPath("$.history[1].price").value(102));
    }
}
