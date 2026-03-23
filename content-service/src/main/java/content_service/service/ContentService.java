package content_service.service;

import content_service.controller.dto.*;
import content_service.domain.Content;
import content_service.domain.UserLike;
import content_service.event.LikeEvent;
import content_service.event.LikeEventType;
import content_service.exception.ContentNotFoundException;
import content_service.outbox.OutboxEventPublisher;
import content_service.repository.ContentRepository;
import content_service.repository.UserLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserLikeRepository userLikeRepository;
    private final OutboxEventPublisher outboxEventPublisher;

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

    @Transactional
    public LikeResponse toggleLike(Long contentId, LikeRequest request) {
        if (!contentRepository.existsById(contentId)) {
            throw new ContentNotFoundException(contentId);
        }

        boolean alreadyLiked = userLikeRepository.findByContentIdAndUserId(contentId, request.userId()).isPresent();

        if (alreadyLiked) {
            userLikeRepository.deleteByContentIdAndUserId(contentId, request.userId());
            contentRepository.decrementLikeCount(contentId);
            outboxEventPublisher.publish(LikeEventType.LIKE_REMOVED, LikeEvent.of(contentId, request.userId(), LikeEventType.LIKE_REMOVED));

            return new LikeResponse(contentId, false);
        }

        userLikeRepository.save(UserLike.create(contentId, request.userId()));
        contentRepository.incrementLikeCount(contentId);
        outboxEventPublisher.publish(LikeEventType.LIKE_ADDED, LikeEvent.of(contentId, request.userId(), LikeEventType.LIKE_ADDED));

        return new LikeResponse(contentId, true);
    }
}
