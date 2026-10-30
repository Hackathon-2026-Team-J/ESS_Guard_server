package kr.sentinel.event;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.repository.CommandLogRepository;
import kr.sentinel.detection.Severity;
import kr.sentinel.event.domain.DetectionEvent;
import kr.sentinel.event.dto.EventResponse;
import kr.sentinel.event.dto.StatsResponse;
import kr.sentinel.event.repository.DetectionEventRepository;
import kr.sentinel.event.repository.RuleCount;
import kr.sentinel.event.repository.SeverityCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final CommandLogRepository commandLogRepository;
    private final DetectionEventRepository detectionEventRepository;

    public List<EventResponse> getEvents(int limit) {
        List<DetectionEvent> events = detectionEventRepository.findAllByOrderByIdDesc(PageRequest.of(0, limit));
        Map<Long, CommandLog> commandIdToLog = getCommandIdToLog(events);

        return events.stream()
                .map(event -> EventResponse.of(event, commandIdToLog.get(event.getCommandLogId())))
                .toList();
    }

    private Map<Long, CommandLog> getCommandIdToLog(List<DetectionEvent> events) {
        List<Long> commandLogIds = events.stream()
                .map(DetectionEvent::getCommandLogId)
                .distinct()
                .toList();

        return commandLogRepository.findAllById(commandLogIds).stream()
                .collect(Collectors.toMap(CommandLog::getId, Function.identity()));
    }

    public StatsResponse getStats() {
        Map<Severity, Long> severityToCount = detectionEventRepository.countGroupBySeverity().stream()
                .collect(Collectors.toMap(SeverityCount::getSeverity, SeverityCount::getCount));
        Map<String, Long> ruleIdToCount = detectionEventRepository.countGroupByRuleId().stream()
                .collect(Collectors.toMap(RuleCount::getRuleId, RuleCount::getCount));

        return StatsResponse.of(
                commandLogRepository.countByProcessedTrue(),
                commandLogRepository.count(),
                detectionEventRepository.count(),
                commandLogRepository.countByLabeledAnomalyTrue(),
                severityToCount,
                ruleIdToCount
        );
    }
}
