package content_service.controller.dto;

import content_service.domain.Content;

import java.time.LocalDateTime;

public record ContentSummaryResponse(
        Long contentId,
        String title,
        String category,
        Integer likeCount,
        LocalDateTime createdAt
) {
    public static ContentSummaryResponse from(Content content) {
        return new ContentSummaryResponse(
                content.getId(),
                content.getTitle(),
                content.getCategory().name(),
                content.getLikeCount(),
                content.getCreatedAt()
        );
    }
}
