package query_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RankingItemResponse(
        int rank,
        Long contentId
) {
}
