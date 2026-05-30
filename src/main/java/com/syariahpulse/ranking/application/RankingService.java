package com.syariahpulse.ranking.application;

import com.syariahpulse.scoring.domain.StockScore;
import com.syariahpulse.scoring.infrastructure.StockScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final StockScoreRepository stockScoreRepository;

    @Transactional
    public void rankForDate(LocalDate date) {
        List<StockScore> top10 = stockScoreRepository.findTop10ByScoringDate(date);
        log.info("Ranking top {} stocks for {}", top10.size(), date);

        for (int i = 0; i < top10.size(); i++) {
            StockScore score = top10.get(i);
            score.setRankPosition(i + 1);
            stockScoreRepository.save(score);
        }
    }
}
