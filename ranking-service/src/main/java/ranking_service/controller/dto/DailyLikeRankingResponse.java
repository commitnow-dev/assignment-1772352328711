package ranking_service.controller.dto;

import ranking_service.domain.DailyRanking;

import java.time.LocalDateTime;
import java.util.List;

public record DailyLikeRankingResponse(
        String date,
        List<DailyLikeRankingItem> items,
        LocalDateTime generatedAt
) {
    public static DailyLikeRankingResponse from(DailyRanking ranking) {
        List<DailyLikeRankingItem> items = ranking.getItems().stream()
                .map(item -> new DailyLikeRankingItem(item.getRank(), item.getContentId(), item.getCount()))
                .toList();
        return new DailyLikeRankingResponse(ranking.getDate(), items, ranking.getGeneratedAt());
    }
}