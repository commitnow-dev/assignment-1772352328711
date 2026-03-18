package content_service.service;

import content_service.controller.dto.ContentCreateRequest;
import content_service.controller.dto.ContentCreateResponse;
import content_service.controller.dto.ContentUpdateRequest;
import content_service.controller.dto.ContentUpdateResponse;
import content_service.domain.Content;
import content_service.exception.ContentNotFoundException;
import content_service.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    @Transactional
    public ContentCreateResponse createContent(ContentCreateRequest request) {
        Content content = Content.create(request.title(), request.category(), request.body());
        Content saved = contentRepository.save(content);

        return new ContentCreateResponse(saved.getId(), saved.getTitle(), saved.getCategory(), saved.getCreatedAt());
    }

    @Transactional
    public ContentUpdateResponse updateContent(Long contentId, ContentUpdateRequest request) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));
        content.update(request.title(), request.category(), request.body());

        return new ContentUpdateResponse(content.getId(), content.getTitle(), content.getCategory(), content.getUpdatedAt());
    }

    @Transactional
    public void deleteContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));
        contentRepository.delete(content);
    }
}
