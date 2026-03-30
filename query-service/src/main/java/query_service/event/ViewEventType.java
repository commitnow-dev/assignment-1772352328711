package query_service.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ViewEventType {
    VIEW("content-viewed");

    private final String topic;
}
