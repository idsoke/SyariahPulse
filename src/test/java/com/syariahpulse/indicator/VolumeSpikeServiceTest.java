package com.syariahpulse.indicator;

import com.syariahpulse.indicator.application.VolumeSpikeService;
import com.syariahpulse.indicator.domain.TechnicalIndicator;
import com.syariahpulse.indicator.infrastructure.TechnicalIndicatorRepository;
import com.syariahpulse.stock.domain.DailyPrice;
import com.syariahpulse.stock.domain.Stock;
import com.syariahpulse.stock.infrastructure.DailyPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VolumeSpikeServiceTest {

    @Mock DailyPriceRepository dailyPriceRepository;
    @Mock TechnicalIndicatorRepository technicalIndicatorRepository;

    @InjectMocks
    VolumeSpikeService volumeSpikeService;

    @Test
    void ranks_stocks_by_volume_ratio_descending_and_excludes_below_threshold() {
        LocalDate date = LocalDate.now();

        Stock dild = stock(1L, "DILD");
        Stock bsde = stock(2L, "BSDE");
        Stock tlkm = stock(3L, "TLKM");

        DailyPrice dildPrice = dailyPrice(dild, date, 9_000_000L); // ratio 4.5x
        DailyPrice bsdePrice = dailyPrice(bsde, date, 3_000_000L); // ratio 1.5x (below minRatio)
        DailyPrice tlkmPrice = dailyPrice(tlkm, date, 6_000_000L); // ratio 3x

        when(dailyPriceRepository.findAllByTradingDate(date))
                .thenReturn(List.of(dildPrice, bsdePrice, tlkmPrice));
        when(technicalIndicatorRepository.findByTradingDate(date)).thenReturn(List.of(
                indicator(dild, date, 2_000_000L),
                indicator(bsde, date, 2_000_000L),
                indicator(tlkm, date, 2_000_000L)
        ));

        List<VolumeSpikeService.VolumeSpike> result =
                volumeSpikeService.findVolumeSpikes(date, BigDecimal.valueOf(2), 10);

        assertThat(result).extracting(s -> s.stock().getSymbol())
                .containsExactly("DILD", "TLKM");
        assertThat(result.get(0).volumeRatio()).isEqualByComparingTo("4.5000");
    }

    @Test
    void excludes_stocks_without_indicator_or_zero_avg_volume() {
        LocalDate date = LocalDate.now();
        Stock stock = stock(1L, "ANTM");
        DailyPrice price = dailyPrice(stock, date, 1_000_000L);

        when(dailyPriceRepository.findAllByTradingDate(date)).thenReturn(List.of(price));
        when(technicalIndicatorRepository.findByTradingDate(date)).thenReturn(List.of());

        List<VolumeSpikeService.VolumeSpike> result =
                volumeSpikeService.findVolumeSpikes(date, BigDecimal.valueOf(2), 10);

        assertThat(result).isEmpty();
    }

    private Stock stock(Long id, String symbol) {
        return Stock.builder().id(id).symbol(symbol).companyName("Test " + symbol).isSyariah(true).build();
    }

    private DailyPrice dailyPrice(Stock stock, LocalDate date, long volume) {
        BigDecimal price = BigDecimal.valueOf(250);
        return DailyPrice.builder()
                .stock(stock).tradingDate(date)
                .open(price).high(price).low(price).close(price).volume(volume)
                .build();
    }

    private TechnicalIndicator indicator(Stock stock, LocalDate date, long avgVolume20) {
        return TechnicalIndicator.builder()
                .stock(stock).tradingDate(date).avgVolume20(avgVolume20)
                .build();
    }
}
