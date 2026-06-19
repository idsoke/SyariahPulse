package com.syariahpulse.stock.presentation;

import com.syariahpulse.indicator.domain.TechnicalIndicator;
import com.syariahpulse.indicator.infrastructure.TechnicalIndicatorRepository;
import com.syariahpulse.scoring.domain.StockScore;
import com.syariahpulse.scoring.infrastructure.StockScoreRepository;
import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.domain.Stock;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import com.syariahpulse.stock.infrastructure.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;
    private final StockScoreRepository stockScoreRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final TechnicalIndicatorRepository technicalIndicatorRepository;

    @GetMapping("/top-picks")
    public List<TopPickResponse> getTopPicks(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int minScore) {
        LocalDate today = LocalDate.now();
        return stockScoreRepository.findByScoringDate(today, minScore, limit).stream()
                .map(ss -> new TopPickResponse(
                        ss.getStock().getSymbol(),
                        ss.getScore(),
                        latestPrice(ss)
                ))
                .toList();
    }

    @GetMapping("/stocks")
    public List<StockResponse> getStocks() {
        return stockRepository.findByIsSyariahTrue().stream()
                .map(s -> new StockResponse(s.getSymbol(), s.getCompanyName(), s.getSector()))
                .toList();
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockDetailResponse> getStock(@PathVariable String symbol) {
        return stockScoreRepository.findLatestBySymbol(symbol)
                .map(ss -> {
                    List<String> reasons = buildReasons(ss);
                    return ResponseEntity.ok(new StockDetailResponse(
                            ss.getStock().getSymbol(),
                            latestPrice(ss),
                            ss.getScore(),
                            reasons
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stocks/{symbol}/history")
    public ResponseEntity<StockHistoryResponse> getStockHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "30") int days) {
        return stockRepository.findBySymbol(symbol)
                .map(stock -> {
                    Map<LocalDate, BigDecimal> pricesByDate = dailyPriceRepository
                            .findRecentByStockId(stock.getId(), days).stream()
                            .collect(Collectors.toMap(DailyPrice::getTradingDate, DailyPrice::getClose));

                    List<StockHistoryResponse.HistoryEntry> history = stockScoreRepository
                            .findHistoryBySymbol(symbol, days).stream()
                            .map(ss -> new StockHistoryResponse.HistoryEntry(
                                    ss.getScoringDate(),
                                    pricesByDate.getOrDefault(ss.getScoringDate(), BigDecimal.ZERO).longValue(),
                                    ss.getScore(),
                                    ss.getRankPosition()
                            ))
                            .toList();

                    return ResponseEntity.ok(new StockHistoryResponse(stock.getSymbol(), history));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private long latestPrice(StockScore ss) {
        return dailyPriceRepository
                .findByStockIdAndTradingDate(ss.getStock().getId(), ss.getScoringDate())
                .map(dp -> dp.getClose().longValue())
                .orElse(0L);
    }

    private List<String> buildReasons(StockScore ss) {
        List<String> reasons = new ArrayList<>();
        if (ss.getPriceScore() > 0) reasons.add("Price Below IDR 300");
        if (ss.getVolumeScore() > 0) reasons.add("Volume Spike");
        if (ss.getRsiScore() > 0) reasons.add("Healthy RSI");
        if (ss.getEma20Score() > 0) reasons.add("Above EMA20");
        if (ss.getTrendScore() > 0) reasons.add("Bullish Trend EMA20>EMA50");
        return reasons;
    }
}
