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
        @Schema(description = "탐지 규칙 ID", example = "R1")
        String ruleId,
        @Schema(description = "심각도", example = "HIGH")
        Severity severity,
        @Schema(description = "탐지 메시지", example = "계획 방향(충전)과 반대인 방전 명령")
        String message,
        @Schema(description = "탐지 시각", example = "2026-11-07T09:01:02")
        LocalDateTime detectedAt,
        @Schema(description = "대상 ESS 장치 ID", example = "ESS-02")
        String deviceId,
        @Schema(description = "원인 명령 로그 ID (측정값 기반 탐지면 null)", example = "4", nullable = true)
        Long commandLogId,
        @Schema(description = "원인 측정값 ID (명령 기반 탐지면 null)", example = "12", nullable = true)
        Long telemetryId,
        @Schema(description = "명령을 보낸 클라이언트 ID (측정값 기반 탐지면 null)", example = "HMI-01", nullable = true)
        String clientId,
        @Schema(description = "클라이언트 역할 (측정값 기반 탐지면 null)", example = "MONITOR", nullable = true)
        ClientRole role,
        @Schema(description = "Modbus 기능 코드, 6과 16은 쓰기 명령 (측정값 기반 탐지면 null)", example = "6", nullable = true)
        Integer functionCode,
        @Schema(description = "레지스터 주소 (측정값 기반 탐지면 null)", example = "11", nullable = true)
        Integer registerAddress,
        @Schema(description = "명령 값 (측정값 기반 탐지면 null)", example = "20", nullable = true)
        Integer value,
        @Schema(description = "명령 시점의 ESS 상태 (없으면 null)", example = "RUNNING", nullable = true)
        EssState essState
) {

    public static EventResponse of(
            DetectionEvent event,
            CommandLog commandLog
    ) {
        EventResponseBuilder builder = EventResponse.builder()
                .id(event.getId())
                .ruleId(event.getRuleId())
                .severity(event.getSeverity())
                .message(event.getMessage())
                .detectedAt(event.getDetectedAt())
                .deviceId(event.getDeviceId())
                .commandLogId(event.getCommandLogId())
                .telemetryId(event.getTelemetryId());

        if (commandLog == null) {
            return builder.build();
        }

        return builder
                .clientId(commandLog.getClientId())
                .role(commandLog.getRole())
                .functionCode(commandLog.getFunctionCode())
                .registerAddress(commandLog.getRegisterAddress())
                .value(commandLog.getValue())
                .essState(commandLog.getEssState())
                .build();
    }
}
