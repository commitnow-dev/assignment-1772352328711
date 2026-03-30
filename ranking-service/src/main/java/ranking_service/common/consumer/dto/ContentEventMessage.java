package ranking_service.common.consumer.dto;

import ranking_service.common.event.EventType;

import java.time.LocalDateTime;

public record ContentEventMessage(
        String eventId,
        EventType eventType,
        Long contentId,
        String userId,
        LocalDateTime occurredAt
) {
}
