package kr.sentinel.command;

import java.util.Optional;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.repository.CommandLogRepository;
import kr.sentinel.detection.PhysicalConsistencyChecker;
import kr.sentinel.detection.RuleEngine;
import kr.sentinel.event.DetectionPublishedEvent;
import kr.sentinel.event.repository.DetectionEventRepository;
import kr.sentinel.stream.SseHub;
import kr.sentinel.telemetry.domain.Telemetry;
import kr.sentinel.telemetry.repository.TelemetryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class ReplayService {

    private static final String RESET_EVENT = "reset";

    private final CommandLogRepository commandLogRepository;
    private final DetectionEventRepository detectionEventRepository;
    private final TelemetryRepository telemetryRepository;

    private final RuleEngine ruleEngine;
    private final PhysicalConsistencyChecker physicalConsistencyChecker;
    private final SseHub sseHub;

    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    @Getter
    private volatile boolean running = false;

    public synchronized void start() {
        running = true;
    }

    public synchronized void stop() {
        running = false;
    }

    public synchronized void reset() {
        running = false;

        transactionTemplate.executeWithoutResult(status -> {
            detectionEventRepository.deleteAllInBatch();
            commandLogRepository.resetAllProcessed();
            telemetryRepository.resetAllProcessed();
        });

        sseHub.send(RESET_EVENT, RESET_EVENT);
    }

    public long countProcessed() {
        return commandLogRepository.countByProcessedTrue();
    }

    @Scheduled(fixedDelay = 1000)
    public synchronized void processNext() {
        if (!running) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            Optional<CommandLog> commandLog = commandLogRepository.findFirstByProcessedFalseOrderByIssuedAtAscIdAsc();
            Optional<Telemetry> telemetry = telemetryRepository.findFirstByProcessedFalseOrderByMeasuredAtAscIdAsc();

            if (commandLog.isEmpty() && telemetry.isEmpty()) {
                running = false;
                return;
            }

            if (isCommandFirst(commandLog, telemetry)) {
                process(commandLog.get());
            } else {
                process(telemetry.get());
            }
        });
    }

    private boolean isCommandFirst(
            Optional<CommandLog> commandLog,
            Optional<Telemetry> telemetry
    ) {
        if (commandLog.isEmpty() || telemetry.isEmpty()) {
            return commandLog.isPresent();
        }

        return !commandLog.get().getIssuedAt().isAfter(telemetry.get().getMeasuredAt());
    }

    private void process(CommandLog commandLog) {
        ruleEngine.evaluate(commandLog).forEach(detection -> eventPublisher.publishEvent(
                new DetectionPublishedEvent(commandLog.getDeviceId(), commandLog.getId(), null, detection)));

        commandLog.markProcessed();
    }

    private void process(Telemetry telemetry) {
        physicalConsistencyChecker.check(telemetry).ifPresent(detection -> eventPublisher.publishEvent(
                new DetectionPublishedEvent(telemetry.getDeviceId(), null, telemetry.getId(), detection)));

        telemetry.markProcessed();
    }
}
