package content_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import content_service.controller.dto.*;
import content_service.domain.Category;
import content_service.exception.ContentNotFoundException;
import content_service.exception.GlobalExceptionHandler;
import content_service.service.ContentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ContentController.class, GlobalExceptionHandler.class})
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContentService contentService;

    @Test
    @DisplayName("GET /api/contents/{contentId} 요청 시 존재하는 contentId를 전달하면 200과 상세 정보를 반환한다")
    void getContent_success() throws Exception {
        // given
        Long contentId = 1L;
        ContentDetailResponse response = new ContentDetailResponse(
                contentId, "제목", "TECH", "본문", 10,
                LocalDateTime.of(2026, 3, 24, 0, 0),
                LocalDateTime.of(2026, 3, 24, 0, 0)
        );
        given(contentService.getContent(contentId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/contents/{contentId}", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(contentId))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.body").value("본문"))
                .andExpect(jsonPath("$.likeCount").value(10));
    }

    @Test
    @DisplayName("GET /api/contents/{contentId} 요청 시 존재하지 않는 contentId를 전달하면 404를 반환한다")
    void getContent_notFound_returns404() throws Exception {
        // given
        Long contentId = 999L;
        given(contentService.getContent(contentId)).willThrow(new ContentNotFoundException(contentId));

        // when & then
        mockMvc.perform(get("/api/contents/{contentId}", contentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("콘텐츠를 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("GET /api/contents 요청 시 200과 페이지 목록을 반환한다")
    void getContents_success() throws Exception {
        // given
        ContentSummaryResponse summary = new ContentSummaryResponse(
                1L, "제목", "TECH", 5, LocalDateTime.of(2026, 3, 24, 0, 0)
        );
        given(contentService.getContents(any(PageRequest.class)))
                .willReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        // when & then
        mockMvc.perform(get("/api/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].contentId").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("제목"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/contents 요청 시 title, category, body를 전달하면 201을 반환한다.")
    void createContent_success() throws Exception {
        // given
        ContentCreateRequest request = new ContentCreateRequest("제목", Category.TECH, "본문");
        ContentCreateResponse response = new ContentCreateResponse(1L, "제목", Category.TECH, LocalDateTime.of(2026, 3, 18, 0, 0));

        given(contentService.createContent(any(ContentCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentId").value(1L))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.category").value("TECH"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /api/contents/{contentId} 요청 시 존재하는 contentId와 수정 정보를 전달하면 200과 수정된 콘텐츠 정보를 반환한다")
    void updateContent_success() throws Exception {
        // given
        Long contentId = 1L;
        ContentUpdateRequest request = new ContentUpdateRequest("수정 제목", Category.TECH, "수정 본문");
        ContentUpdateResponse response = new ContentUpdateResponse(contentId, "수정 제목", Category.TECH, LocalDateTime.of(2026, 3, 18, 1, 0));

        given(contentService.updateContent(eq(contentId), any(ContentUpdateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/contents/{contentId}", contentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(contentId))
                .andExpect(jsonPath("$.title").value("수정 제목"))
                .andExpect(jsonPath("$.category").value("TECH"));
    }

    @Test
    @DisplayName("PUT /api/contents/{contentId} 요청 시 존재하지 않는 contentId를 전달하면 404를 반환한다")
    void updateContent_notFound_returns404() throws Exception {
        // given
        Long contentId = 999L;
        ContentUpdateRequest request = new ContentUpdateRequest("제목", Category.TECH, "본문");

        given(contentService.updateContent(eq(contentId), any(ContentUpdateRequest.class)))
                .willThrow(new ContentNotFoundException(contentId));

        // when & then
        mockMvc.perform(put("/api/contents/{contentId}", contentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("콘텐츠를 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("DELETE /api/contents/{contentId} 요청 시 존재하는 contentId를 전달하면 204를 반환한다")
    void deleteContent_success() throws Exception {
        // given
        Long contentId = 1L;
        willDoNothing().given(contentService).deleteContent(contentId);

        // when & then
        mockMvc.perform(delete("/api/contents/{contentId}", contentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/contents/{contentId} 요청 시 존재하지 않는 contentId를 전달하면 404를 반환한다")
    void deleteContent_notFound_returns404() throws Exception {
        // given
        Long contentId = 999L;
        willThrow(new ContentNotFoundException(contentId)).given(contentService).deleteContent(contentId);

        // when & then
        mockMvc.perform(delete("/api/contents/{contentId}", contentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("콘텐츠를 찾을 수 없습니다. id=999"));
    }
}
