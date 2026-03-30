package content_service.controller.dto;

import content_service.domain.Category;

public record ContentUpdateRequest(
        String title,
        Category category,
        String body
) {
}
