package kr.sentinel.detection.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.sentinel.command.domain.ClientRole;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.domain.EssState;
import kr.sentinel.command.repository.CommandLogRepository;
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
class RapidSwitchRuleTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 7, 9, 10);
    private static final String DEVICE_ID = "ESS-01";
    private static final int DISCHARGE_KW = 100;
    private static final int CHARGE_KW = -100;

    @Mock
    private CommandLogRepository commandLogRepository;

    @Mock
    private DispatchPlanRepository dispatchPlanRepository;

    @InjectMocks
    private RapidSwitchRule rule;

    @Test
    void 전환이_3회면_탐지하지_않는다() {
        givenProcessed(
                command(180, DISCHARGE_KW),
                command(120, CHARGE_KW),
                command(60, DISCHARGE_KW)
        );

        assertThat(rule.check(command(0, CHARGE_KW))).isEmpty();
    }

    @Test
    void 전환이_4회면_탐지한다() {
        givenProcessed(
                command(240, DISCHARGE_KW),
                command(180, CHARGE_KW),
                command(120, DISCHARGE_KW),
                command(60, CHARGE_KW)
        );

        Optional<Detection> detection = rule.check(command(0, DISCHARGE_KW));

        assertThat(detection).isPresent();
        assertThat(detection.get().severity()).isEqualTo(Severity.HIGH);
        assertThat(detection.get().message()).isEqualTo("최근 5분간 방향 전환 4회");
    }

    @Test
    void 대기_명령은_전환으로_세지_않는다() {
        givenProcessed(
                command(240, DISCHARGE_KW),
                command(180, 0),
                command(120, DISCHARGE_KW),
                command(60, CHARGE_KW)
        );

        assertThat(rule.check(command(0, DISCHARGE_KW))).isEmpty();
    }

    @Test
    void 계획_전환_30초_안의_전환은_제외한다() {
        givenProcessed(
                command(240, DISCHARGE_KW),
                command(180, CHARGE_KW),
                command(120, DISCHARGE_KW),
                command(60, CHARGE_KW)
        );
        given(dispatchPlanRepository.findAllByDeviceIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                any(),
                any(),
                any()
        )).willReturn(List.of(
                plan(600, 90, 100),
                plan(90, -600, -100)
        ));

        assertThat(rule.check(command(0, DISCHARGE_KW))).isEmpty();
    }

    @Test
    void 계획_공백으로_대기가_되는_시각도_계획_전환으로_본다() {
        givenProcessed(
                command(240, DISCHARGE_KW),
                command(180, CHARGE_KW),
                command(120, DISCHARGE_KW),
                command(60, CHARGE_KW)
        );
        given(dispatchPlanRepository.findAllByDeviceIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                any(),
                any(),
                any()
        )).willReturn(List.of(
                plan(600, 90, 100),
                plan(30, -600, 100)
        ));

        assertThat(rule.check(command(0, DISCHARGE_KW))).isEmpty();
    }

    @Test
    void 계획_전환_30초_밖의_전환은_제외하지_않는다() {
        givenProcessed(
                command(240, DISCHARGE_KW),
                command(180, CHARGE_KW),
                command(120, DISCHARGE_KW),
                command(60, CHARGE_KW)
        );
        given(dispatchPlanRepository.findAllByDeviceIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                any(),
                any(),
                any()
        )).willReturn(List.of(
                plan(600, 271, 100),
                plan(271, -600, -100)
        ));

        assertThat(rule.check(command(0, DISCHARGE_KW))).isPresent();
    }

    @Test
    void 직전_방향을_59초_유지하고_전환하면_탐지한다() {
        givenProcessed(command(59, DISCHARGE_KW));

        Optional<Detection> detection = rule.check(command(0, CHARGE_KW));

        assertThat(detection).isPresent();
        assertThat(detection.get().message()).isEqualTo("직전 방향 59초 유지 후 방향 전환");
    }

    @Test
    void 직전_방향을_60초_유지하고_전환하면_탐지하지_않는다() {
        givenProcessed(command(60, DISCHARGE_KW));

        assertThat(rule.check(command(0, CHARGE_KW))).isEmpty();
    }

    @Test
    void 계획_전환에_따른_전환은_59초_유지여도_탐지하지_않는다() {
        givenProcessed(command(59, DISCHARGE_KW));
        given(dispatchPlanRepository.findAllByDeviceIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                any(),
                any(),
                any()
        )).willReturn(List.of(
                plan(600, 10, 100),
                plan(10, -600, -100)
        ));

        assertThat(rule.check(command(0, CHARGE_KW))).isEmpty();
    }

    @Test
    void 쓰기가_아닌_명령은_대상이_아니다() {
        assertThat(rule.check(command(0, 3, CHARGE_KW))).isEmpty();
    }

    private void givenProcessed(CommandLog... commands) {
        given(commandLogRepository.findAllByDeviceIdAndProcessedTrueAndIssuedAtBetweenOrderByIssuedAtAscIdAsc(
                DEVICE_ID,
                NOW.minusMinutes(5),
                NOW
        )).willReturn(List.of(commands));
    }

    private DispatchPlan plan(
            int startSecondsAgo,
            int endSecondsAgo,
            int plannedKw
    ) {
        return DispatchPlan.create(
                DEVICE_ID,
                NOW.minusSeconds(startSecondsAgo),
                NOW.minusSeconds(endSecondsAgo),
                plannedKw
        );
    }

    private CommandLog command(
            int secondsAgo,
            int commandKw
    ) {
        return command(secondsAgo, 6, commandKw);
    }

    private CommandLog command(
            int secondsAgo,
            int functionCode,
            int commandKw
    ) {
        return CommandLog.create(
                NOW.minusSeconds(secondsAgo),
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
