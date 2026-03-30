package ranking_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ranking_service.domain.DailyRanking;
import ranking_service.domain.RankingItem;
import ranking_service.domain.RankingType;
import ranking_service.service.RankingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    @Test
    @DisplayName("일별 조회수 랭킹 조회 - 200 반환")
    void getDailyViewRanking_200() throws Exception {
        // given
        DailyRanking ranking = DailyRanking.create(
                "20260324",
                RankingType.VIEW,
                List.of(
                        new RankingItem(1, 42L, 1500),
                        new RankingItem(2, 7L, 1200)
                ),
                LocalDateTime.now()
        );
        given(rankingService.getDailyRanking("20260324", RankingType.VIEW)).willReturn(ranking);

        // when & then
        mockMvc.perform(get("/api/rankings/views/daily")
                        .param("date", "20260324"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("20260324"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].rank").value(1))
                .andExpect(jsonPath("$.items[0].contentId").value(42L))
                .andExpect(jsonPath("$.items[0].viewCount").value(1500));
    }

    @Test
    @DisplayName("일별 좋아요 랭킹 조회 - 200 반환")
    void getDailyLikeRanking_200() throws Exception {
        // given
        DailyRanking ranking = DailyRanking.create(
                "20260324", RankingType.LIKE,
                List.of(new RankingItem(1, 15L, 300)),
                LocalDateTime.now()
        );
        given(rankingService.getDailyRanking("20260324", RankingType.LIKE)).willReturn(ranking);

        // when & then
        mockMvc.perform(get("/api/rankings/likes/daily")
                        .param("date", "20260324"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("20260324"))
                .andExpect(jsonPath("$.items[0].rank").value(1))
                .andExpect(jsonPath("$.items[0].contentId").value(15L))
                .andExpect(jsonPath("$.items[0].likeCount").value(300));

    }

    @Test
    @DisplayName("랭킹 데이터 없을 때 - 빈 items 200 반환")
    void getDailyViewRanking_데이터없음_빈리스트() throws Exception {
        // given
        DailyRanking emptyRanking = DailyRanking.create(
                "20260101", RankingType.VIEW, List.of(), null
        );
        given(rankingService.getDailyRanking("20260101", RankingType.VIEW)).willReturn(emptyRanking);

        // when & then
        mockMvc.perform(get("/api/rankings/views/daily")
                        .param("date", "20260101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("20260101"))
                .andExpect(jsonPath("$.items").isEmpty());
    }
}
