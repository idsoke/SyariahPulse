package com.syariahpulse.scoring.domain;

import com.syariahpulse.stock.domain.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "scoring_date", nullable = false)
    private LocalDate scoringDate;

    @Column(nullable = false)
    private int score;

    @Column(name = "price_score", nullable = false)
    private int priceScore;

    @Column(name = "volume_score", nullable = false)
    private int volumeScore;

    @Column(name = "rsi_score", nullable = false)
    private int rsiScore;

    @Column(name = "ema20_score", nullable = false)
    private int ema20Score;

    @Column(name = "trend_score", nullable = false)
    private int trendScore;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
