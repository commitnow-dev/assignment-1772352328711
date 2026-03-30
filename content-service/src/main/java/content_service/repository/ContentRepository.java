package content_service.repository;

import content_service.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRepository extends JpaRepository<Content,Long> {

    @Modifying
    @Query("UPDATE Content c SET c.likeCount = c.likeCount + 1 WHERE c.id = :contentId")
    void incrementLikeCount(@Param("contentId") Long contentId);

    @Modifying
    @Query("UPDATE Content c SET c.likeCount = c.likeCount - 1 WHERE c.id = :contentId")
    void decrementLikeCount(@Param("contentId") Long contentId);
}
