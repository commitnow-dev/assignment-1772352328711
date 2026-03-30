package query_service.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxRepository extends MongoRepository<Outbox, String> {

    List<Outbox> findAllByCreatedAtLessThanEqualOrderByCreatedAtAsc(LocalDateTime before);
}