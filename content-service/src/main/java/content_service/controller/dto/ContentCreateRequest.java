package content_service.controller.dto;

import content_service.domain.Category;

public record ContentCreateRequest(
        String title,
        Category category,
        String body
) {
}
