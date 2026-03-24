package ranking_service.common.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ranking_service.common.consumer.dto.ContentEventMessage;
import ranking_service.service.ContentEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventConsumer {

    private final ContentEventService contentEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"content-liked", "content-viewed"})
    public void consume(String message) {
        try {
            ContentEventMessage event = objectMapper.readValue(message, ContentEventMessage.class);
            log.info("[ContentEventConsumer] Received event: eventType={}, contentId={}", event.eventType(), event.contentId());
            contentEventService.save(event);
        } catch (Exception e) {
            log.error("[ContentEventConsumer] Failed to process message: {}", message, e);
        }
    }
}
