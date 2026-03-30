package content_service.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LikeEventType {
    LIKE_ADDED("content-liked"),
    LIKE_REMOVED("content-liked");

    private final String topic;
}
