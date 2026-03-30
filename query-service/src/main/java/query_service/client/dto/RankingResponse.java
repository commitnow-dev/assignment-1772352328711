package query_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RankingResponse(
        String date,
        List<RankingItemResponse> items
) {
}