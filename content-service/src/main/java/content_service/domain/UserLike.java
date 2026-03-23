package content_service.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contentId", "userId"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contentId;
    private String userId;

    public static UserLike create(Long contentId, String userId) {
        UserLike userLike = new UserLike();
        userLike.contentId = contentId;
        userLike.userId = userId;
        return userLike;
    }
}
