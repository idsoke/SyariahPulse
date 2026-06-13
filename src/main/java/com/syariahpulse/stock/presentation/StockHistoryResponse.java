package com.syariahpulse.stock.presentation;

import java.time.LocalDate;
import java.util.List;

public record StockHistoryResponse(
        String symbol,
        List<HistoryEntry> history
) {
    public record HistoryEntry(
            LocalDate date,
            long price,
            int score,
            Integer rankPosition
    ) {}
}
