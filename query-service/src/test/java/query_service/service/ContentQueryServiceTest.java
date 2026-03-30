package query_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import query_service.client.ContentClient;
import query_service.controller.dto.ContentPageResponse;
import query_service.controller.dto.ContentResponse;
import query_service.controller.dto.ContentSummaryResponse;
import query_service.event.ViewEventType;
import query_service.outbox.OutboxEventPublisher;
import query_service.util.DataSerializer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ContentQueryServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ContentClient contentClient;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @InjectMocks
    private ContentQueryService contentQueryService;

    @Test
    @DisplayName("캐시 히트 - content-service 호출 없이 캐시에서 반환")
    void getContent_캐시히트_contentService호출안함() {
        // given
        Long contentId = 1L;
        ContentResponse expected = new ContentResponse(
                contentId, "제목", "TECH", "본문", 10,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("content:1")).willReturn(DataSerializer.serialize(expected));

        // when
        ContentResponse result = contentQueryService.getContent(contentId, "user1");

        // then
        assertThat(result.contentId()).isEqualTo(contentId);
        assertThat(result.title()).isEqualTo("제목");
        then(contentClient).should(never()).getContent(any());
    }

    @Test
    @DisplayName("캐시 미스 - content-service 호출 후 Redis에 저장")
    void getContent_캐시미스_contentService호출하고캐시저장() {
        // given
        Long contentId = 1L;
        ContentResponse expected = new ContentResponse(
                contentId, "제목", "TECH", "본문", 10,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("content:1")).willReturn(null);
        given(contentClient.getContent(contentId)).willReturn(expected);

        // when
        ContentResponse result = contentQueryService.getContent(contentId, "user1");

        // then
        assertThat(result.contentId()).isEqualTo(contentId);
        then(contentClient).should().getContent(contentId);
        then(valueOperations).should().set(eq("content:1"), anyString(), any());
    }

    @Test
    @DisplayName("캐시 히트/미스 관계없이 VIEW 이벤트 항상 발행")
    void getContent_항상_VIEW_이벤트_발행() {
        // given
        Long contentId = 1L;
        ContentResponse response = new ContentResponse(
                contentId, "제목", "TECH", "본문", 10,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("content:1")).willReturn(DataSerializer.serialize(response));

        // when
        contentQueryService.getContent(contentId, "user1");

        // then
        then(outboxEventPublisher).should().publish(eq(ViewEventType.VIEW), any());
    }

    @Test
    @DisplayName("목록 조회 - content-service에 직접 위임")
    void getContents_contentService에위임() {
        // given
        ContentPageResponse expected = new ContentPageResponse(
                List.of(new ContentSummaryResponse(1L, "제목", "TECH", 10, LocalDateTime.now())),
                1L, 1, 0, 20
        );
        given(contentClient.getContents(0, 20)).willReturn(expected);

        // when
        ContentPageResponse result = contentQueryService.getContents(0, 20);

        // then
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        then(contentClient).should().getContents(0, 20);
    }
}