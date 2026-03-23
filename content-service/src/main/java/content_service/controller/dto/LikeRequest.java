package content_service.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record LikeRequest(
        @NotBlank String userId
) {
}
