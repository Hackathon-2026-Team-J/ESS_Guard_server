package kr.sentinel.event.dto;

import java.time.LocalDateTime;
import kr.sentinel.command.domain.ClientRole;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.domain.EssState;
import kr.sentinel.detection.Severity;
import kr.sentinel.event.domain.DetectionEvent;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record EventResponse(
        Long id,
        String ruleId,
        Severity severity,
        String message,
        LocalDateTime detectedAt,
        Long commandLogId,
        String clientId,
        ClientRole role,
        String deviceId,
        int functionCode,
        int registerAddress,
        int value,
        EssState essState
) {

    public static EventResponse of(
            DetectionEvent event,
            CommandLog commandLog
    ) {
        return EventResponse.builder()
                .id(event.getId())
                .ruleId(event.getRuleId())
                .severity(event.getSeverity())
                .message(event.getMessage())
                .detectedAt(event.getDetectedAt())
                .commandLogId(event.getCommandLogId())
                .clientId(commandLog.getClientId())
                .role(commandLog.getRole())
                .deviceId(commandLog.getDeviceId())
                .functionCode(commandLog.getFunctionCode())
                .registerAddress(commandLog.getRegisterAddress())
                .value(commandLog.getValue())
                .essState(commandLog.getEssState())
                .build();
    }
}
