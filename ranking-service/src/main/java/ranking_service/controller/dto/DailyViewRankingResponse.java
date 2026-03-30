package ranking_service.controller.dto;

import ranking_service.domain.DailyRanking;

import java.time.LocalDateTime;
import java.util.List;

public record DailyViewRankingResponse(
        String date,
        List<DailyViewRankingItem> items,
        LocalDateTime generatedAt
) {
    public static DailyViewRankingResponse from(DailyRanking ranking) {
        List<DailyViewRankingItem> items = ranking.getItems().stream()
                .map(item -> new DailyViewRankingItem(item.getRank(), item.getContentId(), item.getCount()))
                .toList();
        return new DailyViewRankingResponse(ranking.getDate(), items, ranking.getGeneratedAt());
    }
}