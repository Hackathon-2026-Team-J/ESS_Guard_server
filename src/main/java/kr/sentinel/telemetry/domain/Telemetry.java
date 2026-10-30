package kr.sentinel.telemetry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_telemetry_processed_measured_at", columnList = "processed, measured_at"),
        @Index(name = "idx_telemetry_device_id_measured_at", columnList = "device_id, measured_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Telemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "actual_kw", nullable = false)
    private int actualKw;

    @Column(nullable = false)
    private double soc;

    @Column(name = "labeled_anomaly", nullable = false)
    private boolean labeledAnomaly;

    private boolean processed;

    @Builder(access = AccessLevel.PRIVATE)
    private Telemetry(
            String deviceId,
            LocalDateTime measuredAt,
            int actualKw,
            double soc,
            boolean labeledAnomaly
    ) {
        this.deviceId = deviceId;
        this.measuredAt = measuredAt;
        this.actualKw = actualKw;
        this.soc = soc;
        this.labeledAnomaly = labeledAnomaly;
        this.processed = false;
    }

    public static Telemetry create(
            String deviceId,
            LocalDateTime measuredAt,
            int actualKw,
            double soc,
            boolean labeledAnomaly
    ) {
        return Telemetry.builder()
                .deviceId(deviceId)
                .measuredAt(measuredAt)
                .actualKw(actualKw)
                .soc(soc)
                .labeledAnomaly(labeledAnomaly)
                .build();
    }

    public void markProcessed() {
        this.processed = true;
    }

    public void resetProcessed() {
        this.processed = false;
    }
}
