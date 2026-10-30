package kr.sentinel.event;

import java.time.LocalDateTime;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.repository.CommandLogRepository;
import kr.sentinel.event.domain.DetectionEvent;
import kr.sentinel.event.dto.EventResponse;
import kr.sentinel.event.repository.DetectionEventRepository;
import kr.sentinel.stream.SseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DetectionListener {

    private static final String DETECTION_EVENT = "detection";

    private final CommandLogRepository commandLogRepository;
    private final DetectionEventRepository detectionEventRepository;

    private final SseHub sseHub;

    @EventListener
    public void handle(DetectionPublishedEvent event) {
        DetectionEvent detectionEvent = detectionEventRepository.save(DetectionEvent.create(
                event.deviceId(),
                event.commandLogId(),
                event.telemetryId(),
                event.detection(),
                LocalDateTime.now()
        ));
        CommandLog commandLog = event.commandLogId() == null
                ? null
                : commandLogRepository.getReferenceById(event.commandLogId());

        sseHub.send(DETECTION_EVENT, EventResponse.of(detectionEvent, commandLog));
    }
}
