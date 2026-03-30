package ranking_service.controller.dto;

public record DailyViewRankingItem(
        int rank,
        Long contentId,
        long viewCount
) {
}