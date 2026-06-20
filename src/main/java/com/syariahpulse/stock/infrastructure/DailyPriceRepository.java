package com.syariahpulse.stock.infrastructure;

import com.syariahpulse.stock.domain.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {

    List<DailyPrice> findByStockIdOrderByTradingDateDesc(Long stockId);

    @Query("SELECT dp FROM DailyPrice dp WHERE dp.stock.id = :stockId ORDER BY dp.tradingDate DESC LIMIT :limit")
    List<DailyPrice> findRecentByStockId(@Param("stockId") Long stockId, @Param("limit") int limit);

    Optional<DailyPrice> findByStockIdAndTradingDate(Long stockId, LocalDate tradingDate);

    long countByStockId(Long stockId);

    @Query("SELECT dp FROM DailyPrice dp WHERE dp.tradingDate = :date")
    List<DailyPrice> findAllByTradingDate(@Param("date") LocalDate date);

    @Query("SELECT MAX(dp.tradingDate) FROM DailyPrice dp")
    Optional<LocalDate> findLatestTradingDate();
}
