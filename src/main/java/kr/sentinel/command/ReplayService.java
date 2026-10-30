package kr.sentinel.command;

import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.repository.CommandLogRepository;
import kr.sentinel.detection.RuleEngine;
import kr.sentinel.event.DetectionPublishedEvent;
import kr.sentinel.event.repository.DetectionEventRepository;
import kr.sentinel.stream.SseHub;
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

    private final RuleEngine ruleEngine;
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

        transactionTemplate.executeWithoutResult(status ->
                commandLogRepository.findFirstByProcessedFalseOrderByIdAsc()
                        .ifPresentOrElse(this::process, () -> running = false));
    }

    private void process(CommandLog commandLog) {
        ruleEngine.evaluate(commandLog).forEach(detection ->
                eventPublisher.publishEvent(new DetectionPublishedEvent(commandLog.getId(), detection)));

        commandLog.markProcessed();
    }
}
