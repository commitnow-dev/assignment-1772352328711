package query_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import query_service.client.ContentClient;
import query_service.client.RankingClient;
import query_service.client.dto.RankingResponse;
import query_service.controller.dto.ContentResponse;
import query_service.util.CacheConstants;
import query_service.util.DataSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmupService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RankingClient rankingClient;
    private final ContentClient contentClient;
    private final RedisTemplate<String, String> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        log.info("[CacheWarmupService] Starting cache warm-up for date={}", yesterday);

        try {
            RankingResponse viewRanking = rankingClient.getDailyViewRanking(yesterday);
            RankingResponse likeRanking = rankingClient.getDailyLikeRanking(yesterday);

            // VIEW TOP 10 + LIKE TOP 10 중복 제거
            List<Long> contentIds = Stream.concat(
                    viewRanking.items().stream().map(item -> item.contentId()),
                    likeRanking.items().stream().map(item -> item.contentId())
            ).distinct().toList();

            log.info("[CacheWarmupService] Warming up {} contents", contentIds.size());

            for (Long contentId : contentIds) {
                warmUpContent(contentId);
            }

            log.info("[CacheWarmupService] Cache warm-up completed");
        } catch (Exception e) {
            log.warn("[CacheWarmupService] Cache warm-up failed, continuing without warm-up", e);
        }
    }

    private void warmUpContent(Long contentId) {
        try {
            ContentResponse content = contentClient.getContent(contentId);
            String cacheKey = CacheConstants.CONTENT_CACHE_PREFIX + contentId;
            redisTemplate.opsForValue().set(cacheKey, DataSerializer.serialize(content), CacheConstants.CONTENT_CACHE_TTL);
            log.info("[CacheWarmupService] Cached contentId={}", contentId);
        } catch (Exception e) {
            log.warn("[CacheWarmupService] Failed to warm up contentId={}", contentId, e);
        }
    }
}
