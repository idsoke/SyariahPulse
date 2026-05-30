package com.syariahpulse.stock.presentation;

import com.syariahpulse.indicator.domain.TechnicalIndicator;
import com.syariahpulse.indicator.infrastructure.TechnicalIndicatorRepository;
import com.syariahpulse.scoring.domain.StockScore;
import com.syariahpulse.scoring.infrastructure.StockScoreRepository;
import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StockController {

    private final StockScoreRepository stockScoreRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final TechnicalIndicatorRepository technicalIndicatorRepository;

    @GetMapping("/top-picks")
    public List<TopPickResponse> getTopPicks() {
        LocalDate today = LocalDate.now();
        return stockScoreRepository.findTop10ByScoringDate(today).stream()
                .map(ss -> new TopPickResponse(
                        ss.getStock().getSymbol(),
                        ss.getScore(),
                        latestPrice(ss)
                ))
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
