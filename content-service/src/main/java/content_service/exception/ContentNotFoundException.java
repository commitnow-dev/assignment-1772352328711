package content_service.exception;

public class ContentNotFoundException extends RuntimeException {

    public ContentNotFoundException(Long contentId) {
        super("콘텐츠를 찾을 수 없습니다. id=" + contentId);
    }
}
