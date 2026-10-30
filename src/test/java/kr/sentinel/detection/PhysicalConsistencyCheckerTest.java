package kr.sentinel.detection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.sentinel.event.repository.DetectionEventRepository;
import kr.sentinel.telemetry.domain.Telemetry;
import kr.sentinel.telemetry.repository.TelemetryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhysicalConsistencyCheckerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 7, 9, 6);
    private static final String DEVICE_ID = "ESS-01";
    private static final double PREVIOUS_SOC = 50.0;

    @Mock
    private DetectionEventRepository detectionEventRepository;

    @Mock
    private TelemetryRepository telemetryRepository;

    @InjectMocks
    private PhysicalConsistencyChecker checker;

    @Test
    void 예상과_일치하면_탐지하지_않는다() {
        givenPrevious();

        assertThat(checker.check(telemetry(48.0))).isEmpty();
    }

    @Test
    void 비율은_넘어도_0_5퍼센트포인트_미만이면_탐지하지_않는다() {
        givenPrevious();

        assertThat(checker.check(telemetry(47.6))).isEmpty();
    }

    @Test
    void 둘_다_넘고_직전_탐지가_없으면_HIGH로_탐지한다() {
        givenPrevious();
        given(detectionEventRepository.countCommandDetections(DEVICE_ID, NOW.minusMinutes(2), NOW)).willReturn(0L);

        Optional<Detection> detection = checker.check(telemetry(47.0));

        assertThat(detection).isPresent();
        assertThat(detection.get().ruleId()).isEqualTo("R4");
        assertThat(detection.get().severity()).isEqualTo(Severity.HIGH);
        assertThat(detection.get().message()).isEqualTo("SOC 변화 불일치: 예상 -2.00%p, 실제 -3.00%p");
    }

    @Test
    void 직전_2분_안에_명령_탐지가_있으면_이벤트를_만들지_않는다() {
        givenPrevious();
        given(detectionEventRepository.countCommandDetections(DEVICE_ID, NOW.minusMinutes(2), NOW)).willReturn(1L);

        assertThat(checker.check(telemetry(47.0))).isEmpty();
    }

    @Test
    void 직전_측정값이_없으면_탐지하지_않는다() {
        given(telemetryRepository.findFirstByDeviceIdAndMeasuredAtBeforeOrderByMeasuredAtDesc(any(), any()))
                .willReturn(Optional.empty());

        assertThat(checker.check(telemetry(10.0))).isEmpty();
    }

    private void givenPrevious() {
        Telemetry previous = Telemetry.create(DEVICE_ID, NOW.minusMinutes(6), 100, PREVIOUS_SOC, false);

        given(telemetryRepository.findFirstByDeviceIdAndMeasuredAtBeforeOrderByMeasuredAtDesc(DEVICE_ID, NOW))
                .willReturn(Optional.of(previous));
    }

    private Telemetry telemetry(double soc) {
        return Telemetry.create(DEVICE_ID, NOW, 100, soc, false);
    }
}
