package query_service.controller.dto;

import java.time.LocalDateTime;

public record ContentSummaryResponse(
        Long contentId,
        String title,
        String category,
        Integer likeCount,
        LocalDateTime createdAt
) {
}