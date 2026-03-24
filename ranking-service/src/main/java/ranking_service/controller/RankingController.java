package ranking_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ranking_service.controller.dto.DailyLikeRankingResponse;
import ranking_service.controller.dto.DailyViewRankingResponse;
import ranking_service.domain.DailyRanking;
import ranking_service.domain.RankingType;
import ranking_service.service.RankingService;

@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/api/rankings/views/daily")
    public ResponseEntity<DailyViewRankingResponse> getDailyViewRanking(@RequestParam String date) {
        DailyRanking ranking = rankingService.getDailyRanking(date, RankingType.VIEW);
        return ResponseEntity.ok(DailyViewRankingResponse.from(ranking));
    }

    @GetMapping("/api/rankings/likes/daily")
    public ResponseEntity<DailyLikeRankingResponse> getDailyLikeRanking(@RequestParam String date) {
        DailyRanking ranking = rankingService.getDailyRanking(date, RankingType.LIKE);
        return ResponseEntity.ok(DailyLikeRankingResponse.from(ranking));
    }
}
