package kr.sentinel.detection.rule;

import java.util.Optional;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.domain.Direction;
import kr.sentinel.detection.Detection;
import kr.sentinel.detection.Rule;
import kr.sentinel.detection.Severity;
import kr.sentinel.plan.domain.DispatchPlan;
import kr.sentinel.plan.repository.DispatchPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutputDeviationRule implements Rule {

    private static final String RULE_ID = "R2";
    private static final int IDLE_KW = 0;
    private static final int MIN_DEVIATION_KW = 2;
    private static final int DETECT_RATIO_PERCENT = 20;
    private static final int HIGH_RATIO_PERCENT = 50;
    private static final double PERCENT = 100.0;

    private final DispatchPlanRepository dispatchPlanRepository;

    @Override
    public Optional<Detection> check(CommandLog log) {
        if (!log.isWrite()) {
            return Optional.empty();
        }

        int plannedKw = findPlannedKw(log);
        if (plannedKw == IDLE_KW || log.getDirection() != Direction.from(plannedKw)) {
            return Optional.empty();
        }

        int deviationKw = Math.abs(log.getCommandKw() - plannedKw);
        int plannedAbsKw = Math.abs(plannedKw);
        if (!exceedsRatio(deviationKw, plannedAbsKw, DETECT_RATIO_PERCENT) || deviationKw < MIN_DEVIATION_KW) {
            return Optional.empty();
        }

        Severity severity = exceedsRatio(deviationKw, plannedAbsKw, HIGH_RATIO_PERCENT)
                ? Severity.HIGH
                : Severity.MEDIUM;
        String message = "계획 %dkW 대비 명령 %dkW (편차 %.1f%%)"
                .formatted(plannedKw, log.getCommandKw(), deviationKw * PERCENT / plannedAbsKw);

        return Optional.of(new Detection(RULE_ID, severity, message));
    }

    private int findPlannedKw(CommandLog log) {
        return dispatchPlanRepository.findFirstByDeviceIdAndStartAtLessThanEqualAndEndAtGreaterThan(
                        log.getDeviceId(),
                        log.getIssuedAt(),
                        log.getIssuedAt())
                .map(DispatchPlan::getPlannedKw)
                .orElse(IDLE_KW);
    }

    private boolean exceedsRatio(
            int deviationKw,
            int plannedAbsKw,
            int ratioPercent
    ) {
        return deviationKw * 100 > plannedAbsKw * ratioPercent;
    }
}
