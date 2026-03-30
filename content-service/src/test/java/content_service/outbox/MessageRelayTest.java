package content_service.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageRelayTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private MessageRelay messageRelay;

    @BeforeEach
    void setUp() {
        messageRelay = new MessageRelay(outboxRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("createOutbox 호출 시 Outbox를 Repository에 저장한다")
    void createOutbox_savesOutboxToRepository() {
        // given
        Outbox outbox = Outbox.create("content-liked", "{\"eventType\":\"LIKE_ADDED\"}");
        OutboxEvent event = OutboxEvent.of(outbox);

        // when
        messageRelay.createOutbox(event);

        // then
        then(outboxRepository).should(times(1)).save(outbox);
    }

    @Test
    @DisplayName("Kafka 전송 성공 시 Outbox를 삭제한다")
    void publishEvent_kafkaSuccess_deletesOutbox() {
        // given
        Outbox outbox = Outbox.create("content-liked", "{\"eventType\":\"LIKE_ADDED\"}");
        OutboxEvent event = OutboxEvent.of(outbox);

        CompletableFuture<SendResult<String, String>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
        given(kafkaTemplate.send(eq("content-liked"), any())).willReturn(future);

        // when
        messageRelay.publishEvent(event);

        // then
        then(kafkaTemplate).should(times(1)).send("content-liked", "{\"eventType\":\"LIKE_ADDED\"}");
        then(outboxRepository).should(times(1)).delete(outbox);
    }

    @Test
    @DisplayName("Kafka 전송 실패 시 Outbox를 삭제하지 않는다 (재시도 대상)")
    void publishEvent_kafkaFails_outboxNotDeleted() {
        // given
        Outbox outbox = Outbox.create("content-liked", "{\"eventType\":\"LIKE_ADDED\"}");
        OutboxEvent event = OutboxEvent.of(outbox);

        CompletableFuture<SendResult<String, String>> failedFuture =
                CompletableFuture.failedFuture(new RuntimeException("Kafka unavailable"));
        given(kafkaTemplate.send(any(), any())).willReturn(failedFuture);

        // when
        messageRelay.publishEvent(event);

        // then
        then(outboxRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("미전송 메시지가 있으면 Kafka로 재전송하고 Outbox를 삭제한다")
    void publishPendingMessages_retriesAndDeletesPendingOutbox() {
        // given
        Outbox outbox = Outbox.create("content-liked", "{\"eventType\":\"LIKE_ADDED\"}");
        given(outboxRepository.findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
                any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of(outbox));

        CompletableFuture<SendResult<String, String>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
        given(kafkaTemplate.send(any(), any())).willReturn(future);

        // when
        messageRelay.publishPendingMessages();

        // then
        then(kafkaTemplate).should(times(1)).send("content-liked", "{\"eventType\":\"LIKE_ADDED\"}");
        then(outboxRepository).should(times(1)).delete(outbox);
    }

    @Test
    @DisplayName("미전송 메시지가 없으면 Kafka 전송을 시도하지 않는다")
    void publishPendingMessages_noPendingMessages_doesNotSendToKafka() {
        // given
        given(outboxRepository.findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(
                any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of());

        // when
        messageRelay.publishPendingMessages();

        // then
        then(kafkaTemplate).should(never()).send(any(), any());
    }
}

