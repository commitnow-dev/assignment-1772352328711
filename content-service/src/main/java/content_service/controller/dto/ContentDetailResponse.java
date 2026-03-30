package content_service.controller.dto;

import content_service.domain.Content;

import java.time.LocalDateTime;

public record ContentDetailResponse(
        Long contentId,
        String title,
        String category,
        String body,
        Integer likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ContentDetailResponse from(Content content) {
        return new ContentDetailResponse(
                content.getId(),
                content.getTitle(),
                content.getCategory().name(),
                content.getBody(),
                content.getLikeCount(),
                content.getCreatedAt(),
                content.getUpdatedAt()
        );
    }
}
