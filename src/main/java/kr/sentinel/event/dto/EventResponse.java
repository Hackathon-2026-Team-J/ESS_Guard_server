package kr.sentinel.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "탐지 이벤트 ID", example = "1")
        Long id,
        @Schema(description = "탐지 규칙 ID", example = "R2")
        String ruleId,
        @Schema(description = "심각도", example = "HIGH")
        Severity severity,
        @Schema(description = "탐지 메시지", example = "읽기 전용 계정의 쓰기 명령")
        String message,
        @Schema(description = "탐지 시각", example = "2026-11-07T09:01:02")
        LocalDateTime detectedAt,
        @Schema(description = "원인 명령 로그 ID", example = "4")
        Long commandLogId,
        @Schema(description = "명령을 보낸 클라이언트 ID", example = "HMI-01")
        String clientId,
        @Schema(description = "클라이언트 역할", example = "MONITOR")
        ClientRole role,
        @Schema(description = "대상 ESS 장치 ID", example = "ESS-02")
        String deviceId,
        @Schema(description = "Modbus 기능 코드 (6, 16은 쓰기 명령)", example = "6")
        int functionCode,
        @Schema(description = "레지스터 주소", example = "11")
        int registerAddress,
        @Schema(description = "명령 값", example = "20")
        int value,
        @Schema(description = "명령 시점의 ESS 상태 (없으면 null)", example = "RUNNING", nullable = true)
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
