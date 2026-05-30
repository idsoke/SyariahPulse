package com.syariahpulse.indicator.infrastructure;

import com.syariahpulse.indicator.domain.TechnicalIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TechnicalIndicatorRepository extends JpaRepository<TechnicalIndicator, Long> {
    Optional<TechnicalIndicator> findByStockIdAndTradingDate(Long stockId, LocalDate tradingDate);
}
