package ranking_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ranking_service.common.consumer.dto.ContentEventMessage;
import ranking_service.common.event.ContentEvent;
import ranking_service.common.event.EventType;
import ranking_service.repository.ContentEventRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ContentEventServiceTest {

    @Mock
    private ContentEventRepository contentEventRepository;

    @InjectMocks
    private ContentEventService contentEventService;

    @Test
    @DisplayName("새 이벤트 - 저장")
    void save_새이벤트_저장() {
        // given
        ContentEventMessage message = new ContentEventMessage(
                "event-uuid-1", EventType.VIEW, 1L, "user1", LocalDateTime.now()
        );
        given(contentEventRepository.existsByEventId("event-uuid-1")).willReturn(false);

        // when
        contentEventService.save(message);

        // then
        then(contentEventRepository).should().save(any(ContentEvent.class));
    }

    @Test
    @DisplayName("중복 이벤트 - 저장하지 않고 무시")
    void save_중복이벤트_무시() {
        // given
        ContentEventMessage message = new ContentEventMessage(
                "event-uuid-1", EventType.VIEW, 1L, "user1", LocalDateTime.now()
        );
        given(contentEventRepository.existsByEventId("event-uuid-1")).willReturn(true);

        // when
        contentEventService.save(message);

        // then
        then(contentEventRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이벤트 저장 시 date 필드가 occurredAt 기준으로 yyyyMMdd 형식으로 저장")
    void save_date_yyyyMMdd_형식() {
        // given
        LocalDateTime occurredAt = LocalDateTime.of(2026, 3, 24, 15, 30);
        ContentEventMessage message = new ContentEventMessage(
                "event-uuid-2", EventType.LIKE_ADDED, 2L, "user2", occurredAt
        );
        given(contentEventRepository.existsByEventId("event-uuid-2")).willReturn(false);

        ArgumentCaptor<ContentEvent> captor = ArgumentCaptor.forClass(ContentEvent.class);

        // when
        contentEventService.save(message);

        // then
        then(contentEventRepository).should().save(captor.capture());
        assertThat(captor.getValue().getDate()).isEqualTo("20260324");
    }
}

