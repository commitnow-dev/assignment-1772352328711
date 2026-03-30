package query_service.warmup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import query_service.client.ContentClient;
import query_service.client.RankingClient;
import query_service.client.dto.RankingItemResponse;
import query_service.client.dto.RankingResponse;
import query_service.config.CacheWarmupService;
import query_service.controller.dto.ContentResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CacheWarmupServiceTest {

    @Mock
    private RankingClient rankingClient;

    @Mock
    private ContentClient contentClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CacheWarmupService cacheWarmupService;

    @Test
    @DisplayName("정상 동작 - VIEW/LIKE 랭킹의 모든 컨텐츠를 Redis에 적재")
    void warmUp_정상_Redis에적재() {
        // given
        RankingResponse viewRanking = new RankingResponse("20260323", List.of(
                new RankingItemResponse(1, 1L),
                new RankingItemResponse(2, 2L)
        ));
        RankingResponse likeRanking = new RankingResponse("20260323", List.of(
                new RankingItemResponse(1, 3L),
                new RankingItemResponse(2, 4L)
        ));
        given(rankingClient.getDailyViewRanking(anyString())).willReturn(viewRanking);
        given(rankingClient.getDailyLikeRanking(anyString())).willReturn(likeRanking);
        given(contentClient.getContent(any())).willReturn(dummyContent(1L));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        cacheWarmupService.warmUp();

        // then - contentId 1,2,3,4 총 4번 캐시
        then(valueOperations).should(times(4)).set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("VIEW와 LIKE 랭킹에 같은 contentId가 있으면 중복 제거 후 1번만 캐싱")
    void warmUp_중복contentId_한번만캐싱() {
        // given - contentId 1이 VIEW, LIKE 랭킹에 모두 존재
        RankingResponse viewRanking = new RankingResponse("20260323", List.of(
                new RankingItemResponse(1, 1L),
                new RankingItemResponse(2, 2L)
        ));
        RankingResponse likeRanking = new RankingResponse("20260323", List.of(
                new RankingItemResponse(1, 1L),
                new RankingItemResponse(2, 3L)
        ));
        given(rankingClient.getDailyViewRanking(anyString())).willReturn(viewRanking);
        given(rankingClient.getDailyLikeRanking(anyString())).willReturn(likeRanking);
        given(contentClient.getContent(any())).willReturn(dummyContent(1L));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        cacheWarmupService.warmUp();

        // then - 1,2,3 총 3번 (중복 1 제거)
        then(contentClient).should(times(3)).getContent(any());
        then(valueOperations).should(times(3)).set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("ranking-service 장애 시 예외를 삼키고 서비스 시작을 막지 않음")
    void warmUp_rankingService장애_예외삼킴() {
        // given
        given(rankingClient.getDailyViewRanking(anyString()))
                .willThrow(new RuntimeException("ranking-service 연결 실패"));

        // when - 예외가 전파되지 않아야 함
        cacheWarmupService.warmUp();

        // then
        then(contentClient).should(never()).getContent(any());
        then(valueOperations).should(never()).set(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("일부 컨텐츠 조회 실패 시 해당 건만 건너뛰고 나머지는 캐싱")
    void warmUp_일부컨텐츠실패_나머지는캐싱() {
        // given
        RankingResponse viewRanking = new RankingResponse("20260323", List.of(
                new RankingItemResponse(1, 1L),
                new RankingItemResponse(2, 2L),
                new RankingItemResponse(3, 3L)
        ));
        RankingResponse likeRanking = new RankingResponse("20260323", List.of());

        given(rankingClient.getDailyViewRanking(anyString())).willReturn(viewRanking);
        given(rankingClient.getDailyLikeRanking(anyString())).willReturn(likeRanking);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // contentId 2 조회 실패
        given(contentClient.getContent(1L)).willReturn(dummyContent(1L));
        given(contentClient.getContent(2L)).willThrow(new RuntimeException("content-service 오류"));
        given(contentClient.getContent(3L)).willReturn(dummyContent(3L));

        // when
        cacheWarmupService.warmUp();

        // then - 1, 3만 캐싱 (2는 실패로 건너뜀)
        then(valueOperations).should(times(2)).set(anyString(), anyString(), any());
        then(valueOperations).should().set(eq("content:1"), anyString(), any());
        then(valueOperations).should().set(eq("content:3"), anyString(), any());
    }

    private ContentResponse dummyContent(Long contentId) {
        return new ContentResponse(contentId, "제목", "TECH", "본문", 0,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
