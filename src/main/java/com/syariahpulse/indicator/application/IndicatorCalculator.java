package com.syariahpulse.indicator.application;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Component
public class IndicatorCalculator {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    public BigDecimal calculateRsi(List<BigDecimal> closePrices, int period) {
        if (closePrices.size() < period + 1) {
            return null;
        }

        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;

        for (int i = 1; i <= period; i++) {
            BigDecimal change = closePrices.get(i).subtract(closePrices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                avgGain = avgGain.add(change);
            } else {
                avgLoss = avgLoss.add(change.abs());
            }
        }

        BigDecimal periodBD = BigDecimal.valueOf(period);
        avgGain = avgGain.divide(periodBD, MC);
        avgLoss = avgLoss.divide(periodBD, MC);

        for (int i = period + 1; i < closePrices.size(); i++) {
            BigDecimal change = closePrices.get(i).subtract(closePrices.get(i - 1));
            BigDecimal gain = change.compareTo(BigDecimal.ZERO) > 0 ? change : BigDecimal.ZERO;
            BigDecimal loss = change.compareTo(BigDecimal.ZERO) < 0 ? change.abs() : BigDecimal.ZERO;

            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1)).add(gain).divide(periodBD, MC);
            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1)).add(loss).divide(periodBD, MC);
        }

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }

        BigDecimal rs = avgGain.divide(avgLoss, MC);
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), MC));
        return rsi.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEma(List<BigDecimal> closePrices, int period) {
        if (closePrices.size() < period) {
            return null;
        }

        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));

        BigDecimal ema = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            ema = ema.add(closePrices.get(i));
        }
        ema = ema.divide(BigDecimal.valueOf(period), MC);

        for (int i = period; i < closePrices.size(); i++) {
            BigDecimal price = closePrices.get(i);
            ema = price.subtract(ema).multiply(multiplier, MC).add(ema);
        }

        return ema.setScale(4, RoundingMode.HALF_UP);
    }

    public long calculateAvgVolume(List<Long> volumes, int period) {
        if (volumes.size() < period) {
            return 0L;
        }
        long sum = 0L;
        for (int i = volumes.size() - period; i < volumes.size(); i++) {
            sum += volumes.get(i);
        }
        return sum / period;
    }
}
