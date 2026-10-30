package kr.sentinel.detection;

import java.util.Optional;
import kr.sentinel.command.CommandLog;

public interface Rule {

    Optional<Detection> check(CommandLog log);
}
