package ranking_service.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ranking_service.service.RankingService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingBatchScheduler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RankingService rankingService;

    @Scheduled(cron = "0 0 1 * * *")  // 매일 01:00
    public void aggregateDailyRanking() {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        log.info("[RankingBatchScheduler] Starting daily aggregation for date={}", yesterday);
        rankingService.aggregateAndSave(yesterday);
        log.info("[RankingBatchScheduler] Daily aggregation completed for date={}", yesterday);
    }
}
