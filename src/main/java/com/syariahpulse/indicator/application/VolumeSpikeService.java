package com.syariahpulse.indicator.application;

import com.syariahpulse.indicator.domain.TechnicalIndicator;
import com.syariahpulse.indicator.infrastructure.TechnicalIndicatorRepository;
import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.domain.Stock;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VolumeSpikeService {

    private final DailyPriceRepository dailyPriceRepository;
    private final TechnicalIndicatorRepository technicalIndicatorRepository;

    public List<VolumeSpike> findVolumeSpikes(LocalDate date, BigDecimal minRatio, int limit) {
        Map<Long, TechnicalIndicator> indicatorsByStockId = technicalIndicatorRepository.findByTradingDate(date)
                .stream()
                .collect(Collectors.toMap(ti -> ti.getStock().getId(), Function.identity()));

        return dailyPriceRepository.findAllByTradingDate(date).stream()
                .filter(dp -> dp.getStock().isSyariah())
                .map(dp -> toVolumeSpike(dp, indicatorsByStockId.get(dp.getStock().getId())))
                .filter(spike -> spike != null && spike.volumeRatio().compareTo(minRatio) >= 0)
                .sorted(Comparator.comparing(VolumeSpike::volumeRatio).reversed())
                .limit(limit)
                .toList();
    }

    private VolumeSpike toVolumeSpike(DailyPrice price, TechnicalIndicator indicator) {
        if (indicator == null || indicator.getAvgVolume20() == null || indicator.getAvgVolume20() <= 0) {
            return null;
        }
        BigDecimal ratio = BigDecimal.valueOf(price.getVolume())
                .divide(BigDecimal.valueOf(indicator.getAvgVolume20()), 4, RoundingMode.HALF_UP);
        return new VolumeSpike(price.getStock(), price.getVolume(), indicator.getAvgVolume20(), ratio, price.getClose());
    }

    public record VolumeSpike(
            Stock stock,
            long volume,
            long avgVolume20,
            BigDecimal volumeRatio,
            BigDecimal price
    ) {}
}
