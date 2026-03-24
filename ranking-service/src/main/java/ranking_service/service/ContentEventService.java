package ranking_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ranking_service.common.consumer.dto.ContentEventMessage;
import ranking_service.common.event.ContentEvent;
import ranking_service.repository.ContentEventRepository;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentEventService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ContentEventRepository contentEventRepository;

    public void save(ContentEventMessage message) {
        if (contentEventRepository.existsByEventId(message.eventId())) {
            log.warn("[ContentEventService] Duplicate event ignored: eventId={}", message.eventId());
            return;
        }

        String date = message.occurredAt().format(DATE_FORMATTER);

        ContentEvent event = ContentEvent.create(
                message.eventId(),
                message.eventType(),
                message.contentId(),
                message.userId(),
                message.occurredAt(),
                date
        );

        contentEventRepository.save(event);
    }
}