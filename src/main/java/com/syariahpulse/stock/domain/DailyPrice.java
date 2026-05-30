package com.syariahpulse.stock.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal open;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal high;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal low;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal close;

    @Column(nullable = false)
    private Long volume;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
