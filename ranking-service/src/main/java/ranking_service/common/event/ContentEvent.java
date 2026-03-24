package ranking_service.common.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "content_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentEvent {

    @Id
    private String id;

    @Indexed(unique = true)
    private String eventId;       // 중복 소비 방지

    private EventType eventType;
    private Long contentId;
    private String userId;
    private LocalDateTime occurredAt;
    private String date;          // "20260301" - 집계 쿼리용

    public static ContentEvent create(String eventId, EventType eventType, Long contentId,
                                                                   String userId, LocalDateTime occurredAt, String date) {
        ContentEvent event = new ContentEvent();
        event.eventId = eventId;
        event.eventType = eventType;
        event.contentId = contentId;
        event.userId = userId;
        event.occurredAt = occurredAt;
        event.date = date;
        return event;
    }
}