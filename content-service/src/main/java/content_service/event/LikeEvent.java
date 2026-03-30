package content_service.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record LikeEvent(
        String eventId,
        LikeEventType eventType,
        Long contentId,
        String userId,
        LocalDateTime occurredAt
) implements EventPayload {

    public static LikeEvent of(Long contentId, String userId, LikeEventType eventType) {
        return new LikeEvent(UUID.randomUUID().toString(), eventType, contentId, userId, LocalDateTime.now());
    }
}
