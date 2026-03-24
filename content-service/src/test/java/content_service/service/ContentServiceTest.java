package content_service.service;

import content_service.controller.dto.*;
import content_service.domain.Category;
import content_service.domain.Content;
import content_service.domain.UserLike;
import content_service.event.EventPayload;
import content_service.event.LikeEventType;
import content_service.exception.ContentNotFoundException;
import content_service.outbox.OutboxEventPublisher;
import content_service.repository.ContentRepository;
import content_service.repository.UserLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private UserLikeRepository userLikeRepository;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Test
    @DisplayName("존재하는 contentId를 전달하면 ContentDetailResponse를 반환한다")
    void getContent_success() {
        // given
        Long contentId = 1L;
        Content content = Content.create("제목", Category.TECH, "본문");
        ReflectionTestUtils.setField(content, "id", contentId);
        ReflectionTestUtils.setField(content, "likeCount", 5);
        ReflectionTestUtils.setField(content, "createdAt", LocalDateTime.of(2026, 3, 24, 0, 0));
        ReflectionTestUtils.setField(content, "updatedAt", LocalDateTime.of(2026, 3, 24, 0, 0));

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when
        ContentDetailResponse response = contentService.getContent(contentId);

        // then
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.body()).isEqualTo("본문");
        assertThat(response.likeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("존재하지 않는 contentId로 조회하면 ContentNotFoundException이 발생한다")
    void getContent_notFound_throwsException() {
        // given
        Long contentId = 999L;
        given(contentRepository.findById(contentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contentService.getContent(contentId))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessage("콘텐츠를 찾을 수 없습니다. id=" + contentId);
    }

    @Test
    @DisplayName("페이지 요청을 전달하면 ContentSummaryResponse 목록을 반환한다")
    void getContents_success() {
        // given
        Content content = Content.create("제목", Category.TECH, "본문");
        ReflectionTestUtils.setField(content, "id", 1L);
        ReflectionTestUtils.setField(content, "likeCount", 3);
        ReflectionTestUtils.setField(content, "createdAt", LocalDateTime.of(2026, 3, 24, 0, 0));

        PageRequest pageable = PageRequest.of(0, 20);
        Page<Content> page = new PageImpl<>(List.of(content), pageable, 1);
        given(contentRepository.findAll(pageable)).willReturn(page);

        // when
        Page<ContentSummaryResponse> result = contentService.getContents(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).contentId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).title()).isEqualTo("제목");
        assertThat(result.getContent().get(0).likeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("title, category, body를 전달하면 Content를 저장하고 저장된 정보를 반환한다")
    void createContent_success() {
        // given
        ContentCreateRequest request = new ContentCreateRequest("제목", Category.TECH, "본문");

        Content savedContent = Content.create("제목", Category.TECH, "본문");
        ReflectionTestUtils.setField(savedContent, "id", 1L);
        ReflectionTestUtils.setField(savedContent, "createdAt", LocalDateTime.of(2026, 3, 18, 0, 0));

        given(contentRepository.save(any(Content.class))).willReturn(savedContent);

        // when
        ContentCreateResponse response = contentService.createContent(request);

        // then
        assertThat(response.contentId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.category()).isEqualTo(Category.TECH);
        assertThat(response.createdAt()).isNotNull();
        then(contentRepository).should(times(1)).save(any(Content.class));
    }

    @Test
    @DisplayName("존재하는 contentId와 수정할 title, category, body를 전달하면 Content를 수정하고 수정된 정보를 반환한다")
    void updateContent_success() {
        // given
        Long contentId = 1L;
        Content content = Content.create("기존 제목", Category.TECH, "기존 본문");
        ReflectionTestUtils.setField(content, "id", contentId);

        ContentUpdateRequest request = new ContentUpdateRequest("수정 제목", Category.TECH, "수정 본문");
        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when
        ContentUpdateResponse response = contentService.updateContent(contentId, request);

        // then
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.title()).isEqualTo("수정 제목");
        assertThat(response.category()).isEqualTo(Category.TECH);
    }

    @Test
    @DisplayName("존재하지 않는 contentId로 수정 요청하면 ContentNotFoundException이 발생한다")
    void updateContent_notFound_throwsException() {
        // given
        Long contentId = 999L;
        ContentUpdateRequest request = new ContentUpdateRequest("제목", Category.TECH, "본문");
        given(contentRepository.findById(contentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contentService.updateContent(contentId, request))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessage("콘텐츠를 찾을 수 없습니다. id=" + contentId);
    }

    @Test
    @DisplayName("존재하는 contentId를 전달하면 해당 Content를 삭제한다")
    void deleteContent_success() {
        // given
        Long contentId = 1L;
        Content content = Content.create("제목", Category.TECH, "본문");
        ReflectionTestUtils.setField(content, "id", contentId);
        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when
        contentService.deleteContent(contentId);

        // then
        then(contentRepository).should(times(1)).delete(content);
    }

    @Test
    @DisplayName("존재하지 않는 contentId로 삭제 요청하면 ContentNotFoundException이 발생한다")
    void deleteContent_notFound_throwsException() {
        // given
        Long contentId = 999L;
        given(contentRepository.findById(contentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contentService.deleteContent(contentId))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessage("콘텐츠를 찾을 수 없습니다. id=" + contentId);
    }

    @Test
    @DisplayName("좋아요 이력이 없으면 UserLike를 저장하고 liked: true를 반환한다")
    void toggleLike_likeAdded() {
        // given
        Long contentId = 1L;
        LikeRequest request = new LikeRequest("u123");

        given(contentRepository.existsById(contentId)).willReturn(true);
        given(userLikeRepository.findByContentIdAndUserId(contentId, "u123")).willReturn(Optional.empty());

        // when
        LikeResponse response = contentService.toggleLike(contentId, request);

        // then
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.liked()).isTrue();
        then(userLikeRepository).should(times(1)).save(any(UserLike.class));
        then(contentRepository).should(times(1)).incrementLikeCount(contentId);
        then(outboxEventPublisher).should(times(1)).publish(eq(LikeEventType.LIKE_ADDED), any(EventPayload.class));    }

    @Test
    @DisplayName("이미 좋아요한 이력이 있으면 UserLike를 삭제하고 liked: false를 반환한다")
    void toggleLike_likeRemoved() {
        // given
        Long contentId = 1L;
        LikeRequest request = new LikeRequest("u123");

        given(contentRepository.existsById(contentId)).willReturn(true);
        given(userLikeRepository.findByContentIdAndUserId(contentId, "u123")).willReturn(Optional.of(UserLike.create(contentId, "u123")));

        // when
        LikeResponse response = contentService.toggleLike(contentId, request);

        // then
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.liked()).isFalse();
        then(userLikeRepository).should(times(1)).deleteByContentIdAndUserId(contentId, "u123");
        then(contentRepository).should(times(1)).decrementLikeCount(contentId);
        then(outboxEventPublisher).should(times(1)).publish(eq(LikeEventType.LIKE_REMOVED), any(EventPayload.class));
    }

    @Test
    @DisplayName("존재하지 않는 contentId로 좋아요 요청 시 ContentNotFoundException이 발생한다")
    void toggleLike_contentNotFound_throwsException() {
        // given
        Long contentId = 999L;
        LikeRequest request = new LikeRequest("u123");

        given(contentRepository.existsById(contentId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> contentService.toggleLike(contentId, request))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessage("콘텐츠를 찾을 수 없습니다. id=" + contentId);
    }
}

