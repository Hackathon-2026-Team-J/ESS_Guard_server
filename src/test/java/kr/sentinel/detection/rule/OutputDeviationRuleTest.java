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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutputDeviationRuleTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 7, 9, 0);
    private static final String DEVICE_ID = "ESS-01";

    @Mock
    private DispatchPlanRepository dispatchPlanRepository;

    @InjectMocks
    private OutputDeviationRule rule;

    @Test
    void 비율이_20퍼센트면_탐지하지_않는다() {
        givenPlannedKw(100);

        assertThat(rule.check(command(120))).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "100, 121, MEDIUM",
            "100, 150, MEDIUM",
            "100, 151, HIGH",
            "-100, -121, MEDIUM",
            "-100, -151, HIGH"
    })
    void 비율에_따라_심각도가_정해진다(
            int plannedKw,
            int commandKw,
            Severity severity
    ) {
        givenPlannedKw(plannedKw);

        Optional<Detection> detection = rule.check(command(commandKw));

        assertThat(detection).isPresent();
        assertThat(detection.get().severity()).isEqualTo(severity);
    }

    @Test
    void 메시지에_계획_명령_비율을_포함한다() {
        givenPlannedKw(100);

        assertThat(rule.check(command(121)).get().message()).isEqualTo("계획 100kW 대비 명령 121kW (편차 21.0%)");
    }

    @Test
    void 비율을_넘어도_차이가_2kW_미만이면_탐지하지_않는다() {
        givenPlannedKw(4);

        assertThat(rule.check(command(5))).isEmpty();
    }

    @Test
    void 방향이_다르면_대상이_아니다() {
        givenPlannedKw(100);

        assertThat(rule.check(command(-100))).isEmpty();
    }

    @Test
    void 계획이_없으면_대상이_아니다() {
        given(dispatchPlanRepository.findFirstByDeviceIdAndStartAtLessThanEqualAndEndAtGreaterThan(any(), any(), any()))
                .willReturn(Optional.empty());

        assertThat(rule.check(command(100))).isEmpty();
    }

    private void givenPlannedKw(int plannedKw) {
        given(dispatchPlanRepository.findFirstByDeviceIdAndStartAtLessThanEqualAndEndAtGreaterThan(
                DEVICE_ID,
                NOW,
                NOW
        )).willReturn(Optional.of(DispatchPlan.create(DEVICE_ID, NOW.minusHours(1), NOW.plusHours(1), plannedKw)));
    }

    private CommandLog command(int commandKw) {
        return CommandLog.create(
                NOW,
                "EMS-01",
                ClientRole.OPERATOR,
                DEVICE_ID,
                6,
                10,
                commandKw,
                commandKw,
                EssState.RUNNING,
                false
        );
    }
}
