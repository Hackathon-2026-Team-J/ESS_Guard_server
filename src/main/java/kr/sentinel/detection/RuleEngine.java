package kr.sentinel.detection;

import java.util.List;
import java.util.Optional;
import kr.sentinel.command.domain.CommandLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final List<Rule> rules;

    public List<Detection> evaluate(CommandLog log) {
        return rules.stream()
                .map(rule -> rule.check(log))
                .flatMap(Optional::stream)
                .toList();
    }
}
