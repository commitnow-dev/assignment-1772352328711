package content_service.controller.dto;

public record LikeResponse(
        Long contentId,
        boolean liked
) {
}
