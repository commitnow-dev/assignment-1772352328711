package content_service.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

    // 메인 트랜잭션 커밋 직전 → 같은 트랜잭션 안에 Outbox 저장 (원자성 보장)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.createOutbox] Saving outbox: topic={}", outboxEvent.getOutbox().getTopic());
        outboxRepository.save(outboxEvent.getOutbox());
    }

    // 트랜잭션 커밋 성공 후 → 비동기로 Kafka 전송
    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.publishEvent] Publishing event to Kafka");
        publishToKafka(outboxEvent.getOutbox());
    }

    // 10초마다 미전송 이벤트 재시도 (Kafka 전송 실패 복구)
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void publishPendingMessages() {
        List<Outbox> pendingMessages = outboxRepository.findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
                LocalDateTime.now().minusSeconds(10),
                PageRequest.of(0, 100)
        );

        if (!pendingMessages.isEmpty()) {
            log.info("[MessageRelay.publishPendingMessages] Retrying {} pending messages", pendingMessages.size());
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
            log.info("[MessageRelay.publishToKafka] Published and deleted: topic={}, id={}", outbox.getTopic(), outbox.getId());
        } catch (Exception e) {
            log.error("[MessageRelay.publishToKafka] Failed to publish: outbox={}", outbox.getId(), e);
        }
    }
}
