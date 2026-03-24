package query_service.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MessageRelay {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageRelay(
            OutboxRepository outboxRepository,
            @Qualifier("messageRelayKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void publishPendingMessages() {
        List<Outbox> pendingMessages = outboxRepository.findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
                LocalDateTime.now().minusSeconds(10)
        );

        if (!pendingMessages.isEmpty()) {
            log.info("[MessageRelay] Publishing {} pending messages", pendingMessages.size());
        }

        for (Outbox outbox : pendingMessages) {
            publishToKafka(outbox);
        }
    }

    private void publishToKafka(Outbox outbox) {
        try {
            kafkaTemplate.send(outbox.getTopic(), outbox.getPayload())
                    .get(1, TimeUnit.SECONDS);
            outboxRepository.delete(outbox);
            log.info("[MessageRelay] Published and deleted: topic={}, id={}", outbox.getTopic(), outbox.getId());
        } catch (Exception e) {
            log.error("[MessageRelay] Failed to publish: outbox={}", outbox.getId(), e);
        }
    }
}
