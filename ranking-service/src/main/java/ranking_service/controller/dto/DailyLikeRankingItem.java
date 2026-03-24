package ranking_service.controller.dto;

public record DailyLikeRankingItem(
        int rank,
        Long contentId,
        long likeCount
) {
}
