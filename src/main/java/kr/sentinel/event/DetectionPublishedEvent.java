package kr.sentinel.event;

import kr.sentinel.detection.Detection;

public record DetectionPublishedEvent(
        Long commandLogId,
        Detection detection
) {
}
