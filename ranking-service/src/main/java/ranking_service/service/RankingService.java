package ranking_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import ranking_service.common.event.ContentEvent;
import ranking_service.common.event.EventType;
import ranking_service.domain.DailyRanking;
import ranking_service.domain.RankingItem;
import ranking_service.domain.RankingType;
import ranking_service.repository.DailyRankingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final MongoTemplate mongoTemplate;
    private final DailyRankingRepository dailyRankingRepository;

    public void aggregateAndSave(String date) {
        log.info("[RankingService] Aggregating rankings for date={}", date);
        aggregateByType(date, EventType.VIEW, RankingType.VIEW);
        aggregateByType(date, EventType.LIKE_ADDED, RankingType.LIKE);
        log.info("[RankingService] Aggregation complete for date={}", date);
    }

    private void aggregateByType(String date, EventType eventType, RankingType rankingType) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("date").is(date).and("eventType").is(eventType)),
                Aggregation.group("contentId").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(10),
                Aggregation.project("count").and("_id").as("contentId")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, ContentEvent.class, Map.class);
        List<Map> mappedResults = results.getMappedResults();

        List<RankingItem> items = IntStream.range(0, mappedResults.size())
                .mapToObj(i -> {
                    Map row = mappedResults.get(i);
                    Long contentId = ((Number) row.get("contentId")).longValue();
                    long count = ((Number) row.get("count")).longValue();
                    return new RankingItem(i + 1, contentId, count);
                })
                .toList();

        // 재실행 가능하도록 기존 결과 삭제 후 저장
        dailyRankingRepository.deleteByDateAndRankingType(date, rankingType);
        dailyRankingRepository.save(DailyRanking.create(date, rankingType, items, LocalDateTime.now()));
    }

    public DailyRanking getDailyRanking(String date, RankingType rankingType) {
        return dailyRankingRepository.findByDateAndRankingType(date, rankingType)
                .orElse(DailyRanking.create(date, rankingType, List.of(), null));
    }
}
