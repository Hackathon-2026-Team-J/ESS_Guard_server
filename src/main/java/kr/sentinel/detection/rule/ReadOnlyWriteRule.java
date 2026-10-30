package kr.sentinel.detection.rule;

import java.util.Optional;
import kr.sentinel.command.domain.ClientRole;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.detection.Detection;
import kr.sentinel.detection.Rule;
import kr.sentinel.detection.Severity;
import org.springframework.stereotype.Component;

@Component
public class ReadOnlyWriteRule implements Rule {

    private static final String RULE_ID = "R2";
    private static final Severity SEVERITY = Severity.HIGH;
    private static final String MESSAGE = "읽기 전용 계정의 쓰기 명령";

    @Override
    public Optional<Detection> check(CommandLog log) {
        if (log.getRole() == ClientRole.MONITOR && log.isWrite()) {
            return Optional.of(new Detection(RULE_ID, SEVERITY, MESSAGE));
        }

        return Optional.empty();
    }
}
