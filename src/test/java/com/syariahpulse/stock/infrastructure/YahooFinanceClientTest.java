package com.syariahpulse.stock.infrastructure;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YahooFinanceClientTest {

    private final YahooFinanceClient client = new YahooFinanceClient();

    @Test
    void parses_bars_and_skips_null_entries() throws Exception {
        // timestamps: 2024-01-02 (valid), 2024-01-03 (null bar, e.g. holiday gap)
        String json = """
                {
                  "chart": {
                    "result": [
                      {
                        "timestamp": [1704162000, 1704248400],
                        "indicators": {
                          "quote": [
                            {
                              "open": [200.0, null],
                              "high": [210.0, null],
                              "low": [195.0, null],
                              "close": [205.0, null],
                              "volume": [1000000, null]
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
                """;

        List<YahooFinanceClient.DailyOhlcv> bars = client.parseChartResponse(json);

        assertThat(bars).hasSize(1);
        YahooFinanceClient.DailyOhlcv bar = bars.get(0);
        assertThat(bar.tradingDate()).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(bar.open()).isEqualByComparingTo(BigDecimal.valueOf(200.0));
        assertThat(bar.close()).isEqualByComparingTo(BigDecimal.valueOf(205.0));
        assertThat(bar.volume()).isEqualTo(1_000_000L);
    }

    @Test
    void returns_empty_list_when_result_missing() throws Exception {
        String json = """
                { "chart": { "result": null, "error": { "code": "Not Found" } } }
                """;

        List<YahooFinanceClient.DailyOhlcv> bars = client.parseChartResponse(json);

        assertThat(bars).isEmpty();
    }
}
