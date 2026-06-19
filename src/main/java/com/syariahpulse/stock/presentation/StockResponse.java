package com.syariahpulse.stock.presentation;

public record StockResponse(
        String symbol,
        String companyName,
        String sector
) {}
