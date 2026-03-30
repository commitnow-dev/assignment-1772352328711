package query_service.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ViewEvent(
        String eventId,
        String eventType,
        Long contentId,
        String userId,
        LocalDateTime occurredAt
) {
    public static ViewEvent of(Long contentId, String userId) {
        return new ViewEvent(UUID.randomUUID().toString(), "VIEW", contentId, userId, LocalDateTime.now());
    }
}