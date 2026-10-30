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
public class DirectionMismatchRule implements Rule {

    private static final String RULE_ID = "R1";
    private static final int IDLE_KW = 0;

    private final DispatchPlanRepository dispatchPlanRepository;

    @Override
    public Optional<Detection> check(CommandLog log) {
        Direction commandDirection = log.getDirection();
        if (!log.isWrite() || commandDirection == Direction.IDLE) {
            return Optional.empty();
        }

        Direction planDirection = Direction.from(findPlannedKw(log));
        if (commandDirection == planDirection) {
            return Optional.empty();
        }

        return Optional.of(new Detection(RULE_ID, Severity.HIGH, createMessage(planDirection, commandDirection)));
    }

    private int findPlannedKw(CommandLog log) {
        return dispatchPlanRepository.findFirstByDeviceIdAndStartAtLessThanEqualAndEndAtGreaterThan(
                        log.getDeviceId(),
                        log.getIssuedAt(),
                        log.getIssuedAt())
                .map(DispatchPlan::getPlannedKw)
                .orElse(IDLE_KW);
    }

    private String createMessage(
            Direction planDirection,
            Direction commandDirection
    ) {
        if (planDirection == Direction.IDLE) {
            return "계획 방향(%s)과 다른 %s 명령".formatted(planDirection.getLabel(), commandDirection.getLabel());
        }

        return "계획 방향(%s)과 반대인 %s 명령".formatted(planDirection.getLabel(), commandDirection.getLabel());
    }
}
