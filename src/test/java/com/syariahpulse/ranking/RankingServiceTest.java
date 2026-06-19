package com.syariahpulse.ranking;

import com.syariahpulse.ranking.application.RankingService;
import com.syariahpulse.scoring.domain.StockScore;
import com.syariahpulse.scoring.infrastructure.StockScoreRepository;
import com.syariahpulse.stock.domain.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock StockScoreRepository stockScoreRepository;
    @InjectMocks RankingService rankingService;

    @Test
    void assigns_rank_positions_starting_from_1() {
        LocalDate date = LocalDate.now();
        List<StockScore> scores = List.of(
                scoreWithStock("DILD", 90),
                scoreWithStock("BSDE", 80),
                scoreWithStock("CPIN", 70)
        );

        when(stockScoreRepository.findByScoringDate(date, 0, 10)).thenReturn(scores);
        when(stockScoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        rankingService.rankForDate(date);

        assertThat(scores.get(0).getRankPosition()).isEqualTo(1);
        assertThat(scores.get(1).getRankPosition()).isEqualTo(2);
        assertThat(scores.get(2).getRankPosition()).isEqualTo(3);
        verify(stockScoreRepository, times(3)).save(any());
    }

    @Test
    void empty_list_does_nothing() {
        LocalDate date = LocalDate.now();
        when(stockScoreRepository.findByScoringDate(date, 0, 10)).thenReturn(List.of());
        rankingService.rankForDate(date);
        verify(stockScoreRepository, never()).save(any());
    }

    private StockScore scoreWithStock(String symbol, int score) {
        Stock stock = Stock.builder().symbol(symbol).companyName("Test").isSyariah(true).build();
        return StockScore.builder().stock(stock).score(score).scoringDate(LocalDate.now()).build();
    }
}
