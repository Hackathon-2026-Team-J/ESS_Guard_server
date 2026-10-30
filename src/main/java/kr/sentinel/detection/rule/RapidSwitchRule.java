package kr.sentinel.detection.rule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.domain.Direction;
import kr.sentinel.command.repository.CommandLogRepository;
import kr.sentinel.detection.Detection;
import kr.sentinel.detection.Rule;
import kr.sentinel.detection.Severity;
import kr.sentinel.plan.domain.DispatchPlan;
import kr.sentinel.plan.repository.DispatchPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RapidSwitchRule implements Rule {

    private static final String RULE_ID = "R3";
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final Duration PLAN_CHANGE_MARGIN = Duration.ofSeconds(30);
    private static final Duration MIN_HOLD = Duration.ofSeconds(60);
    private static final int MAX_SWITCHES = 4;

    private final CommandLogRepository commandLogRepository;
    private final DispatchPlanRepository dispatchPlanRepository;

    @Override
    public Optional<Detection> check(CommandLog log) {
        if (!log.isWrite()) {
            return Optional.empty();
        }

        LocalDateTime windowStart = log.getIssuedAt().minus(WINDOW);
        List<LocalDateTime> planChangeTimes = findPlanChangeTimes(log, windowStart);

        int switchCount = 0;
        Duration switchedAfter = null;
        Direction currentDirection = null;
        LocalDateTime directionStartedAt = null;

        for (CommandLog command : findRecentWrites(log, windowStart)) {
            Direction direction = command.getDirection();
            if (direction == Direction.IDLE || direction == currentDirection) {
                continue;
            }

            if (currentDirection != null && !isNearPlanChange(command.getIssuedAt(), planChangeTimes)) {
                switchCount++;
                switchedAfter = command == log
                        ? Duration.between(directionStartedAt, command.getIssuedAt())
                        : null;
            }

            currentDirection = direction;
            directionStartedAt = command.getIssuedAt();
        }

        if (switchCount >= MAX_SWITCHES) {
            String message = "최근 %d분간 방향 전환 %d회".formatted(WINDOW.toMinutes(), switchCount);
            return Optional.of(new Detection(RULE_ID, Severity.HIGH, message));
        }

        if (switchedAfter != null && switchedAfter.compareTo(MIN_HOLD) < 0) {
            String message = "직전 방향 %d초 유지 후 방향 전환".formatted(switchedAfter.toSeconds());
            return Optional.of(new Detection(RULE_ID, Severity.HIGH, message));
        }

        return Optional.empty();
    }

    private List<LocalDateTime> findPlanChangeTimes(
            CommandLog log,
            LocalDateTime windowStart
    ) {
        List<DispatchPlan> plans = dispatchPlanRepository
                .findAllByDeviceIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                        log.getDeviceId(),
                        log.getIssuedAt().plus(PLAN_CHANGE_MARGIN),
                        windowStart.minus(PLAN_CHANGE_MARGIN));

        List<LocalDateTime> changeTimes = new ArrayList<>();
        Direction previousDirection = Direction.IDLE;
        LocalDateTime previousEndAt = null;

        for (DispatchPlan plan : plans) {
            boolean hasGap = previousEndAt != null && previousEndAt.isBefore(plan.getStartAt());
            if (hasGap && previousDirection != Direction.IDLE) {
                changeTimes.add(previousEndAt);
                previousDirection = Direction.IDLE;
            }

            if (plan.getDirection() != previousDirection) {
                changeTimes.add(plan.getStartAt());
            }

            previousDirection = plan.getDirection();
            previousEndAt = plan.getEndAt();
        }

        if (previousDirection != Direction.IDLE) {
            changeTimes.add(previousEndAt);
        }

        return changeTimes;
    }

    private List<CommandLog> findRecentWrites(
            CommandLog log,
            LocalDateTime windowStart
    ) {
        List<CommandLog> writes = new ArrayList<>(commandLogRepository
                .findAllByDeviceIdAndProcessedTrueAndIssuedAtBetweenOrderByIssuedAtAscIdAsc(
                        log.getDeviceId(),
                        windowStart,
                        log.getIssuedAt())
                .stream()
                .filter(CommandLog::isWrite)
                .toList());
        writes.add(log);

        return writes;
    }

    private boolean isNearPlanChange(
            LocalDateTime switchedAt,
            List<LocalDateTime> planChangeTimes
    ) {
        return planChangeTimes.stream()
                .anyMatch(changeTime ->
                        Duration.between(changeTime, switchedAt).abs().compareTo(PLAN_CHANGE_MARGIN) <= 0);
    }
}
