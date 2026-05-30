package com.syariahpulse.indicator;

import com.syariahpulse.indicator.application.IndicatorCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class IndicatorCalculatorTest {

    private IndicatorCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IndicatorCalculator();
    }

    @Test
    void rsi_returns_null_when_insufficient_data() {
        List<BigDecimal> prices = List.of(BigDecimal.valueOf(100), BigDecimal.valueOf(110));
        assertThat(calculator.calculateRsi(prices, 14)).isNull();
    }

    @Test
    void rsi_returns_100_when_only_gains() {
        List<BigDecimal> prices = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> BigDecimal.valueOf(100 + i))
                .toList();
        BigDecimal rsi = calculator.calculateRsi(prices, 14);
        assertThat(rsi).isNotNull();
        assertThat(rsi.doubleValue()).isCloseTo(100.0, within(0.01));
    }

    @Test
    void rsi_returns_0_when_only_losses() {
        List<BigDecimal> prices = IntStream.rangeClosed(0, 19)
                .mapToObj(i -> BigDecimal.valueOf(200 - i))
                .toList();
        BigDecimal rsi = calculator.calculateRsi(prices, 14);
        assertThat(rsi).isNotNull();
        assertThat(rsi.doubleValue()).isCloseTo(0.0, within(0.01));
    }

    @Test
    void rsi_is_between_0_and_100_for_mixed_prices() {
        List<BigDecimal> prices = List.of(
                bd(44.34), bd(44.09), bd(44.15), bd(43.61), bd(44.33),
                bd(44.83), bd(45.10), bd(45.15), bd(43.61), bd(44.33),
                bd(44.83), bd(45.10), bd(45.15), bd(46.28), bd(46.00),
                bd(46.03), bd(46.41), bd(46.22), bd(45.64), bd(46.21)
        );
        BigDecimal rsi = calculator.calculateRsi(prices, 14);
        assertThat(rsi).isNotNull();
        assertThat(rsi.doubleValue()).isBetween(0.0, 100.0);
    }

    @Test
    void ema_returns_null_when_insufficient_data() {
        List<BigDecimal> prices = List.of(BigDecimal.valueOf(100), BigDecimal.valueOf(110));
        assertThat(calculator.calculateEma(prices, 20)).isNull();
    }

    @Test
    void ema_with_constant_prices_equals_that_price() {
        List<BigDecimal> prices = IntStream.range(0, 25)
                .mapToObj(i -> BigDecimal.valueOf(150))
                .toList();
        BigDecimal ema = calculator.calculateEma(prices, 20);
        assertThat(ema).isNotNull();
        assertThat(ema.doubleValue()).isCloseTo(150.0, within(0.01));
    }

    @Test
    void avg_volume_returns_0_when_insufficient_data() {
        List<Long> volumes = List.of(1000L, 2000L);
        assertThat(calculator.calculateAvgVolume(volumes, 20)).isEqualTo(0L);
    }

    @Test
    void avg_volume_calculates_correctly() {
        List<Long> volumes = IntStream.range(0, 20)
                .mapToObj(i -> 1000L)
                .toList();
        assertThat(calculator.calculateAvgVolume(volumes, 20)).isEqualTo(1000L);
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }
}
