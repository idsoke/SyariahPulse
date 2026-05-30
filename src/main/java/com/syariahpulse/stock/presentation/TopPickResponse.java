package com.syariahpulse.stock.presentation;

public record TopPickResponse(
        String symbol,
        int score,
        long price
) {}
