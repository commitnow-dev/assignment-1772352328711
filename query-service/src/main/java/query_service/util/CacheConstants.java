package query_service.util;

import java.time.Duration;

public final class CacheConstants {

    private CacheConstants() {}

    public static final String CONTENT_CACHE_PREFIX = "content:";
    public static final Duration CONTENT_CACHE_TTL = Duration.ofMinutes(1);
}