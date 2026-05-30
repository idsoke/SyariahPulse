package com.syariahpulse.indicator.domain;

import com.syariahpulse.stock.domain.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "technical_indicator")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicalIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "rsi_14", precision = 10, scale = 4)
    private BigDecimal rsi14;

    @Column(name = "ema_20", precision = 15, scale = 4)
    private BigDecimal ema20;

    @Column(name = "ema_50", precision = 15, scale = 4)
    private BigDecimal ema50;

    @Column(name = "avg_volume_20")
    private Long avgVolume20;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
