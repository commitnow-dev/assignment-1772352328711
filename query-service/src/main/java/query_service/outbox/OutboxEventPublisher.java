package query_service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import query_service.event.ViewEventType;
import query_service.util.DataSerializer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxRepository outboxRepository;

    public void publish(ViewEventType eventType, Object payload) {
        String json = DataSerializer.serialize(payload);
        Outbox outbox = Outbox.create(eventType.getTopic(), json);
        outboxRepository.save(outbox);
        log.info("[OutboxEventPublisher.publish] Outbox saved: topic={}", eventType.getTopic());
    }
}
