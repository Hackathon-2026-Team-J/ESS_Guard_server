package kr.sentinel.event;

import kr.sentinel.detection.Detection;

public record DetectionPublishedEvent(
        String deviceId,
        Long commandLogId,
        Long telemetryId,
        Detection detection
) {
}
