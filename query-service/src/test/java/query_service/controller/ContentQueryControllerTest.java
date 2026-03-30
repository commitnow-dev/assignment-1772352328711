package query_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import query_service.controller.dto.ContentPageResponse;
import query_service.controller.dto.ContentResponse;
import query_service.controller.dto.ContentSummaryResponse;
import query_service.service.ContentQueryService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContentQueryController.class)
class ContentQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContentQueryService contentQueryService;

    @Test
    @DisplayName("단건 컨텐츠 조회 - 200 반환")
    void getContent_200() throws Exception {
        // given
        ContentResponse response = new ContentResponse(
                1L, "제목", "TECH", "본문", 10,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(contentQueryService.getContent(1L, "user1")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/contents/1")
                        .header("X-User-Id", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(1L))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.category").value("TECH"));
    }

    @Test
    @DisplayName("단건 컨텐츠 조회 - X-User-Id 헤더 없어도 200 반환")
    void getContent_헤더없음_200() throws Exception {
        // given
        ContentResponse response = new ContentResponse(
                1L, "제목", "TECH", "본문", 10,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(contentQueryService.getContent(1L, null)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/contents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(1L));
    }

    @Test
    @DisplayName("목록 조회 - 기본 파라미터 (page=0, size=20)")
    void getContents_기본파라미터_200() throws Exception {
        // given
        ContentPageResponse response = new ContentPageResponse(
                List.of(new ContentSummaryResponse(1L, "제목", "TECH", 10, LocalDateTime.now())),
                1L, 1, 0, 20
        );
        given(contentQueryService.getContents(0, 20)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1L))
                .andExpect(jsonPath("$.content[0].contentId").value(1L));
    }

    @Test
    @DisplayName("목록 조회 - 커스텀 파라미터 (page=1, size=10)")
    void getContents_커스텀파라미터_200() throws Exception {
        // given
        ContentPageResponse response = new ContentPageResponse(
                List.of(), 0L, 0, 1, 10
        );
        given(contentQueryService.getContents(1, 10)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/contents")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }
}

