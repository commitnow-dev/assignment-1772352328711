package ranking_service.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Document(collection = "daily_rankings")
@CompoundIndex(def = "{'date': 1, 'rankingType': 1}", unique = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRanking {

    @Id
    private String id;

    private String date;              // "20260301"
    private RankingType rankingType;  // VIEW or LIKE
    private List<RankingItem> items;
    private LocalDateTime generatedAt;

    public static DailyRanking create(String date, RankingType rankingType,
                                      List<RankingItem> items, LocalDateTime generatedAt) {
        DailyRanking ranking = new DailyRanking();
        ranking.date = date;
        ranking.rankingType = rankingType;
        ranking.items = items;
        ranking.generatedAt = generatedAt;
        return ranking;
    }
}