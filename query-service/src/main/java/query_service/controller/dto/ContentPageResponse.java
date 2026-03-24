package query_service.controller.dto;

import java.util.List;

public record ContentPageResponse(
        List<ContentSummaryResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
}
