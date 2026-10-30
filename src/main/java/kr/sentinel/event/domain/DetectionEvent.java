package kr.sentinel.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.sentinel.detection.Detection;
import kr.sentinel.detection.Severity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_detection_event_detected_at", columnList = "detected_at"),
        @Index(name = "idx_detection_event_command_log_id", columnList = "command_log_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetectionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_log_id", nullable = false)
    private Long commandLogId;

    @Column(name = "rule_id", nullable = false, length = 10)
    private String ruleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private DetectionEvent(
            Long commandLogId,
            String ruleId,
            Severity severity,
            String message,
            LocalDateTime detectedAt
    ) {
        this.commandLogId = commandLogId;
        this.ruleId = ruleId;
        this.severity = severity;
        this.message = message;
        this.detectedAt = detectedAt;
    }

    public static DetectionEvent create(
            Long commandLogId,
            Detection detection,
            LocalDateTime detectedAt
    ) {
        return DetectionEvent.builder()
                .commandLogId(commandLogId)
                .ruleId(detection.ruleId())
                .severity(detection.severity())
                .message(detection.message())
                .detectedAt(detectedAt)
                .build();
    }
}
