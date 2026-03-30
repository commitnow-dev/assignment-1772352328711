package query_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import query_service.client.dto.RankingResponse;

@Slf4j
@Component
public class RankingClient {

    private final RestClient restClient;

    public RankingClient(@Value("${ranking-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public RankingResponse getDailyViewRanking(String date) {
        log.info("[RankingClient] Fetching view ranking: date={}", date);
        return restClient.get()
                .uri("/api/rankings/views/daily?date={date}", date)
                .retrieve()
                .body(RankingResponse.class);
    }

    public RankingResponse getDailyLikeRanking(String date) {
        log.info("[RankingClient] Fetching like ranking: date={}", date);
        return restClient.get()
                .uri("/api/rankings/likes/daily?date={date}", date)
                .retrieve()
                .body(RankingResponse.class);
    }
}
