package content_service.controller;

import content_service.controller.dto.*;
import content_service.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDetailResponse> getContent(@PathVariable Long contentId) {
        return ResponseEntity.ok(contentService.getContent(contentId));
    }

    @GetMapping
    public ResponseEntity<Page<ContentSummaryResponse>> getContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contentService.getContents(PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<ContentCreateResponse> createContent(@Valid @RequestBody ContentCreateRequest request) {
        ContentCreateResponse response = contentService.createContent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ContentUpdateResponse> updateContent(@PathVariable Long contentId,
                                                               @Valid @RequestBody ContentUpdateRequest request) {
        ContentUpdateResponse response = contentService.updateContent(contentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{contentId}/likes")
    public ResponseEntity<LikeResponse> toggleLike(@PathVariable Long contentId,
                                                   @Valid @RequestBody LikeRequest request) {
        LikeResponse response = contentService.toggleLike(contentId, request);
        return ResponseEntity.ok(response);
    }
}
