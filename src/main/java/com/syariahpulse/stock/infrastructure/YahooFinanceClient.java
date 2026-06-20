package com.syariahpulse.stock.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches end-of-day OHLCV data from Yahoo Finance's public chart endpoint
 * (unofficial API, no key required). IDX symbols are suffixed with ".JK".
 */
@Slf4j
@Component
public class YahooFinanceClient {

    private static final String CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s.JK?range=%s&interval=1d";
    private static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<DailyOhlcv> fetchDailyHistory(String symbol, String range) {
        String url = String.format(CHART_URL, symbol, range);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Yahoo Finance returned status {} for {}", response.statusCode(), symbol);
                return List.of();
            }
            return parseChartResponse(response.body());
        } catch (IOException | InterruptedException e) {
            log.error("Failed to fetch Yahoo Finance data for {}: {}", symbol, e.getMessage());
            return List.of();
        }
    }

    List<DailyOhlcv> parseChartResponse(String body) throws IOException {
        JsonNode result = objectMapper.readTree(body).path("chart").path("result");
        if (!result.isArray() || result.isEmpty()) {
            return List.of();
        }

        JsonNode chart = result.get(0);
        JsonNode timestamps = chart.path("timestamp");
        JsonNode quote = chart.path("indicators").path("quote").get(0);
        if (!timestamps.isArray() || quote == null) {
            return List.of();
        }

        JsonNode opens = quote.path("open");
        JsonNode highs = quote.path("high");
        JsonNode lows = quote.path("low");
        JsonNode closes = quote.path("close");
        JsonNode volumes = quote.path("volume");

        List<DailyOhlcv> bars = new ArrayList<>();
        for (int i = 0; i < timestamps.size(); i++) {
            if (isNull(opens, i) || isNull(highs, i) || isNull(lows, i) || isNull(closes, i) || isNull(volumes, i)) {
                continue; // non-trading day or missing bar
            }
            LocalDate tradingDate = Instant.ofEpochSecond(timestamps.get(i).asLong()).atZone(JAKARTA).toLocalDate();
            bars.add(new DailyOhlcv(
                    tradingDate,
                    BigDecimal.valueOf(opens.get(i).asDouble()),
                    BigDecimal.valueOf(highs.get(i).asDouble()),
                    BigDecimal.valueOf(lows.get(i).asDouble()),
                    BigDecimal.valueOf(closes.get(i).asDouble()),
                    volumes.get(i).asLong()
            ));
        }
        return bars;
    }

    private boolean isNull(JsonNode array, int index) {
        return array.get(index) == null || array.get(index).isNull();
    }

    public record DailyOhlcv(
            LocalDate tradingDate,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            long volume
    ) {}
}
