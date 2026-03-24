package query_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import query_service.client.ContentClient;
import query_service.controller.dto.ContentPageResponse;
import query_service.controller.dto.ContentResponse;
import query_service.event.ViewEvent;
import query_service.event.ViewEventType;
import query_service.outbox.OutboxEventPublisher;
import query_service.util.CacheConstants;
import query_service.util.DataSerializer;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentQueryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ContentClient contentClient;
    private final OutboxEventPublisher outboxEventPublisher;

    public ContentResponse getContent(Long contentId, String userId) {
        String cacheKey = CacheConstants.CONTENT_CACHE_PREFIX + contentId;
        String cached = redisTemplate.opsForValue().get(cacheKey);

        ContentResponse response;
        if (cached != null) {
            log.info("[ContentQueryService] Cache hit: contentId={}", contentId);
            response = DataSerializer.deserialize(cached, ContentResponse.class);
        } else {
            log.info("[ContentQueryService] Cache miss: contentId={}", contentId);
            response = contentClient.getContent(contentId);
            redisTemplate.opsForValue().set(cacheKey, DataSerializer.serialize(response), CacheConstants.CONTENT_CACHE_TTL);
        }

        outboxEventPublisher.publish(ViewEventType.VIEW, ViewEvent.of(contentId, userId));
        return response;
    }

    public ContentPageResponse getContents(int page, int size) {
        return contentClient.getContents(page, size);
    }
}
