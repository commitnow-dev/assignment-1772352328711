package query_service.controller.dto;

import java.time.LocalDateTime;

public record ContentResponse(
        Long contentId,
        String title,
        String category,
        String body,
        Integer likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}