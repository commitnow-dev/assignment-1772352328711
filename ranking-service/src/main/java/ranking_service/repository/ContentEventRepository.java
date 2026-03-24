package ranking_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ranking_service.common.event.ContentEvent;

public interface ContentEventRepository extends MongoRepository<ContentEvent, String> {

    boolean existsByEventId(String eventId);
}
