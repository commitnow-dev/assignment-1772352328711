package ranking_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import ranking_service.common.event.ContentEvent;
import ranking_service.domain.DailyRanking;
import ranking_service.domain.RankingItem;
import ranking_service.domain.RankingType;
import ranking_service.repository.DailyRankingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private DailyRankingRepository dailyRankingRepository;

    @InjectMocks
    private RankingService rankingService;

    @Test
    @DisplayName("랭킹 조회 - 데이터 존재하면 반환")
    void getDailyRanking_존재하면_반환() {
        // given
        DailyRanking ranking = DailyRanking.create(
                "20260324", RankingType.VIEW,
                List.of(new RankingItem(1, 42L, 1500)),
                LocalDateTime.now()
        );
        given(dailyRankingRepository.findByDateAndRankingType("20260324", RankingType.VIEW))
                .willReturn(Optional.of(ranking));

        // when
        DailyRanking result = rankingService.getDailyRanking("20260324", RankingType.VIEW);

        // then
        assertThat(result.getDate()).isEqualTo("20260324");
        assertThat(result.getRankingType()).isEqualTo(RankingType.VIEW);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getContentId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("랭킹 조회 - 데이터 없으면 빈 리스트 반환")
    void getDailyRanking_없으면_빈랭킹반환() {
        // given
        given(dailyRankingRepository.findByDateAndRankingType("20260324", RankingType.LIKE))
                .willReturn(Optional.empty());

        // when
        DailyRanking result = rankingService.getDailyRanking("20260324", RankingType.LIKE);

        // then
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getGeneratedAt()).isNull();
    }

    @Test
    @DisplayName("집계 - 기존 랭킹 삭제 후 새 랭킹 저장 (VIEW, LIKE 각각)")
    @SuppressWarnings("unchecked")
    void aggregateAndSave_기존삭제후_저장() {
        // given
        AggregationResults<Map> mockResults = mock(AggregationResults.class);
        given(mockResults.getMappedResults()).willReturn(List.of());
        given(mongoTemplate.aggregate(any(Aggregation.class), eq(ContentEvent.class), eq(Map.class)))
                .willReturn(mockResults);

        // when
        rankingService.aggregateAndSave("20260324");

        // then
        then(dailyRankingRepository).should(times(1))
                .deleteByDateAndRankingType("20260324", RankingType.VIEW);
        then(dailyRankingRepository).should(times(1))
                .deleteByDateAndRankingType("20260324", RankingType.LIKE);
        then(dailyRankingRepository).should(times(2)).save(any(DailyRanking.class));
    }

    @Test
    @DisplayName("집계 결과가 있으면 RankingItem 순위대로 저장")
    @SuppressWarnings("unchecked")
    void aggregateAndSave_집계결과_순위저장() {
        // given
        List<Map> aggregationResult = List.of(
                Map.of("contentId", 42L, "count", 1500L),
                Map.of("contentId", 7L, "count", 1200L)
        );
        AggregationResults<Map> mockResults = mock(AggregationResults.class);
        given(mockResults.getMappedResults()).willReturn(aggregationResult);
        given(mongoTemplate.aggregate(any(Aggregation.class), eq(ContentEvent.class), eq(Map.class)))
                .willReturn(mockResults);

        DailyRanking savedRanking = DailyRanking.create("20260324", RankingType.VIEW, List.of(), null);
        given(dailyRankingRepository.save(any())).willReturn(savedRanking);

        // when
        rankingService.aggregateAndSave("20260324");

        // then - VIEW와 LIKE 각각 2건씩 저장
        then(dailyRankingRepository).should(times(2)).save(any(DailyRanking.class));
    }
}
