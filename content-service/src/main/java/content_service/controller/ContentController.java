package content_service.controller;

import content_service.controller.dto.ContentCreateRequest;
import content_service.controller.dto.ContentCreateResponse;
import content_service.controller.dto.ContentUpdateRequest;
import content_service.controller.dto.ContentUpdateResponse;
import content_service.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

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
}
