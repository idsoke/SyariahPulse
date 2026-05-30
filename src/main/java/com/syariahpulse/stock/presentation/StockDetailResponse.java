package com.syariahpulse.stock.presentation;

import java.util.List;

public record StockDetailResponse(
        String symbol,
        long price,
        int score,
        List<String> reasons
) {}
