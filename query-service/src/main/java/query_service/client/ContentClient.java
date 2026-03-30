package query_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import query_service.controller.dto.ContentPageResponse;
import query_service.controller.dto.ContentResponse;

@Slf4j
@Component
public class ContentClient {

    private final RestClient restClient;

    public ContentClient(@Value("${content-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ContentResponse getContent(Long contentId) {
        log.info("[ContentClient] Fetching content from content-service: contentId={}", contentId);
        return restClient.get()
                .uri("/api/contents/{contentId}", contentId)
                .retrieve()
                .body(ContentResponse.class);
    }

    public ContentPageResponse getContents(int page, int size) {
        return restClient.get()
                .uri("/api/contents?page={page}&size={size}", page, size)
                .retrieve()
                .body(ContentPageResponse.class);
    }
}
