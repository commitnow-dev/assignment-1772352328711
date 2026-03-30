package ranking_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ranking_service.domain.DailyRanking;
import ranking_service.domain.RankingType;

import java.util.Optional;

public interface DailyRankingRepository extends MongoRepository<DailyRanking, String> {

    Optional<DailyRanking> findByDateAndRankingType(String date, RankingType rankingType);

    void deleteByDateAndRankingType(String date, RankingType rankingType);
}