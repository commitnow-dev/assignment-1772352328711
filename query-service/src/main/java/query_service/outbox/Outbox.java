package query_service.outbox;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {

    @Id
    private String id;

    private String topic;
    private String payload;
    private LocalDateTime createdAt;

    public static Outbox create(String topic, String payload) {
        Outbox outbox = new Outbox();
        outbox.topic = topic;
        outbox.payload = payload;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }
}