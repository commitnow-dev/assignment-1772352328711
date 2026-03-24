package query_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import query_service.controller.dto.ContentPageResponse;
import query_service.controller.dto.ContentResponse;
import query_service.service.ContentQueryService;

@RestController
@RequiredArgsConstructor
public class ContentQueryController {

    private final ContentQueryService contentQueryService;

    @GetMapping("/api/contents/{contentId}")
    public ResponseEntity<ContentResponse> getContent(
            @PathVariable Long contentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(contentQueryService.getContent(contentId, userId));
    }

    @GetMapping("/api/contents")
    public ResponseEntity<ContentPageResponse> getContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contentQueryService.getContents(page, size));
    }
}
