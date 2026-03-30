package content_service.service;

import content_service.controller.dto.LikeRequest;
import content_service.domain.Category;
import content_service.domain.Content;
import content_service.outbox.OutboxEventPublisher;
import content_service.repository.ContentRepository;
import content_service.repository.UserLikeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LikeConcurrencyTest {

    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserLikeRepository userLikeRepository;

    @MockitoBean
    private OutboxEventPublisher outboxEventPublisher;

    private Long contentId;

    @BeforeEach
    void setUp() {
        Content content = Content.create("테스트 콘텐츠", Category.TECH, "본문");
        contentId = contentRepository.save(content).getId();
    }

    @AfterEach
    void tearDown() {
        userLikeRepository.deleteAll();
        contentRepository.deleteAll();
    }

    @Test
    @DisplayName("서로 다른 100명이 동시에 좋아요를 누르면 likeCount는 100이어야 한다")
    void concurrentLike_differentUsers_likeCountShouldBe100() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            String userId = "user-" + i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    contentService.toggleLike(contentId, new LikeRequest(userId));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        Content result = contentRepository.findById(contentId).orElseThrow();
        assertThat(successCount.get()).isEqualTo(100);
        assertThat(failCount.get()).isEqualTo(0);
        assertThat(result.getLikeCount()).isEqualTo(100);
        assertThat(userLikeRepository.count()).isEqualTo(100);
    }

    @Test
    @DisplayName("같은 유저가 동시에 100번 좋아요를 누르면 UserLike는 최대 1개만 존재한다")
    void concurrentLike_sameUser_noDuplicateRow() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    contentService.toggleLike(contentId, new LikeRequest("same-user"));
                } catch (Exception ignored) {
                    // 유니크 제약 위반 예외는 정상 동작
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // 핵심 검증: 유니크 제약으로 같은 유저의 중복 행은 절대 생기지 않는다
        assertThat(userLikeRepository.count()).isLessThanOrEqualTo(1);
    }
}
