package content_service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import content_service.event.EventPayload;
import content_service.event.LikeEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public void publish(LikeEventType eventType, EventPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            Outbox outbox = Outbox.create(eventType.getTopic(), json);
            applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
            log.info("[OutboxEventPublisher.publish] Outbox event registered: topic={}", eventType.getTopic());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }
}
