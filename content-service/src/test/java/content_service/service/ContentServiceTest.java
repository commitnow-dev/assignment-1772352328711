package content_service.service;

import content_service.controller.dto.ContentCreateRequest;
import content_service.controller.dto.ContentCreateResponse;
import content_service.controller.dto.ContentUpdateRequest;
import content_service.controller.dto.ContentUpdateResponse;
import content_service.domain.Category;
import content_service.domain.Content;
import content_service.exception.ContentNotFoundException;
import content_service.repository.ContentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;

    @Mock
    private ContentRepository contentRepository;

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
}
