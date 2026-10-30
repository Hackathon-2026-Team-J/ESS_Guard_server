package kr.sentinel.detection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.sentinel.event.repository.DetectionEventRepository;
import kr.sentinel.telemetry.domain.Telemetry;
import kr.sentinel.telemetry.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhysicalConsistencyChecker {

    private static final String RULE_ID = "R4";
    private static final double DEVICE_CAPACITY_KWH = 500.0;
    private static final double PERCENT = 100.0;
    private static final double MILLIS_PER_HOUR = 3_600_000.0;
    private static final double TOLERANCE_RATIO = 0.15;
    private static final double MIN_GAP_PERCENT_POINT = 0.5;
    private static final Duration COMMAND_LOOKBACK = Duration.ofMinutes(2);

    private final DetectionEventRepository detectionEventRepository;
    private final TelemetryRepository telemetryRepository;

    public Optional<Detection> check(Telemetry telemetry) {
        return telemetryRepository
                .findFirstByDeviceIdAndMeasuredAtBeforeOrderByMeasuredAtDesc(
                        telemetry.getDeviceId(),
                        telemetry.getMeasuredAt())
                .flatMap(previous -> compare(previous, telemetry));
    }

    private Optional<Detection> compare(
            Telemetry previous,
            Telemetry telemetry
    ) {
        double elapsedHours = Duration.between(previous.getMeasuredAt(), telemetry.getMeasuredAt()).toMillis()
                / MILLIS_PER_HOUR;
        double expectedChange = -previous.getActualKw() * elapsedHours / DEVICE_CAPACITY_KWH * PERCENT;
        double actualChange = telemetry.getSoc() - previous.getSoc();
        double gap = Math.abs(actualChange - expectedChange);

        if (gap <= Math.abs(expectedChange) * TOLERANCE_RATIO || gap < MIN_GAP_PERCENT_POINT) {
            return Optional.empty();
        }

        if (hasRecentCommandDetection(telemetry)) {
            log.debug(
                    "R1~R3 탐지와 겹치는 SOC 불일치: telemetryId={}, 예상 {}%p, 실제 {}%p",
                    telemetry.getId(),
                    expectedChange,
                    actualChange
            );
            return Optional.empty();
        }

        String message = "SOC 변화 불일치: 예상 %.2f%%p, 실제 %.2f%%p".formatted(expectedChange, actualChange);

        return Optional.of(new Detection(RULE_ID, Severity.HIGH, message));
    }

    private boolean hasRecentCommandDetection(Telemetry telemetry) {
        LocalDateTime measuredAt = telemetry.getMeasuredAt();

        return detectionEventRepository.countCommandDetections(
                telemetry.getDeviceId(),
                measuredAt.minus(COMMAND_LOOKBACK),
                measuredAt
        ) > 0;
    }
}
