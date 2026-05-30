package com.syariahpulse.scoring.infrastructure;

import com.syariahpulse.scoring.domain.StockScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockScoreRepository extends JpaRepository<StockScore, Long> {

    Optional<StockScore> findByStockIdAndScoringDate(Long stockId, LocalDate scoringDate);

    @Query("""
            SELECT ss FROM StockScore ss
            JOIN FETCH ss.stock s
            WHERE ss.scoringDate = :date
            ORDER BY ss.score DESC
            LIMIT 10
            """)
    List<StockScore> findTop10ByScoringDate(@Param("date") LocalDate date);

    @Query("""
            SELECT ss FROM StockScore ss
            JOIN FETCH ss.stock s
            WHERE s.symbol = :symbol
            ORDER BY ss.scoringDate DESC
            LIMIT 1
            """)
    Optional<StockScore> findLatestBySymbol(@Param("symbol") String symbol);
}
