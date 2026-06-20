package com.syariahpulse.stock.presentation;

import java.math.BigDecimal;

public record VolumeSpikeResponse(
        String symbol,
        String companyName,
        long volume,
        long avgVolume20,
        BigDecimal volumeRatio,
        long price
) {}
