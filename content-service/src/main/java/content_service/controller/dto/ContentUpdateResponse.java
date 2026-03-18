package content_service.controller.dto;

import content_service.domain.Category;

import java.time.LocalDateTime;

public record ContentUpdateResponse(
        Long contentId,
        String title,
        Category category,
        LocalDateTime updatedAt
) {
}
