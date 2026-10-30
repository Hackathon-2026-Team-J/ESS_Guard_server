package kr.sentinel.detection.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.sentinel.command.domain.ClientRole;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.domain.EssState;
import kr.sentinel.detection.Detection;
import kr.sentinel.detection.Severity;
import kr.sentinel.plan.domain.DispatchPlan;
import kr.sentinel.plan.repository.DispatchPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionMismatchRuleTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 7, 9, 0);
    private static final String DEVICE_ID = "ESS-01";

    @Mock
    private DispatchPlanRepository dispatchPlanRepository;

    @InjectMocks
    private DirectionMismatchRule rule;

    @Test
    void 충전_계획에_방전_명령이면_탐지한다() {
        givenPlannedKw(-100);

        Optional<Detection> detection = rule.check(command(6, 100));

        assertThat(detection).isPresent();
        assertThat(detection.get().severity()).isEqualTo(Severity.HIGH);
        assertThat(detection.get().message()).isEqualTo("계획 방향(충전)과 반대인 방전 명령");
    }

    @Test
    void 대기_계획에_0_명령이면_탐지하지_않는다() {
        assertThat(rule.check(command(6, 0))).isEmpty();
    }

    @Test
    void 대기_계획에_충전_명령이면_탐지한다() {
        givenPlannedKw(0);

        assertThat(rule.check(command(6, -50))).isPresent();
    }

    @Test
    void 계획이_없으면_대기로_보고_충전_명령을_탐지한다() {
        given(dispatchPlanRepository.findFirstByDeviceIdAndStartAtLessThanEqualAndEndAtGreaterThan(any(), any(), any()))
                .willReturn(Optional.empty());

        assertThat(rule.check(command(6, -50))).isPresent();
    }

    @Test
    void 계획과_방향이_같으면_탐지하지_않는다() {
        givenPlannedKw(100);

        assertThat(rule.check(command(6, 80))).isEmpty();
    }

    @Test
    void 쓰기_명령이_아니면_대상이_아니다() {
        assertThat(rule.check(command(3, 100))).isEmpty();
    }

    private void givenPlannedKw(int plannedKw) {
        given(dispatchPlanRepository.findFirstByDeviceIdAndStartAtLessThanEqualAndEndAtGreaterThan(
                DEVICE_ID,
                NOW,
                NOW
        )).willReturn(Optional.of(DispatchPlan.create(DEVICE_ID, NOW.minusHours(1), NOW.plusHours(1), plannedKw)));
    }

    private CommandLog command(
            int functionCode,
            int commandKw
    ) {
        return CommandLog.create(
                NOW,
                "EMS-01",
                ClientRole.OPERATOR,
                DEVICE_ID,
                functionCode,
                10,
                commandKw,
                commandKw,
                EssState.RUNNING,
                false
        );
    }
}
