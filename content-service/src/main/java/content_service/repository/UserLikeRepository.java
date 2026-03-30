package content_service.repository;

import content_service.domain.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    Optional<UserLike> findByContentIdAndUserId(Long contentId, String userId);

    boolean existsByContentIdAndUserId(Long contentId, String userId);

    void deleteByContentIdAndUserId(Long contentId, String userId);
}
